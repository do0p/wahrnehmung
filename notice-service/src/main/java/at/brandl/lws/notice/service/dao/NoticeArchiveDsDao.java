package at.brandl.lws.notice.service.dao;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import at.brandl.lws.notice.shared.util.Constants.ArchiveNotice;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNoticeGroup;
import at.brandl.lws.notice.shared.util.Constants.Notice;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
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
			tmpCount++;
			Iterable<Entity> groups = execute(new Query(NoticeGroup.KIND,
					parentNoticeKey));
			for (Entity group : groups) {
				if (newParentNoticeKey != null) {
					Key noticeKey = (Key) group
							.getProperty(NoticeGroup.BEOBACHTUNG);
					Key newNoticeKey = moveNoticeToArchive(noticeKey, ds);
					if (newNoticeKey != null) {
						copyGroupToArchive(group, newParentNoticeKey,
								newNoticeKey, ds);
						tmpCount++;
					}

				}
				ds.delete(group.getKey());
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

	private Key moveNoticeToArchive(Key noticeKey, DatastoreService ds) {

		System.err
				.println("Moving notice " + KeyFactory.keyToString(noticeKey));
		Entity notice = getNotice(noticeKey);
		if (notice == null) {
			return null;
		}

		final Entity archived = new Entity(ArchiveNotice.KIND,
				noticeKey.getParent());
		archived.setPropertiesFrom(notice);
		ds.put(archived);
		ds.delete(noticeKey);
		return archived.getKey();
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

}
