package at.brandl.lws.notice.service.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.brandl.lws.notice.shared.util.Constants.ArchiveNotice;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNoticeGroup;
import at.brandl.lws.notice.shared.util.Constants.MigrationKeyMapping;
import at.brandl.lws.notice.shared.util.Constants.MigrationKeyMapping.KeyMappingType;
import at.brandl.lws.notice.shared.util.Constants.Notice;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class NoticeArchiveDsDao {

	private static class EntityComparator implements Comparator<Entity> {

		@Override
		public int compare(Entity o1, Entity o2) {

			// text
			int result = compare(o1, o2, Notice.TEXT);
			// date
			if (result == 0) {
				result = compare(o1, o2, Notice.DATE);
			}
			// section
			if (result == 0) {
				result = compare(o1, o2, Notice.SECTION);
			}
			// duration
			if (result == 0) {
				result = compare(o1, o2, Notice.DURATION);
			}
			// social
			if (result == 0) {
				result = compare(o1, o2, Notice.SOCIAL);
			}
			return result;
		}

		private int compare(Entity o1, Entity o2, String propKey) {
			Comparable prop1;
			Object prop2;
			if (o1.getProperty(propKey) instanceof Text) {
				prop1 = ((Text) o1.getProperty(propKey)).getValue();
				prop2 = ((Text) o2.getProperty(propKey)).getValue();
			} else {
				prop1 = (Comparable) o1.getProperty(propKey);
				prop2 = o2.getProperty(propKey);
			}
			return compare(prop1, prop2);
		}

		private int compare(Comparable obj1, Object obj2) {
			int result = 0;
			if (obj1 == null) {
				if (obj2 == null) {
					result = 0;
				}
				result = 1;
			} else if (obj2 == null) {
				result = -1;
			} else {
				result = obj1.compareTo(obj2);
			}
			return result;
		}
	}

	public Set<Key> getAllNoticeKeysBefore(Date endDate) {

		System.out.println("Fetching all notices from Datastore");
		Set<Key> result = new HashSet<Key>();
		Query query = new Query(Notice.KIND).setFilter(
				new FilterPredicate(Notice.DATE, FilterOperator.LESS_THAN,
						endDate)).setKeysOnly();

		for (Entity notice : execute(query, 100)) {
			result.add(notice.getKey());
		}
		return result;
	}

	public Set<Key> getAllGroupParentKeys(Set<Key> keySet) {

		System.out.println("Fetching all groups from Datastore");
		Query query = new Query(NoticeGroup.KIND).setKeysOnly();
		Iterable<Entity> groups = execute(query, 100);
		return filterKeys(groups, keySet);
	}

	public boolean moveNoticeToArchive(Key noticeKey) {

		boolean success = false;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds.beginTransaction();
		try {
			Key newKey = moveNoticeToArchive(noticeKey, ds);
			if (newKey != null) {
				transaction.commit();
			}
			success = true;
		} finally {
			if (transaction.isActive()) {
				System.err.println("Error moving notice "
						+ KeyFactory.keyToString(noticeKey));
				transaction.rollback();
			}
		}
		return success;
	}

	public int moveGroupsToArchive(Key parentNoticeKey) {

		int tmpCount = 0;
		System.out.println("Moving group of notice "
				+ KeyFactory.keyToString(parentNoticeKey));
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds.beginTransaction();
		List<Key> failure = new ArrayList<Key>();
		try {
			Key newParentNoticeKey = getFromKeyMapping(parentNoticeKey,
					KeyMappingType.ARCHIVE_NOTICE, ds);
			if (newParentNoticeKey == null) {
				throw new IllegalStateException("no notice found for "
						+ KeyFactory.keyToString(parentNoticeKey));
			}
			Iterable<Entity> groups = execute(new Query(NoticeGroup.KIND,
					parentNoticeKey), 25);
			for (Entity group : groups) {
				Key noticeKey = (Key) group
						.getProperty(NoticeGroup.BEOBACHTUNG);
				Key newNoticeKey = getFromKeyMapping(noticeKey,
						KeyMappingType.ARCHIVE_NOTICE, ds);

				if (newNoticeKey == null) {
					failure.add(noticeKey);
					continue;
				}
				copyGroupToArchive(group, newParentNoticeKey, newNoticeKey, ds);
				tmpCount++;

			}
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				System.err.println("Error moving notice group "
						+ KeyFactory.keyToString(parentNoticeKey));
				transaction.rollback();
			}
		}
		if (!failure.isEmpty()) {
			throw new IllegalStateException("no notice found for "
					+ join(failure));
		}
		System.out.println("Moved " + tmpCount + " groups");
		return tmpCount;
	}

	private String join(List<Key> failure) {
		StringBuilder result = new StringBuilder();
		Iterator<Key> iterator = failure.iterator();

		boolean hasNext = iterator.hasNext();
		while (hasNext) {
			result.append(KeyFactory.keyToString(iterator.next()));
			hasNext = iterator.hasNext();
			if (hasNext) {
				result.append(", ");
			}
		}
		return result.toString();
	}

	private Key getFromKeyMapping(Key oldKey, KeyMappingType type,
			DatastoreService ds) {

		Iterable<Entity> result = ds.prepare(createQuery(oldKey, type))
				.asIterable();
		Iterator<Entity> iterator = result.iterator();
		if (iterator.hasNext()) {

			Entity mapping = iterator.next();
			return (Key) mapping.getProperty(MigrationKeyMapping.NEW_KEY);
		}
		return null;
	}

	Query createQuery(Key oldKey, KeyMappingType type) {

		Filter typeFilter = new FilterPredicate(MigrationKeyMapping.TYPE,
				FilterOperator.EQUAL, type.toString());
		return new Query(MigrationKeyMapping.KIND, oldKey)
				.setFilter(typeFilter);
	}

	Key moveNoticeToArchive(Key noticeKey, DatastoreService ds) {

		System.out
				.println("Moving notice " + KeyFactory.keyToString(noticeKey));
		Entity notice = getNotice(noticeKey);
		if (notice == null) {
			return null;
		}

		Entity archived = new Entity(ArchiveNotice.KIND, noticeKey.getParent());
		archived.setPropertiesFrom(notice);
		ds.put(archived);

		Key newKey = archived.getKey();
		Entity keyMapping = createKeyMapping(noticeKey, newKey,
				KeyMappingType.ARCHIVE_NOTICE);
		ds.put(keyMapping);

		ds.delete(noticeKey);

		return newKey;
	}

	private Entity getNotice(Key noticeKey) {

		try {
			return DatastoreServiceFactory.getDatastoreService().get(noticeKey);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	private void copyGroupToArchive(Entity group, Key newNoticeParentKey,
			Key newNoticeKey, DatastoreService ds) {

		System.out.println("Moving group "
				+ KeyFactory.keyToString(group.getKey()));
		Entity newGroup = new Entity(ArchiveNoticeGroup.KIND,
				newNoticeParentKey);
		newGroup.setProperty(NoticeGroup.BEOBACHTUNG, newNoticeKey);
		ds.put(newGroup);

		Entity keyMapping = createKeyMapping(group.getKey(), newGroup.getKey(),
				KeyMappingType.ARCHIVE_GROUP);
		ds.put(keyMapping);

		ds.delete(group.getKey());
	}

	private Iterable<Entity> execute(Query query, int chunkSize) {

		return DatastoreServiceFactory.getDatastoreService().prepare(query)
				.asIterable(FetchOptions.Builder.withChunkSize(chunkSize));
	}

	private Set<Key> filterKeys(Iterable<Entity> groups, Set<Key> keySet) {

		Set<Key> keys = new HashSet<Key>();
		for (Entity group : groups) {
			Key key = group.getKey().getParent();
			if (keySet.contains(key)) {
				keys.add(key);
			}
		}
		return keys;
	}

	private Entity createKeyMapping(Key oldKey, Key newKey, KeyMappingType type) {

		Entity mapping = new Entity(MigrationKeyMapping.KIND, oldKey);
		mapping.setProperty(MigrationKeyMapping.TYPE, type.toString());
		mapping.setProperty(MigrationKeyMapping.NEW_KEY, newKey);

		return mapping;
	}

	public Iterable<Entity> getAllNoticesSorted(Key childKey, boolean archived) {

		String kind = archived ? ArchiveNotice.KIND : Notice.KIND;
		Iterable<Entity> notices = execute(new Query(kind, childKey), 100);

		List<Entity> sortedNotices = new ArrayList<Entity>();
		for (Entity notice : notices) {
			sortedNotices.add(notice);
		}
		Collections.sort(sortedNotices, new EntityComparator());

		return sortedNotices;
	}

	public Set<Key> getAllGroupedKeys(boolean archived) {
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		String cacheKey = archived ? "archivedgroupkeys" : "groupkeys";
		Set<Key> allKeys = (Set<Key>) cache.get(cacheKey);
		if (allKeys == null) {
			synchronized (this) {
				allKeys = (Set<Key>) cache.get(cacheKey);
				if (allKeys == null) {
					allKeys = getAllGroupKeysFromDs(archived);
					cache.put(cacheKey, allKeys);
				}
			}
		}
		return allKeys;
	}

	private Set<Key> getAllGroupKeysFromDs(boolean archived) {
		Set<Key> allKeys = new HashSet<Key>();
		String kind = archived ? ArchiveNoticeGroup.KIND : NoticeGroup.KIND;
		Iterable<Entity> allGroups = execute(new Query(kind), 100);
		for (Entity group : allGroups) {
			allKeys.add(group.getParent());
			allKeys.add((Key) group.getProperty(NoticeGroup.BEOBACHTUNG));
		}
		return allKeys;
	}

	public void deleteNotice(Key key) {

		DatastoreServiceFactory.getDatastoreService().delete(key);
	}

}
