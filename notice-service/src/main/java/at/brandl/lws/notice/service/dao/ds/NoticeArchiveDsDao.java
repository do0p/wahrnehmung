package at.brandl.lws.notice.service.dao.ds;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import at.brandl.lws.notice.shared.util.Constants.ArchiveNotice;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNoticeGroup;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.Notice;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class NoticeArchiveDsDao  {

	public synchronized int moveAllToArchiveBefore(Date endDate) {
		int count = 0;
		System.err.println("Fetching all notices from Datastore");
		Map<Key, Entity> allBeobachtungen = getAllBeobachtungenBefore(endDate);

		System.err.println("Fetching all groups from Datastore");
		Map<Key, Iterable<Entity>> allGroups = getAllGroups(allBeobachtungen
				.keySet());

		System.err.println("Moving groups to Archive");
		for (Entry<Key, Iterable<Entity>> entry : allGroups.entrySet()) {

			count += moveGroupsToArchive(entry.getKey(), entry.getValue(),
					allBeobachtungen);
		}
		System.err.println("Moved " + count + " notices in groups");

		System.err.println("Moving remaining notices to Archive");
		for (Entity notice : allBeobachtungen.values()) {
			if (moveNoticeToArchive(notice)) {
				count++;
			}
		}
		System.err.println("Moved " + count + " notices");

		return count;
	}

	private boolean moveNoticeToArchive(Entity notice) {
		boolean success = false;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {
			moveNoticeToArchive(notice, ds);
			transaction.commit();
			success = true;
		} finally {
			if (transaction.isActive()) {
				System.err.println("Error moving notice "
						+ KeyFactory.keyToString(notice.getKey()));
				transaction.rollback();
			}
		}
		return success;
	}

	private int moveGroupsToArchive(Key parentNoticeKey,
			Iterable<Entity> groups, Map<Key, Entity> allBeobachtungen) {
		int tmpCount = 0;
		System.err.println("Moving group of notice "
				+ KeyFactory.keyToString(parentNoticeKey));
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = ds
				.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {
			Key newParentNoticeKey = moveNoticeToArchive(
					allBeobachtungen.remove(parentNoticeKey), ds);
			tmpCount++;

			for (Entity group : groups) {

				Key noticeKey = (Key) group.getProperty(NoticeGroup.BEOBACHTUNG);
				Key newNoticeKey = moveNoticeToArchive(
						allBeobachtungen.remove(noticeKey), ds);
				moveGroupToArchive(group, newParentNoticeKey, newNoticeKey, ds);
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
		return tmpCount;
	}

	private Key moveNoticeToArchive(Entity beobachtung, DatastoreService ds) {
		System.err.println("Moving notice "
				+ KeyFactory.keyToString(beobachtung.getKey()));
		final Entity archived = new Entity(ArchiveNotice.KIND,
				beobachtung.getParent());
		archived.setPropertiesFrom(beobachtung);
		ds.put(archived);
		ds.delete(beobachtung.getKey());
		return archived.getKey();
	}

	private void moveGroupToArchive(Entity group, Key newNoticeParentKey,
			Key newNoticeKey, DatastoreService ds) {
		System.err.println("Moving group "
				+ KeyFactory.keyToString(group.getKey()));
		Entity newGroup = new Entity(ArchiveNoticeGroup.KIND,
				newNoticeParentKey);
		newGroup.setProperty(NoticeGroup.BEOBACHTUNG, newNoticeKey);
		ds.put(newGroup);
		ds.delete(group.getKey());
	}

	private Map<Key, Iterable<Entity>> getAllGroups(Collection<Key> noticeKeys) {
		Map<Key, Iterable<Entity>> result = new HashMap<Key, Iterable<Entity>>();
		for (Key noticeKey : noticeKeys) {
			Query query = new Query(NoticeGroup.KIND, noticeKey);
			Iterable<Entity> groups = execute(query);
			if (groups.iterator().hasNext()) {
				result.put(noticeKey, groups);
			}
		}
		return result;
	}

	private Map<Key, Entity> getAllBeobachtungenBefore(Date endDate) {

		Map<Key, Entity> result = new HashMap<Key, Entity>();

		for (Entity child : execute(new Query(Child.KIND))) {
			result.putAll(getAllBeobachtungenBefore(endDate, child.getKey()));
		}

		return result;
	}

	private Map<? extends Key, ? extends Entity> getAllBeobachtungenBefore(
			Date endDate, Key childKey) {
		Map<Key, Entity> result = new HashMap<Key, Entity>();
		Query query = new Query(Notice.KIND, childKey)
				.setFilter(new FilterPredicate(Notice.DATE,
						FilterOperator.LESS_THAN, endDate));

		for (Entity notice : execute(query)) {
			result.put(notice.getKey(), notice);
		}
		return result;
	}

	private Iterable<Entity> execute(Query query) {
		return DatastoreServiceFactory.getDatastoreService().prepare(query).asIterable();
	}

}
