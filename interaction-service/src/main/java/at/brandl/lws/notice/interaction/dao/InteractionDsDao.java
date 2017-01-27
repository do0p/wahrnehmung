package at.brandl.lws.notice.interaction.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.CacheEntity;
import at.brandl.lws.notice.dao.CacheKey;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants;
import at.brandl.lws.notice.shared.util.Constants.Interaction;

public class InteractionDsDao extends AbstractDsDao {

	public Map<String, Map<String, Integer>> getAllInteractions(Date from, Date to) {
		Map<String, Map<String, Integer>> allInteractions = new HashMap<>();

		DatastoreService ds = getDatastoreService();
		Set<Key> distinctChildren = getDistinctChildren(ds);

		for (Key childKey : distinctChildren) {
			Map<String, Integer> interactions = doGetInteractions(childKey, from, to, ds);
			if (!interactions.isEmpty()) {
				allInteractions.put(DsUtil.toString(childKey), interactions);
			}
		}

		return allInteractions;
	}

	private Set<Key> getDistinctChildren(DatastoreService ds) {
		Set<Key> distinctChildren = new HashSet<>();
		Query query = new Query(Constants.Interaction.KIND).setKeysOnly();
		Iterable<Entity> interactions = ds.prepare(query).asIterable();
		for (Entity interaction : interactions) {
			distinctChildren.add(interaction.getKey().getParent());
		}
		return distinctChildren;
	}

	public Map<String, Integer> getInteractions(String childKey, Date from, Date to) {

		if (childKey == null) {
			return Collections.emptyMap();
		}

		DatastoreService ds = getDatastoreService();

		return doGetInteractions(DsUtil.toKey(childKey), from, to, ds);
	}

	private Map<String, Integer> doGetInteractions(Key childKey, Date from, Date to, DatastoreService ds) {

		MemcacheService cache = getCache(Interaction.KIND);
		CacheKey cacheKey = new CacheKey(childKey, from, to);
		HashMap<String, Integer> result = getFromCache(cacheKey, ds, Interaction.KIND);
		if (result == null) {

			Transaction tx = ds.beginTransaction();
			Query query = new Query(Interaction.KIND).setAncestor(childKey);
			Filter filter = createDateFilter(from, to);
			if (filter != null) {
				query.setFilter(filter);
			}
			PreparedQuery preparedQuery = ds.prepare(tx, query);

			result = new HashMap<>();
			for (Entity interaction : preparedQuery.asIterable()) {
				addToResult(interaction, result);
			}
			insertIntoCache(cacheKey, result, ds, tx, Interaction.KIND);
			tx.rollback();
		}
		return result;
	}






	private Filter createDateFilter(Date from, Date to) {

		List<Filter> filters = new ArrayList<>();
		if (from != null) {
			filters.add(new FilterPredicate(Interaction.DATE, FilterOperator.GREATER_THAN_OR_EQUAL, from));
		}
		if (to != null) {
			filters.add(new FilterPredicate(Interaction.DATE, FilterOperator.LESS_THAN_OR_EQUAL, to));
		}

		if (filters.isEmpty()) {
			return null;
		}

		if (filters.size() == 1) {
			return filters.get(0);
		}

		return new CompositeFilter(CompositeFilterOperator.AND, filters);
	}

