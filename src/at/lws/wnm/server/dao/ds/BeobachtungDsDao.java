package at.lws.wnm.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.gwt.view.client.Range;

public class BeobachtungDsDao extends AbstractDsDao {

	public static final String BEOBACHTUNGS_DAO_MEMCACHE = "beobachtungsDao";
	public static final String BEOBACHTUNGS_GROUP_KIND = "BeobachtungsGroup";
	public static final String BEOBACHTUNG_KIND = "BeobachtungDs";
	public static final String DATE_FIELD = "date";
	public static final String SECTION_KEY_FIELD = "sectionKey";
	public static final String USER_FIELD = "user";
	public static final String DURATION_FIELD = "duration";
	public static final String TEXT_FIELD = "text";
	public static final String SOCIAL_FIELD = "social";

	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range, User user) {

		final Query query = createQuery(filter, user);
		final Iterable<Entity> dsResult = execute(query,
				withOffset(range.getStart()).limit(range.getLength()));

		return mapToGwtBeobachtung(dsResult);
	}

	public int getRowCount(BeobachtungsFilter filter, User user) {
		final Query query = createQuery(filter, user);
		return count(query, withDefaults());
	}

	public GwtBeobachtung getBeobachtung(String beobachtungsKey) {
		return toGwt(getCachedEntity(toKey(beobachtungsKey)));
	}

	public boolean beobachtungenExist(Collection<String> sectionKeys,
			DatastoreService datastoreService) {
		if (sectionKeys == null || sectionKeys.isEmpty()) {
			return false;
		}
		final Filter sectionFilter = createSectionFilter(sectionKeys);
		final Query query = new Query(BEOBACHTUNG_KIND)
				.setFilter(sectionFilter);
		return count(query, withDefaults(), datastoreService) > 0;
	}

	public void storeBeobachtung(GwtBeobachtung gwtBeobachtung, User user,
			String masterBeobachtungsKey) {
		final Entity beobachtung = toEntity(gwtBeobachtung, user);
		getDatastoreService().put(beobachtung);
		insertIntoCache(beobachtung);
		if (masterBeobachtungsKey != null) {
			final Entity beobachtungsGroup = new Entity(
					BEOBACHTUNGS_GROUP_KIND, toKey(masterBeobachtungsKey));
			getDatastoreService().put(beobachtungsGroup);
		}
		final String key = toString(beobachtung.getKey());
		gwtBeobachtung.setKey(key);
	}

	public void deleteAllFromChild(String childKey) {

		final DatastoreService datastoreService = getDatastoreService();
		final Query query = new Query(BEOBACHTUNG_KIND, toKey(childKey))
				.setKeysOnly();

		final Transaction transaction = datastoreService.beginTransaction();
		try {
			final Iterable<Entity> allBeobachtungen = datastoreService.prepare(
					query).asIterable();
			for (Entity beobachtung : allBeobachtungen) {
				deleteEntity(beobachtung.getKey(), datastoreService);
			}
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void deleteBeobachtung(String beobachtungsKey) {
		deleteEntity(toKey(beobachtungsKey));
	}

	@Override
	protected String getMemcacheServiceName() {
		return BEOBACHTUNGS_DAO_MEMCACHE;
	}

	private Query createQuery(BeobachtungsFilter filter, User user) {

		final Collection<String> childSectionKeys = getSectionDao().getAllChildKeys(
				filter.getSectionKey());

		final String childKey = filter.getChildKey();
		final Query query;
		if (childKey == null) {
			query = new Query(BEOBACHTUNG_KIND);
		} else {
			query = new Query(BEOBACHTUNG_KIND, toKey(childKey));
		}
		final List<Filter> subFilters = new ArrayList<Filter>();

		final String sectionKey = filter.getSectionKey();
		if (sectionKey != null) {
			final List<String> sectionKeys = new ArrayList<String>(
					childSectionKeys);
			sectionKeys.add(sectionKey);
			subFilters.add(createSectionFilter(sectionKeys));
		}

		if (user != null) {
			subFilters.add(createEqualsPredicate(USER_FIELD, user));
		}

		if (!subFilters.isEmpty()) {
			final Filter dsFilter;
			if (subFilters.size() == 1) {
				dsFilter = subFilters.get(0);
			} else {
				dsFilter = new Query.CompositeFilter(
						CompositeFilterOperator.AND, subFilters);
			}
			query.setFilter(dsFilter);
		}

		query.addSort(DATE_FIELD, SortDirection.DESCENDING);

		return query;

	}

	private Filter createSectionFilter(Collection<String> sectionKeys) {
		final Filter sectionFilter;
		if (sectionKeys.size() == 1) {
			sectionFilter = createSectionFilter(sectionKeys.iterator().next());
		} else {
			final Collection<Filter> subSectionFilters = new ArrayList<Filter>();
			for (String sectionKey : sectionKeys) {
				subSectionFilters.add(createSectionFilter(sectionKey));
			}
			sectionFilter = new Query.CompositeFilter(
					CompositeFilterOperator.OR, subSectionFilters);
		}
		return sectionFilter;
	}

	private FilterPredicate createSectionFilter(String sectionKey) {
		return new Query.FilterPredicate(SECTION_KEY_FIELD,
				FilterOperator.EQUAL, toKey(sectionKey));
	}

	private List<GwtBeobachtung> mapToGwtBeobachtung(Iterable<Entity> dsResult) {
		final List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();
		for (Entity beobachtung : dsResult) {
			result.add(toGwt(beobachtung));
		}
		return result;
	}

	private GwtBeobachtung toGwt(Entity entity) {
		final String childKey = toString(entity.getParent());
		final String sectionKey = toString((Key) entity
				.getProperty(SECTION_KEY_FIELD));
		final String duration = (String) entity.getProperty(DURATION_FIELD);
		final String social = (String) entity.getProperty(SOCIAL_FIELD);
		final Date date = (Date) entity.getProperty(DATE_FIELD);
		final String text = ((Text) entity.getProperty(TEXT_FIELD)).getValue();
		final String user = ((User) entity.getProperty(USER_FIELD)).getEmail();
		final String childName = getChildDao().getChildName(childKey);
		final String sectionName = getSectionDao().getSectionName(sectionKey);

		final GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(toString(entity.getKey()));
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setText(text);
		beobachtung.setUser(user);
		beobachtung.setChildName(childName);
		beobachtung.setSectionName(sectionName);
		if (duration != null) {
			beobachtung.setDuration(DurationEnum.valueOf(duration));
		}
		if (social != null) {
			beobachtung.setSocial(SocialEnum.valueOf(social));
		}
		return beobachtung;
	}

	private Entity toEntity(GwtBeobachtung gwtBeobachtung, User user) {
		final String key = gwtBeobachtung.getKey();
		final Entity entity;
		if (key == null) {
			entity = new Entity(BEOBACHTUNG_KIND,
					toKey(gwtBeobachtung.getChildKey()));
		} else {
			entity = new Entity(toKey(key));
		}
		entity.setProperty(SECTION_KEY_FIELD,
				toKey(gwtBeobachtung.getSectionKey()));
		entity.setProperty(DATE_FIELD, gwtBeobachtung.getDate());
		entity.setProperty(TEXT_FIELD, new Text(gwtBeobachtung.getText()));
		entity.setProperty(USER_FIELD, user);
		final DurationEnum duration = gwtBeobachtung.getDuration();
		if (duration != null) {
			entity.setProperty(DURATION_FIELD, duration.name());
		}
		final SocialEnum social = gwtBeobachtung.getSocial();
		if (social != null) {
			entity.setProperty(SOCIAL_FIELD, social.name());
		}
		return entity;
	}

	private ChildDsDao getChildDao() {
		return DaoRegistry.get(ChildDsDao.class);
	}

	private SectionDsDao getSectionDao() {
		return DaoRegistry.get(SectionDsDao.class);
	}

}
