package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.model.GwtSummary;
import at.brandl.lws.notice.server.dao.DaoRegistry;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.gwt.thirdparty.guava.common.base.Predicate;
import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimaps;
import com.google.gwt.view.client.Range;

public class BeobachtungDsDao extends AbstractDsDao {

	private static final String BEOBACHTUNGS_ARCHIVE_DAO_MEMCACHE = "beobachtungsArchiveDao";
	private static final String BEOBACHTUNGS_DAO_MEMCACHE = "beobachtungsDao";
	public static final String BEOBACHTUNGS_GROUP_KIND = "BeobachtungsGroup";
	public static final String BEOBACHTUNGS_KEY_FIELD = "beobachtungsKey";
	private static final String BEOBACHTUNG_KIND = "BeobachtungDs";
	private static final String BEOBACHTUNG_ARCHIVE_KIND = "BeobachtungArchiveDs";
	private static final String BEOBACHTUNG_GROUP_ARCHIVE_KIND = "BeobachtungsGroupArchiveDs";
	public static final String DATE_FIELD = "date";
	public static final String SECTION_KEY_FIELD = "sectionKey";
	public static final String USER_FIELD = "user";
	public static final String DURATION_FIELD = "duration";
	public static final String TEXT_FIELD = "text";
	public static final String SOCIAL_FIELD = "social";

	private static final int EXPECTED_SECTION_PER_CHILD = 100;
	private static final int EXPECTED_BEOBACHTUNG_PER_SECTION = 20;

	private final Map<String, Date> dirty = new ConcurrentHashMap<String, Date>();

