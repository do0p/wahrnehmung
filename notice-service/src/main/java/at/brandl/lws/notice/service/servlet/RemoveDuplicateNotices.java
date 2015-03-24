package at.brandl.lws.notice.service.servlet;

import static com.google.appengine.api.datastore.KeyFactory.keyToString;
import static com.google.appengine.api.datastore.KeyFactory.stringToKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum;
import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;
import at.brandl.lws.notice.shared.util.Constants.Notice;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

public class RemoveDuplicateNotices extends HttpServlet {

	static class EntityComparator implements Comparator<Entity> {

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

		private <T extends Comparable<T>> int compare(Entity o1, Entity o2,
				String propKey) {

			T prop1;
			T prop2;
			if (o1.getProperty(propKey) instanceof Text) {
				prop1 = (T) ((Text) o1.getProperty(propKey)).getValue();
			} else {
				prop1 = (T) o1.getProperty(propKey);
			}

			if (o2.getProperty(propKey) instanceof Text) {

				prop2 = (T) ((Text) o2.getProperty(propKey)).getValue();
			} else {
				prop2 = (T) o2.getProperty(propKey);
			}

			return compare(prop1, prop2);
		}

		private <T> int compare(Comparable<T> obj1, T obj2) {
			int result = 0;
			if (obj1 == null) {
				if (obj2 == null) {
					result = 0;
				} else {
					result = 1;
				}
			} else if (obj2 == null) {
				result = -1;
			} else {
				result = obj1.compareTo(obj2);
			}
			return result;
		}
	}

	private static final long serialVersionUID = -7318489147891141902L;
	private Collection<Key> allGroupedKeys;
	private NoticeArchiveDsDao noticeDao;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Key childKey = stringToKey(req.getParameter(CleanUpServlet.KEY_PARAM));
		boolean archived = Boolean.parseBoolean(req
				.getParameter(CleanUpServlet.ARCHIVED_PARAM));

		Iterable<Entity> allNotices = getAllNotices(childKey, archived);
		Map<Key, Collection<Key>> duples = findDuples(allNotices, archived);

		int count = 0;
		for (Collection<Key> dupleKeys : duples.values()) {
			for (Key key : dupleKeys) {
				delete(key);
				count++;
			}
		}
		if (count > 0) {
			System.err.println("deleted " + count + " duplicate notices for child "
					+ childKey);
		}
	}

	private Iterable<Entity> getAllNotices(Key childKey, boolean archived) {

		Iterable<Entity> notices = getNoticeDao().getAllNoticesForChild(
				childKey, archived);
		List<Entity> sortedNotices = new ArrayList<Entity>();
		for (Entity notice : notices) {
			sortedNotices.add(notice);
		}
		Collections.sort(sortedNotices, new EntityComparator());

		return sortedNotices;
	}

	private NoticeArchiveDsDao getNoticeDao() {
		if (noticeDao == null) {
			synchronized (this) {
				if (noticeDao == null) {
					noticeDao = new NoticeArchiveDsDao();
				}
			}
		}
		return noticeDao;
	}

	Map<Key, Collection<Key>> findDuples(Iterable<Entity> allNotices,
			boolean archived) {
		Map<Key, Collection<Key>> dupleMap = new HashMap<Key, Collection<Key>>();
		GwtBeobachtung lastNotice = null;
		for (Entity entity : allNotices) {
			GwtBeobachtung notice = toGwt(entity);
			if (notice.equals(lastNotice)) {
				Collection<Key> duples = dupleMap.get(stringToKey(lastNotice
						.getKey()));
				if (duples == null) {
					duples = new ArrayList<Key>();
					dupleMap.put(stringToKey(lastNotice.getKey()), duples);
				}
				duples.add(stringToKey(notice.getKey()));
				notice = lastNotice;
			}
			lastNotice = notice;
		}

		Set<Entry<Key, Collection<Key>>> allEntries = new HashMap<>(dupleMap)
				.entrySet();
		for (Entry<Key, Collection<Key>> entry : allEntries) {

			if (isInGroup(entry.getKey(), archived)) {
				continue;
			}

			for (Key key : entry.getValue()) {
				if (isInGroup(key, archived)) {
					Collection<Key> duples = dupleMap.remove(entry.getKey());
					duples.remove(key);
					duples.add(entry.getKey());
					dupleMap.put(key, duples);
					break;
				}
			}
		}

		return dupleMap;
	}

	private boolean isInGroup(Key key, boolean archived) {
		if (allGroupedKeys == null) {
			allGroupedKeys = getAllGroupedKeys(archived);
		}
		return allGroupedKeys.contains(key);
	}

	private Collection<Key> getAllGroupedKeys(boolean archived) {

		return getNoticeDao().getAllGroupedKeys(archived);
	}

	private void delete(Key key) {

		getNoticeDao().deleteNotice(key);
	}

	private GwtBeobachtung toGwt(Entity entity) {

		Key key = entity.getKey();
		Key sectionKey = (Key) entity.getProperty(Notice.SECTION);
		String duration = (String) entity.getProperty(Notice.DURATION);
		String social = (String) entity.getProperty(Notice.SOCIAL);
		Date date = (Date) entity.getProperty(Notice.DATE);
		String text = ((Text) entity.getProperty(Notice.TEXT)).getValue();

		GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(keyToString(key));
		beobachtung.setChildKey("");
		beobachtung.setUser("");
		beobachtung.setSectionKey(sectionKey == null ? ""
				: keyToString(sectionKey));
		beobachtung.setDate(date);
		beobachtung.setText(text);
		if (duration != null) {
			beobachtung.setDuration(DurationEnum.valueOf(duration));
		}
		if (social != null) {
			beobachtung.setSocial(SocialEnum.valueOf(social));
		}
		return beobachtung;
	}
}
