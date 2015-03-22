package at.brandl.lws.notice.service.servlet;

import static com.google.appengine.api.datastore.KeyFactory.keyToString;
import static com.google.appengine.api.datastore.KeyFactory.stringToKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

	private static final boolean ARCHIVED = true;
	private static final long serialVersionUID = -7318489147891141902L;
	private Set<Key> allGroupedKeys;
	private NoticeArchiveDsDao noticeDao;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Key childKey = stringToKey(req.getParameter(MoveAllServlet.KEY_PARAM));

		boolean archived = ARCHIVED;

		Iterable<Entity> allNotices = getAllNotices(childKey, archived);
		Map<Key, Collection<Key>> duples = findDuples(allNotices, archived);
		int countDuples = countDuples(duples);
		System.err.println("found " + countDuples + " duples for key " + childKey);
		int count = 0;
		for (Entry<Key, Collection<Key>> duple : duples.entrySet()) {
			for (Key key : duple.getValue()) {
//				delete(key);
				count++;
			}
		}
		System.err.println("deleted " + count + " duples for key " + childKey);
	}

	private int countDuples(Map<Key, Collection<Key>> duples) {

		int count = 0;
		for (Entry<Key, Collection<Key>> duple : duples.entrySet()) {
			count += duple.getValue().size();
		}
		return count;
	}

	private Iterable<Entity> getAllNotices(Key childKey, boolean archived) {
		return getNoticeDao().getAllNoticesSorted(childKey, archived);
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

	private Map<Key, Collection<Key>> findDuples(Iterable<Entity> allNotices,
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

	private Set<Key> getAllGroupedKeys(boolean archived) {

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