	private SectionDsDao sectionDao = DaoRegistry.get(SectionDsDao.class);
	private ChildDsDao childDao = DaoRegistry.get(ChildDsDao.class);

	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range) {

		List<GwtBeobachtung> beobachtungen = getBeobachtungen(filter);

		Collections.sort(beobachtungen);

		return getRange(beobachtungen, range);

	}

	private List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter) {

		final List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();
		Collection<String> childKeys = getChildKeys(filter);
		Date oldestEntry = null;
		if (childKeys.size() > 1) {
			oldestEntry = shortListLimit();
			filter.setTimeRange(oldestEntry, today());
			filter.setSinceLastDevelopmementDialogue(false);
		}
		String origChildKey = filter.getChildKey();
		for (String childKey : childKeys) {
			filter.setChildKey(childKey);
			List<GwtBeobachtung> beobachtungen = getCachedBeobachtungen(filter,
					childKey, oldestEntry);
			result.addAll(beobachtungen);
		}
		filter.setChildKey(origChildKey);
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<GwtBeobachtung> getCachedBeobachtungen(
			BeobachtungsFilter filter, String childKey, Date oldestEntry) {
		List<GwtBeobachtung> beobachtungen;
		boolean archived = filter.isArchived();
		if (!getCache(getCacheName(archived)).contains(filter) || updateNeeded(childKey)) {
			beobachtungen = getBeobachtungen(childKey, filter, oldestEntry);
			getCache(getCacheName(archived)).put(filter, beobachtungen);
		} else {
			beobachtungen = (List<GwtBeobachtung>) getCache(getCacheName(archived)).get(filter);
		}

		return beobachtungen;
	}

	private Collection<String> getChildKeys(BeobachtungsFilter filter) {

		List<String> childKeys = new ArrayList<String>();

		if (filter.isOver12() && filter.isUnder12()) {
			Collection<GwtChild> allChildren = childDao.getAllChildren();
			for (GwtChild child : allChildren) {
				childKeys.add(child.getKey());
			}
		} else if (filter.isOver12()) {
			Collection<GwtChild> allChildren = childDao.getAllChildrenOver12();
			for (GwtChild child : allChildren) {
				childKeys.add(child.getKey());
			}
		} else if (filter.isUnder12()) {
			Collection<GwtChild> allChildren = childDao.getAllChildrenUnder12();
			for (GwtChild child : allChildren) {
				childKeys.add(child.getKey());
			}
		} else if (filter.getChildKey() != null) {
			childKeys.add(filter.getChildKey());
		} else {
			throw new IllegalArgumentException("no child filter defined");
		}

		return childKeys;
	}

	private List<GwtBeobachtung> getBeobachtungen(String childKey,
			BeobachtungsFilter filter, Date oldestEntry) {
		GwtChild child = childDao.getChild(childKey);

		Multimap<String, GwtBeobachtung> allBeobachtungen = getAllGwtBeobachtungen(
				childKey, oldestEntry, filter.isArchived());

		Predicate<Entry<String, GwtBeobachtung>> mapFilter = createFilter(
				child, filter);

		Multimap<String, GwtBeobachtung> filteredBeobachtungen = Multimaps
				.filterEntries(allBeobachtungen, mapFilter);
		List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();

		if (filter.isShowSummaries()) {
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
		Collection<GwtBeobachtung> values = filteredBeobachtungen.values();
		result.addAll(getBeobachtungenForDisplay(values,
				filter.isShowEmptyEntries()));
		return result;
	}

	private HashSet<GwtBeobachtung> getBeobachtungenForDisplay(
			Collection<GwtBeobachtung> beobachtungen, boolean showEmptyEntries) {
		HashSet<GwtBeobachtung> result = new HashSet<GwtBeobachtung>();
		if (showEmptyEntries) {
			result.addAll(beobachtungen);
		} else {
			for (GwtBeobachtung beobachtung : beobachtungen) {
				String text = beobachtung.getText();
				if (text == null || text.isEmpty() || text.equals("<br>")) {
					continue;
				}
				result.add(beobachtung);
			}
		}
		return result;
	}

	private GwtBeobachtung createSummary(
			Collection<GwtBeobachtung> beobachtungen, String sectionKey) {

		ResourceBundle bundle = ResourceBundle.getBundle("messages");

		List<GwtBeobachtung> tmpList = getSortedList(beobachtungen);
		GwtBeobachtung lastBeobachtung = tmpList.get(0);
		GwtBeobachtung firstBeobachtung = tmpList.get(tmpList.size() - 1);
		String sectionName = sectionDao.getSectionName(sectionKey);
		String childName = firstBeobachtung.getChildName();
		Date startDate = firstBeobachtung.getDate();
		Date endDate = lastBeobachtung.getDate();
		GwtSummary summary = new GwtSummary();
		summary.setChildKey(firstBeobachtung.getChildKey());
		summary.setSectionKey(sectionKey);
		summary.setChildName(childName);
		summary.setSectionName(sectionName);
		summary.setDate(new Date());
		MessageFormat messageFormat = new MessageFormat(
				bundle.getString("summaryTemplate"), Locale.GERMAN);
		summary.setText(messageFormat.format(new Object[] { sectionName,
				tmpList.size(), startDate, endDate }));
		summary.setUser(bundle.getString("summaryUser"));
		summary.setCount(tmpList.size());
		return summary;
	}

	private List<GwtBeobachtung> getSortedList(
			Collection<GwtBeobachtung> beobachtungen) {
		List<GwtBeobachtung> tmpList = new ArrayList<GwtBeobachtung>(
				beobachtungen);
		Collections.sort(tmpList, new Comparator<GwtBeobachtung>() {

			@Override
			public int compare(GwtBeobachtung o1, GwtBeobachtung o2) {
				return o2.getDate().compareTo(o1.getDate());
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
			final GwtChild child, final BeobachtungsFilter filter) {

		return new Predicate<Map.Entry<String, GwtBeobachtung>>() {

			@Override
			public boolean apply(Entry<String, GwtBeobachtung> entry) {
				String sectionKey = entry.getKey();
				GwtBeobachtung beobachtung = entry.getValue();

				final String user = filter.getUser();
				if (user != null
						&& !beobachtung.getUser().equalsIgnoreCase(user)) {
					return false;
				}

				if (filter.getSectionKey() != null
						&& !filter.getSectionKey().equals(sectionKey)) {
					return false;
				}

				Date lastDevelopementDialogueDate = child
						.getLastDevelopementDialogueDate();

				Date[] timeRange = filter.getTimeRange();
				if (timeRange != null && timeRange.length == 2) {

					Date startDate = timeRange[0];
					Date endDate = timeRange[1];
					if (filter.isSinceLastDevelopmementDialogue()
							&& lastDevelopementDialogueDate != null) {
						if (endDate.before(lastDevelopementDialogueDate)) {
							return false;
						}
						startDate = last(lastDevelopementDialogueDate,
								startDate);
					}

					if (beobachtung.getDate().before(startDate)) {
						return false;
					}
					if (beobachtung.getDate().after(endDate)) {
						return false;
					}
				} else if (filter.isSinceLastDevelopmementDialogue()
						&& lastDevelopementDialogueDate != null) {
					if (beobachtung.getDate().before(
							lastDevelopementDialogueDate)) {
						return false;
					}
				}

				return true;
			}

			private Date last(Date date1, Date date2) {
				return date1.before(date2) ? date2 : date1;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private Multimap<String, GwtBeobachtung> getAllGwtBeobachtungen(
			String childKey, Date oldestEntry, boolean archived) {
		MemcacheService cache = getCache(getCacheName(archived));
		Multimap<String, GwtBeobachtung> result = null;

		String key = createKey(childKey, oldestEntry);
		if (!updateNeeded(childKey)) {
			if (cache.contains(key)) {
				result = (Multimap<String, GwtBeobachtung>) cache.get(key);
			}
			if (result == null && oldestEntry != null
					&& cache.contains(childKey)) {
				// if we look for a limited range and there is none, look also
				// for a not limited
				result = (Multimap<String, GwtBeobachtung>) cache.get(childKey);
			}
		}
		if (result == null) {
			synchronized (this) {

				setUpdated(childKey);
				result = getAllBeobachtungen(childKey, oldestEntry, archived);
				cache.put(key, result);

			}
		}
		return result;
	}

	private String createKey(String childKey, Date oldestEntry) {
		return childKey + (oldestEntry == null ? "" : oldestEntry.getTime());
	}

	private Multimap<String, GwtBeobachtung> getAllBeobachtungen(
			String childKey, Date oldestEntry, boolean archived) {

		Multimap<String, GwtBeobachtung> sectionToBeobachtung = ArrayListMultimap
				.<String, GwtBeobachtung> create(EXPECTED_SECTION_PER_CHILD,
						EXPECTED_BEOBACHTUNG_PER_SECTION);

		for (Entity beobachtung : queryAllBeobachtungen(childKey, oldestEntry, archived)) {

			GwtBeobachtung gwtBeobachtung = toGwt(beobachtung);
			gwtBeobachtung.setArchived(archived);
			String sectionKey = gwtBeobachtung.getSectionKey();
			GwtSection section = sectionDao.getSection(sectionKey);
			if (section == null) {
				throw new IllegalArgumentException("unknown section with key "
						+ sectionKey);
			}
			sectionToBeobachtung.put(sectionKey, gwtBeobachtung);

			setParentSections(section, gwtBeobachtung, sectionToBeobachtung);
		}
		return sectionToBeobachtung;
	}

	private Iterable<Entity> queryAllBeobachtungen(String childKey,
			Date oldestEntry, boolean archived) {
		Query query = new Query( getBeobachtungKind(archived), toKey(childKey));
		if (oldestEntry != null) {
			Filter filter = new FilterPredicate(DATE_FIELD,
					FilterOperator.GREATER_THAN, oldestEntry);
			query.setFilter(filter);
		}
		return getDatastoreService().prepare(query).asIterable();
	}

	private Date shortListLimit() {
		return getMidnightIn(Calendar.DAY_OF_MONTH, -21);
	}

	private Date today() {
		return getMidnightIn(Calendar.DAY_OF_MONTH, 1);
	}

	private Date getMidnightIn(int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	private void setParentSections(GwtSection section,
			GwtBeobachtung gwtBeobachtung,
			Multimap<String, GwtBeobachtung> sectionToBeobachtung) {
		String parentKey = section.getParentKey();
		if (parentKey != null) {
			GwtSection parentSection = sectionDao.getSection(parentKey);
			// do not summarize in the top level
			if (parentSection.getParentKey() != null) {
				sectionToBeobachtung.put(parentKey, gwtBeobachtung);
				setParentSections(parentSection, gwtBeobachtung,
						sectionToBeobachtung);
			}
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

	public int getRowCount(BeobachtungsFilter filter) {
		return getBeobachtungen(filter).size();
	}

	public GwtBeobachtung getBeobachtung(String beobachtungsKey, boolean archived) {
		return toGwt(getCachedEntity(toKey(beobachtungsKey), getCacheName(archived)));
	}

	public boolean beobachtungenExist(Collection<String> sectionKeys,
			DatastoreService ds, boolean archived) {
		if (sectionKeys == null || sectionKeys.isEmpty()) {
			return false;
		}
		Filter sectionFilter = createSectionFilter(sectionKeys);
		Query query = new Query(getBeobachtungKind(archived)).setFilter(sectionFilter);
		return count(query, withDefaults(), ds) > 0;
	}

	public synchronized void storeBeobachtung(GwtBeobachtung gwtBeobachtung,
			User user, String masterBeobachtungsKey) {

		Entity beobachtung = toEntity(gwtBeobachtung, user, false);
		getDatastoreService().put(beobachtung);
		insertIntoCache(beobachtung, getCacheName(false));
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

		DatastoreService ds = getDatastoreService();
		Query query = new Query(getBeobachtungKind(false), toKey(childKey))
				.setKeysOnly();
		Transaction transaction = ds.beginTransaction();
		try {
			Iterable<Entity> allBeobachtungen = ds.prepare(query).asIterable();
			for (Entity beobachtung : allBeobachtungen) {
				deleteEntity(beobachtung.getKey(), ds, getCacheName(false));
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

		GwtBeobachtung beobachtung = getBeobachtung(beobachtungsKey, false);
		deleteEntity(toKey(beobachtungsKey), getCacheName(false));
		setUpdateNeeded(beobachtung.getChildKey());
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
		String childName = DaoRegistry.get(ChildDsDao.class).getChildName(
				childKey);
		String sectionName = DaoRegistry.get(SectionDsDao.class)
				.getSectionName(sectionKey);

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

	private Entity toEntity(GwtBeobachtung gwtBeobachtung, User user, boolean archived) {
		String key = gwtBeobachtung.getKey();
		Entity entity;
		if (key == null) {
			entity = new Entity(getBeobachtungKind(archived),
					toKey(gwtBeobachtung.getChildKey()));
		} else {
			entity = new Entity(toKey(key));
		}
		entity.setProperty(SECTION_KEY_FIELD,
				toKey(gwtBeobachtung.getSectionKey()));
		entity.setProperty(DATE_FIELD, gwtBeobachtung.getDate());
		String text = gwtBeobachtung.getText();
		entity.setProperty(TEXT_FIELD, new Text(text));
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

	public synchronized int moveAllToArchiveBefore(Date endDate) {

		Map<Key, Key> oldToNewBeobachtungen = copyAllNoticesToArchive(endDate);
		moveAllNoticeGroupsToArchive(oldToNewBeobachtungen);
		deleteAllNotices(oldToNewBeobachtungen.keySet());
		return oldToNewBeobachtungen.size();
	}

	private Map<Key, Key> copyAllNoticesToArchive(Date endDate) {

		Map<Key, Key> oldToNew = new HashMap<Key, Key>();
		Collection<GwtChild> children = childDao.getAllChildren();
		for (GwtChild child : children) {

			Map<Key, Key> tmpOldToNew = copyNoticesToArchive(child, endDate);
			oldToNew.putAll(tmpOldToNew);
		}

		return oldToNew;
	}

	private void moveAllNoticeGroupsToArchive(Map<Key, Key> oldToNew) {

		Set<Key> keys = new TreeSet<Key>(oldToNew.keySet());
		Iterator<Key> iterator = keys.iterator();
		while (iterator.hasNext()) {

			Collection<Key> relatedKeys = moveNoticeGroupsToArchive(
					iterator.next(), oldToNew);
			if (relatedKeys.isEmpty()) {
				continue;
			}

			iterator.remove();
			keys.removeAll(relatedKeys);
			iterator = keys.iterator();
		}

	}

	private void deleteAllNotices(Set<Key> keySet) {
		
		DatastoreService ds = getDatastoreService();
		Transaction transaction = ds.beginTransaction(TransactionOptions.Builder.withXG(true));
		Iterator<Key> iterator = keySet.iterator();
		try {
			
			int count = 0;
			while (iterator.hasNext()) {
				
				count++;
				deleteEntity(iterator.next(), ds, getCacheName(false));

				if (count == 100) {
					transaction.commit();
					ds = getDatastoreService();
					transaction = ds.beginTransaction();
					count = 0;
				}
			}
			
			if (transaction.isActive()) {
				transaction.commit();
			}
			
		} finally {
			if ( transaction.isActive()) {
				System.err.println("error deleting notices");
				transaction.rollback();
			}
		}
	}

	private Map<Key, Key> copyNoticesToArchive(GwtChild child, Date endDate) {

		Map<Key, Key> tmpOldToNew = new HashMap<Key, Key>();

		DatastoreService ds = getDatastoreService();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {

			String childKey = child.getKey();
			Iterable<Entity> beobachtungen = getAllBeobachtungenBefore(endDate,
					childKey, ds);
			int copied = 0;
			for (Entity beobachtung : beobachtungen) {

				Entity archived = copyToArchive(beobachtung, ds);
				tmpOldToNew.put(beobachtung.getKey(), archived.getKey());

				copied++;
			}

			if (copied > 0) {
				setUpdateNeeded(childKey);
				System.err.println("copying " + copied + " beobachtungen of "
						+ child.getFirstName());
				transaction.commit();
			} 
			
			System.err.println("copied " + copied + " beobachtungen of "
					+ child.getFirstName());

		} finally {
			if (transaction.isActive()) {
				System.err
						.println("got exception while copying beobachtungen, rolling back");
				transaction.rollback();
			}
		}

		return tmpOldToNew;
	}

	private Collection<Key> moveNoticeGroupsToArchive(Key oldKey,
			Map<Key, Key> oldToNew) {

		DatastoreService ds = getDatastoreService();
		List<Entity> groups = findGroups(oldKey, ds);
		if (groups.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Key> relatedKeys = new ArrayList<Key>();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {

			for (Entity group : groups) {

				Key relatedKey = moveToArchive(group, oldToNew, ds);
				relatedKeys.add(relatedKey);
			}

			transaction.commit();

		} finally {
			if (transaction.isActive()) {
				System.err
						.println("got exception while moving beobachtungen, rolling back");
				transaction.rollback();
			}
		}

		return relatedKeys;
	}

	private Key moveToArchive(Entity group, Map<Key, Key> oldToNew,
			DatastoreService ds) {

		copyToArchive(group, oldToNew, ds);
		Key relatedKey = getRelatedKey(group);
		ds.delete(group.getKey());
		return relatedKey;
	}

	private Key getRelatedKey(Entity oldGroup) {
		return (Key) oldGroup.getProperty(BEOBACHTUNGS_KEY_FIELD);
	}

	private void copyToArchive(Entity oldGroup, Map<Key, Key> oldToNew,
			DatastoreService ds) {

		Key relatedKey = getRelatedKey(oldGroup);
		Key newRelatedKey = oldToNew.get(relatedKey);
		Key newKey = oldToNew.get(oldGroup.getKey().getParent());

		Entity newGroup = new Entity(BEOBACHTUNG_GROUP_ARCHIVE_KIND, newKey);
		newGroup.setProperty(BEOBACHTUNGS_KEY_FIELD, newRelatedKey);
		ds.put(newGroup);
	}

	private List<Entity> findGroups(Key beobachtungsKey, DatastoreService ds) {
		Query query = new Query(BEOBACHTUNGS_GROUP_KIND, beobachtungsKey);
		return ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
	}

	private Entity copyToArchive(Entity beobachtung, DatastoreService ds) {

		final Entity archived = new Entity(BEOBACHTUNG_ARCHIVE_KIND,
				beobachtung.getParent());
		archived.setPropertiesFrom(beobachtung);
		ds.put(archived);
		return archived;
	}

	private Iterable<Entity> getAllBeobachtungenBefore(Date endDate,
			String childKey, DatastoreService ds) {
		Query query = new Query(getBeobachtungKind(false), toKey(childKey))
				.setFilter(new FilterPredicate(DATE_FIELD,
						FilterOperator.LESS_THAN, endDate));
		return ds.prepare(query).asIterable();
	}

	public static String getCacheName(boolean archived) {
		return archived ? BEOBACHTUNGS_ARCHIVE_DAO_MEMCACHE : BEOBACHTUNGS_DAO_MEMCACHE;
	}

	public static String getBeobachtungKind(boolean archived) {
		return archived ? BEOBACHTUNG_ARCHIVE_KIND : BEOBACHTUNG_KIND;
	}

	public boolean beobachtungenExist(Collection<String> sectionKeys,
			DatastoreService datastoreService) {
		return beobachtungenExist(sectionKeys, datastoreService, false) || beobachtungenExist(sectionKeys, datastoreService, true);
	}

}
