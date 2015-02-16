package at.brandl.lws.notice.service.dao.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.service.dao.DaoRegistry;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

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

	private final Map<String, Date> dirty = new ConcurrentHashMap<String, Date>();

	private ChildDsDao childDao = DaoRegistry.get(ChildDsDao.class);

	private void setUpdateNeeded(String childKey) {
		dirty.put(childKey, new Date());
	}

	public synchronized int moveAllToArchiveBefore(Date endDate) {

		System.err.println("moving beobachtungen to archive");
		Map<Key, Key> oldToNewBeobachtungen = copyAllNoticesToArchive(endDate);
		System.err.println("moving beobachtungsgroups to archive");
		moveAllNoticeGroupsToArchive(oldToNewBeobachtungen);
		System.err.println("deleting beobachtungen");
		deleteAllNotices(oldToNewBeobachtungen.keySet());
		System.err.println("ready moving beobachtungen");
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
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
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
			if (transaction.isActive()) {
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
		return archived ? BEOBACHTUNGS_ARCHIVE_DAO_MEMCACHE
				: BEOBACHTUNGS_DAO_MEMCACHE;
	}

	public static String getBeobachtungKind(boolean archived) {
		return archived ? BEOBACHTUNG_ARCHIVE_KIND : BEOBACHTUNG_KIND;
	}

}