	public void incrementInteraction(String childKey, String childKeyOther, Date date, int increment) {
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {

			incrementInteractionInDs(datastoreService, childKey, childKeyOther, increment, date);
			incrementInteractionInDs(datastoreService, childKeyOther, childKey, increment, date);
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void addToResult(Entity interaction, Map<String, Integer> result) {

		Integer count = DsUtil.getInteger(interaction, Interaction.COUNT);
		String partner = DsUtil.getKeyString(interaction, Interaction.PARTNER);
		if (count == null) {
			System.err.println("count is null");
			return;
		}
		if (partner == null) {
			System.err.println("partner is null");
			return;
		}
		Integer oldCount = result.get(partner);
		if (oldCount != null) {
			count = new Integer(count.intValue() + oldCount.intValue());
		}
		result.put(partner, count);
	}

	private void incrementInteractionInDs(DatastoreService datastoreService, String childKey, String childKeyOther,
			int increment, Date date) {

		Entity entity = getInteraction(datastoreService, childKey, childKeyOther, date);
		entity.setProperty(Interaction.COUNT, getCount(entity) + increment);
		datastoreService.put(entity);
	}

	private Entity getInteraction(DatastoreService datastoreService, String childKey, String childKeyOther, Date date) {

		PreparedQuery preparedQuery = datastoreService
				.prepare(createQueryForInteraction(childKey, childKeyOther, date));
		List<Entity> result = preparedQuery.asList(FetchOptions.Builder.withDefaults());

		if (result.isEmpty()) {
			return toEntity(childKey, childKeyOther, 0, date);
		}

		if (result.size() > 1) {
			sumUp(datastoreService, result);
		}

		return result.get(0);
	}

	private Query createQueryForInteraction(String childKey, String childKeyOther, Date date) {
		List<Filter> filters = new ArrayList<>();
		filters.add(new FilterPredicate(Interaction.DATE, FilterOperator.EQUAL, date));
		filters.add(new FilterPredicate(Interaction.PARTNER, FilterOperator.EQUAL, DsUtil.toKey(childKeyOther)));
		Filter filter = new CompositeFilter(CompositeFilterOperator.AND, filters);

		Query query = new Query(Interaction.KIND).setAncestor(DsUtil.toKey(childKey));
		query.setFilter(filter);
		return query;
	}

	private void sumUp(DatastoreService datastoreService, List<Entity> result) {
		int count = 0;
		for (int i = 0; i < result.size(); i++) {
			Entity entity = result.get(i);
			count += getCount(entity);
			if (i > 0) {
				datastoreService.delete(entity.getKey());
			}
		}
		result.get(0).setProperty(Interaction.COUNT, count);
	}

	private int getCount(Entity entity) {
		return ((Number) entity.getProperty(Interaction.COUNT)).intValue();
	}

	private Entity toEntity(String childKey, String childOtherKey, Integer count, Date date) {
		Entity entity = new Entity(Interaction.KIND, DsUtil.toKey(childKey));
		entity.setProperty(Interaction.PARTNER, DsUtil.toKey(childOtherKey));
		entity.setProperty(Interaction.COUNT, count);
		entity.setProperty(Interaction.DATE, date);
		return entity;
	}

	public void deleteAllInteractions(String keyString) {
		DatastoreService ds = getDatastoreService();
		Key childKey = DsUtil.toKey(keyString);
		Map<String, Integer> interactions = doGetInteractions(childKey, null, null, ds);

		for (String otherChild : interactions.keySet()) {
			Key otherChildKey = DsUtil.toKey(otherChild);
			Transaction transaction = ds.beginTransaction(TransactionOptions.Builder.withXG(true));
			try {
				List<Key> toDelete = new ArrayList<Key>();

				for (Entity entity : ds.prepare(createQuery(otherChildKey, childKey)).asIterable()) {
					toDelete.add(entity.getKey());
				}

				for (Entity entity : ds.prepare(createQuery(childKey, otherChildKey)).asIterable()) {
					toDelete.add(entity.getKey());
				}

				ds.delete(toDelete);
				transaction.commit();

			} finally {
				if (transaction.isActive()) {
					transaction.rollback();
				}
			}
		}
	}

	private Query createQuery(Key childKey, Key otherChildKey) {
		return new Query(Constants.Interaction.KIND, childKey)
				.setFilter(new FilterPredicate(Constants.Interaction.PARTNER, FilterOperator.EQUAL, otherChildKey))
				.setKeysOnly();
	}

}
