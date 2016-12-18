package at.brandl.lws.notice.interaction.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Interaction;

public class InteractionDsDao extends AbstractDsDao {

	public Map<String, Integer> getInteractions(String childKey, Date from, Date to) {

		if (childKey == null) {
			return Collections.emptyMap();
		}

		DatastoreService datastoreService = getDatastoreService();

		Query query = new Query(Interaction.KIND).setAncestor(DsUtil.toKey(childKey));
		Filter filter = createDateFilter(from, to);
		if (filter != null) {
			query.setFilter(filter);
		}
		PreparedQuery preparedQuery = datastoreService.prepare(query);

		Map<String, Integer> result = new HashMap<>();
		for (Entity interaction : preparedQuery.asIterable()) {
			addToResult(interaction, result);
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

	public void storeInteraction(String childKey, String childKeyOther, Date date, Integer count) {

		// date = normalizeDate(date);
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {

			storeInteractionInDs(datastoreService, childKey, childKeyOther, count, date);
			storeInteractionInDs(datastoreService, childKeyOther, childKey, count, date);
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

	private void storeInteractionInDs(DatastoreService datastoreService, String childKey, String childKeyOther,
			Integer count, Date date) {
		final Entity interaction = toEntity(childKey, childKeyOther, count, date);

		// if (exists(interaction, datastoreService)) {
		// transaction.rollback();
		// throw new IllegalArgumentException(interaction
		// + " existiert bereits!");
		// }

		datastoreService.put(interaction);
	}

	private Entity toEntity(String childKey, String childOtherKey, Integer count, Date date) {
		Entity entity = new Entity(Interaction.KIND, DsUtil.toKey(childKey));
		entity.setProperty(Interaction.PARTNER, DsUtil.toKey(childOtherKey));
		entity.setProperty(Interaction.COUNT, count);
		entity.setProperty(Interaction.DATE, date);
		return entity;
	}

}
