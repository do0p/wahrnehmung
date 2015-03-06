package at.brandl.lws.notice.service.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class NoticeArchiveDsDao {

	public Set<Key> getAllNoticeKeysBefore(Date endDate) {

		System.err.println("Fetching all notices from Datastore");
		Set<Key> result = new HashSet<Key>();
		Query query = new Query(Notice.KIND).setFilter(
				new FilterPredicate(Notice.DATE, FilterOperator.LESS_THAN,
						endDate)).setKeysOnly();

		for (Entity notice : execute(query)) {
			result.add(notice.getKey());
		}
		return result;
	}

	public Set<Key> getAllGroupParentKeys(Set<Key> keySet) {

		System.err.println("Fetching all groups from Datastore");
		Query query = new Query(NoticeGroup.KIND).setKeysOnly();
		Iterable<Entity> groups = execute(query);
		return filterKeys(groups, keySet);
	}

	public boolean moveNoticeToArchive(Key noticeKey) {

		boolean success = false;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
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
		System.err.println("Moving group of notice "
				+ KeyFactory.keyToString(parentNoticeKey));
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {
			Key newParentNoticeKey = moveNoticeToArchive(parentNoticeKey, ds);
			if (newParentNoticeKey == null) {
				newParentNoticeKey = getFromKeyMapping(parentNoticeKey,
						KeyMappingType.ARCHIVE_NOTICE, ds);
			} else {
				tmpCount++;

			}
			if (newParentNoticeKey == null) {
				throw new IllegalStateException("no notice found for "
						+ KeyFactory.keyToString(parentNoticeKey));
			}
			Iterable<Entity> groups = execute(new Query(NoticeGroup.KIND,
					parentNoticeKey));
			for (Entity group : groups) {
				Key noticeKey = (Key) group
						.getProperty(NoticeGroup.BEOBACHTUNG);
				Key newNoticeKey = moveNoticeToArchive(noticeKey, ds);
				if (newNoticeKey == null) {
					newNoticeKey = getFromKeyMapping(noticeKey,
							KeyMappingType.ARCHIVE_NOTICE, ds);
				} else {
					tmpCount++;
				}
				if (newNoticeKey == null) {
					throw new IllegalStateException("no notice found for "
							+ KeyFactory.keyToString(noticeKey));
				}
				copyGroupToArchive(group, newParentNoticeKey, newNoticeKey, ds);

			}
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				System.err.println("Error moving notice group "
						+ KeyFactory.keyToString(parentNoticeKey));
				transaction.rollback();
			}
		}
		return tmpCount;
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
		Filter keyFilter = new FilterPredicate(MigrationKeyMapping.OLD_KEY,
				FilterOperator.EQUAL, oldKey);
		Filter filter = new CompositeFilter(CompositeFilterOperator.AND,
				Arrays.asList(typeFilter, keyFilter));
		return new Query(MigrationKeyMapping.KIND).setFilter(filter);
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

		System.err.println("Moving group "
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

	private Iterable<Entity> execute(Query query) {

		return DatastoreServiceFactory.getDatastoreService().prepare(query)
				.asIterable();
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

		Entity mapping = new Entity(MigrationKeyMapping.KIND);
		mapping.setProperty(MigrationKeyMapping.TYPE, type.toString());
		mapping.setProperty(MigrationKeyMapping.OLD_KEY, oldKey);
		mapping.setProperty(MigrationKeyMapping.NEW_KEY, newKey);

		return mapping;
	}

}
