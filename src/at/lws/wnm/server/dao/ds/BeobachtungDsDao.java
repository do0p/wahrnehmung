package at.lws.wnm.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;
import at.lws.wnm.shared.model.GwtSection;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.gwt.thirdparty.guava.common.base.Predicate;
import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimaps;
import com.google.gwt.view.client.Range;

public class BeobachtungDsDao extends AbstractDsDao {

	public static String BEOBACHTUNGS_DAO_MEMCACHE = "beobachtungsDao";
	public static String BEOBACHTUNGS_GROUP_KIND = "BeobachtungsGroup";
	public static String BEOBACHTUNGS_KEY_FIELD = "beobachtungsKey";
	public static String BEOBACHTUNG_KIND = "BeobachtungDs";
	public static String DATE_FIELD = "date";
	public static String SECTION_KEY_FIELD = "sectionKey";
	public static String USER_FIELD = "user";
	public static String DURATION_FIELD = "duration";
	public static String TEXT_FIELD = "text";
	public static String SOCIAL_FIELD = "social";

	private static int EXPECTED_SECTION_PER_CHILD = 100;
	private static int EXPECTED_BEOBACHTUNG_PER_SECTION = 20;

	private final Map<String, Date> dirty = new ConcurrentHashMap<String, Date>();

	private SectionDsDao sectionDao = DaoRegistry.get(SectionDsDao.class);

	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range, User user, boolean generateSummaries) {

		List<GwtBeobachtung> beobachtungen = getBeobachtungen(filter, user,
				generateSummaries);

		Collections.sort(beobachtungen);

		return getRange(beobachtungen, range);

	}

	private List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			User user, boolean generateSummaries) {
		Multimap<String, GwtBeobachtung> allBeobachtungen = getAllGwtBeobachtungen(filter
				.getChildKey());

		Multimap<String, GwtBeobachtung> filteredBeobachtungen = Multimaps
				.filterEntries(allBeobachtungen, createFilter(filter, user));
		List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();

		if (generateSummaries) {
			for (String sectionKey : filteredBeobachtungen.keySet()) {

				Collection<GwtBeobachtung> beobachtungen = filteredBeobachtungen
						.get(sectionKey);

				if (beobachtungen.isEmpty()) {
					continue;
				}

				GwtBeobachtung summary = createSummary(beobachtungen,
						sectionKey);
				result.add(summary);
			}
		}
		result.addAll(new HashSet<GwtBeobachtung>(filteredBeobachtungen
				.values()));
		return result;
	}

	private GwtBeobachtung createSummary(Collection<GwtBeobachtung> beobachtungen,
			String sectionKey) {

		ResourceBundle bundle = ResourceBundle.getBundle("messages");

		List<GwtBeobachtung> tmpList = getSortedList(beobachtungen);
		GwtBeobachtung firstBeobachtung = tmpList.get(0);
		GwtBeobachtung lastBeobachtung = tmpList
				.get(tmpList.size() - 1);
		String sectionName = sectionDao.getSectionName(sectionKey);
		String childName = firstBeobachtung.getChildName();
		Date startDate = firstBeobachtung.getDate();
		Date endDate = lastBeobachtung.getDate();
		GwtBeobachtung summary = new GwtBeobachtung();
		summary.setChildKey(firstBeobachtung.getChildKey());
		summary.setSectionKey(sectionKey);
		summary.setChildName(childName);
		summary.setSectionName(sectionName);
		summary.setDate(new Date());
		MessageFormat messageFormat = new MessageFormat(
				bundle.getString("summary"), Locale.GERMAN);
		summary.setText(messageFormat.format(new Object[] { childName,
				sectionName, tmpList.size(), startDate, endDate }));
		summary.setUser("System");
		return summary;
	}

	private List<GwtBeobachtung> getSortedList(
			Collection<GwtBeobachtung> beobachtungen) {
		List<GwtBeobachtung> tmpList = new ArrayList<GwtBeobachtung>(beobachtungen);
		Collections.sort(tmpList, new Comparator<GwtBeobachtung>() {

			@Override
			public int compare(GwtBeobachtung o1, GwtBeobachtung o2) {
				return o2.compareTo(o1);
			}
		});
		return tmpList;
	}

	private List<GwtBeobachtung> getRange(List<GwtBeobachtung> result,
			Range range) {
		int startIndex = range.getStart();
		int endIndex = startIndex + range.getLength();
		endIndex = Math.min(endIndex, result.size());
		if (startIndex != 0 || endIndex < result.size()) {
			result = result.subList(startIndex, endIndex);
		}
		return new ArrayList<GwtBeobachtung>(result);
	}

	private Predicate<Entry<String, GwtBeobachtung>> createFilter(
			final BeobachtungsFilter filter, final User user) {

		return new Predicate<Map.Entry<String, GwtBeobachtung>>() {

			@Override
			public boolean apply(Entry<String, GwtBeobachtung> entry) {
				String sectionKey = entry.getKey();
				GwtBeobachtung beobachtung = entry.getValue();

				if (user != null
						&& !beobachtung.getUser().equalsIgnoreCase(
								user.getEmail())) {
					return false;
				}

				if (filter.getSectionKey() != null
						&& !filter.getSectionKey().equals(sectionKey)) {
					return false;
				}

				Date[] timeRange = filter.getTimeRange();
				if (timeRange != null && timeRange.length == 2) {
					Date startDate = timeRange[0];
					Date endDate = timeRange[1];

					if (beobachtung.getDate().before(startDate)) {
						return false;
					}
					if (beobachtung.getDate().after(endDate)) {
						return false;
					}
				}

				return true;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private Multimap<String, GwtBeobachtung> getAllGwtBeobachtungen(
			String childKey) {
		MemcacheService cache = getCache();
		if (!cache.contains(childKey) || updateNeeded(childKey)) {
			synchronized (this) {
				if (!cache.contains(childKey) || updateNeeded(childKey)) {
					setUpdated(childKey);
					Multimap<String, GwtBeobachtung> cachedEntities = getAllBeobachtungen(childKey);
					cache.put(childKey, cachedEntities);
				}
			}
		}
		return (Multimap<String, GwtBeobachtung>) cache.get(childKey);
	}

	private Multimap<String, GwtBeobachtung> getAllBeobachtungen(String childKey) {

		Multimap<String, GwtBeobachtung> sectionToBeobachtung = ArrayListMultimap
				.<String, GwtBeobachtung> create(EXPECTED_SECTION_PER_CHILD,
						EXPECTED_BEOBACHTUNG_PER_SECTION);

		for (Entity beobachtung : queryAllBeobachtungen(childKey)) {

			GwtBeobachtung gwtBeobachtung = toGwt(beobachtung);
			String sectionKey = gwtBeobachtung.getSectionKey();
			sectionToBeobachtung.put(sectionKey, gwtBeobachtung);

			setParentSections(sectionKey, gwtBeobachtung, sectionToBeobachtung);
		}
		return sectionToBeobachtung;
	}

	private Iterable<Entity> queryAllBeobachtungen(String childKey) {
		Query query = new Query(BEOBACHTUNG_KIND, toKey(childKey));
		return getDatastoreService().prepare(query).asIterable();
	}

	private void setParentSections(String sectionKey,
			GwtBeobachtung gwtBeobachtung,
			Multimap<String, GwtBeobachtung> sectionToBeobachtung) {
		GwtSection section = sectionDao.getSection(sectionKey);
		String parentKey = section.getParentKey();
		if (parentKey != null) {
			sectionToBeobachtung.put(parentKey, gwtBeobachtung);
			setParentSections(parentKey, gwtBeobachtung, sectionToBeobachtung);
		}
	}

	private void setUpdated(String childKey) {
		dirty.remove(childKey);
	}

	private boolean updateNeeded(String childKey) {
		return dirty.get(childKey) != null;
	}

	private void setUpdateNeeded(String childKey) {
		dirty.put(childKey, new Date());
	}

	public int getRowCount(BeobachtungsFilter filter, User user,
			boolean generateSummaries) {
		return getBeobachtungen(filter, user, generateSummaries).size();
	}

	public GwtBeobachtung getBeobachtung(String beobachtungsKey) {
		return toGwt(getCachedEntity(toKey(beobachtungsKey)));
	}

	public boolean beobachtungenExist(Collection<String> sectionKeys,
			DatastoreService datastoreService) {
		if (sectionKeys == null || sectionKeys.isEmpty()) {
			return false;
		}
		Filter sectionFilter = createSectionFilter(sectionKeys);
		Query query = new Query(BEOBACHTUNG_KIND).setFilter(sectionFilter);
		return count(query, withDefaults(), datastoreService) > 0;
	}

	public synchronized void storeBeobachtung(GwtBeobachtung gwtBeobachtung,
			User user, String masterBeobachtungsKey) {

		Entity beobachtung = toEntity(gwtBeobachtung, user);
		getDatastoreService().put(beobachtung);
		insertIntoCache(beobachtung);
		setUpdateNeeded(gwtBeobachtung.getChildKey());
		if (masterBeobachtungsKey != null) {
			Entity beobachtungsGroup = new Entity(BEOBACHTUNGS_GROUP_KIND,
					toKey(masterBeobachtungsKey));
			beobachtungsGroup.setProperty(BEOBACHTUNGS_KEY_FIELD,
					beobachtung.getKey());
			getDatastoreService().put(beobachtungsGroup);
		}
		String key = toString(beobachtung.getKey());
		gwtBeobachtung.setKey(key);
	}

	public synchronized void deleteAllFromChild(String childKey) {

		DatastoreService datastoreService = getDatastoreService();
		Query query = new Query(BEOBACHTUNG_KIND, toKey(childKey))
				.setKeysOnly();
		Transaction transaction = datastoreService.beginTransaction();
		try {
			Iterable<Entity> allBeobachtungen = datastoreService.prepare(query)
					.asIterable();
			for (Entity beobachtung : allBeobachtungen) {
				deleteEntity(beobachtung.getKey(), datastoreService);
			}
			transaction.commit();
			setUpdateNeeded(childKey);
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public synchronized void deleteBeobachtung(String beobachtungsKey) {

		GwtBeobachtung beobachtung = getBeobachtung(beobachtungsKey);
		deleteEntity(toKey(beobachtungsKey));
		setUpdateNeeded(beobachtung.getChildKey());
	}

	@Override
	protected String getMemcacheServiceName() {
		return BEOBACHTUNGS_DAO_MEMCACHE;
	}

	private Filter createSectionFilter(Collection<String> sectionKeys) {
		Filter sectionFilter;
		if (sectionKeys.size() == 1) {
			sectionFilter = createSectionFilter(sectionKeys.iterator().next());
		} else {
			Collection<Filter> subSectionFilters = new ArrayList<Filter>();
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

	private GwtBeobachtung toGwt(Entity entity) {
		String childKey = toString(entity.getParent());
		String sectionKey = toString((Key) entity
				.getProperty(SECTION_KEY_FIELD));
		String duration = (String) entity.getProperty(DURATION_FIELD);
		String social = (String) entity.getProperty(SOCIAL_FIELD);
		Date date = (Date) entity.getProperty(DATE_FIELD);
		String text = ((Text) entity.getProperty(TEXT_FIELD)).getValue();
		String user = ((User) entity.getProperty(USER_FIELD)).getEmail();
		String childName = getChildDao().getChildName(childKey);
		String sectionName = getSectionDao().getSectionName(sectionKey);

		GwtBeobachtung beobachtung = new GwtBeobachtung();
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
		String key = gwtBeobachtung.getKey();
		Entity entity;
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
		DurationEnum duration = gwtBeobachtung.getDuration();
		if (duration != null) {
			entity.setProperty(DURATION_FIELD, duration.name());
		}
		SocialEnum social = gwtBeobachtung.getSocial();
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
