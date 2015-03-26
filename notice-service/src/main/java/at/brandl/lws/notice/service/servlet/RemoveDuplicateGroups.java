package at.brandl.lws.notice.service.servlet;

import static com.google.appengine.api.datastore.KeyFactory.stringToKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.model.ObjectUtils;
import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class RemoveDuplicateGroups extends HttpServlet {

	private static final long serialVersionUID = -7318489147891141902L;
	private NoticeArchiveDsDao noticeDao;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Key parentKey = stringToKey(req.getParameter(CleanUpServlet.KEY_PARAM));
		boolean archived = Boolean.parseBoolean(req
				.getParameter(CleanUpServlet.ARCHIVED_PARAM));

		Iterable<Entity> allGroups = getAllGroups(parentKey, archived);
		Map<Key, Collection<Key>> duples = findDuples(allGroups, archived);

		int count = 0;
		for (Entry<Key, Collection<Key>> duple : duples.entrySet()) {
			for (Key key : duple.getValue()) {
				delete(key);
				count++;
			}
		}
		if (count > 0) {
			System.err.println("deleted " + count
					+ " duplicate groups for key " + parentKey);
		}
	}

	private Iterable<Entity> getAllGroups(Key parentKey, boolean archived) {

		Iterable<Entity> groups = getNoticeDao().getAllGroups(parentKey,
				archived);

		List<Entity> sortedGroups = new ArrayList<Entity>();
		for (Entity group : groups) {
			sortedGroups.add(group);
		}
		Collections.sort(sortedGroups, new Comparator<Entity>() {

			@Override
			public int compare(Entity o1, Entity o2) {

				Key key1 = (Key) o1.getProperty(NoticeGroup.BEOBACHTUNG);
				Key key2 = (Key) o2.getProperty(NoticeGroup.BEOBACHTUNG);

				if (isNull(key1)) {
					if (isNull(key2)) {
						return 0;
					}
					return 1;
				}
				if (isNull(key2)) {
					return -1;
				}
				return key1.compareTo(key2);
			}

			private boolean isNull(Key key) {
				return key == null || !key.isComplete();
			}
		});

		return sortedGroups;
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

	Map<Key, Collection<Key>> findDuples(Iterable<Entity> allGroups,
			boolean archived) {

		Map<Key, Collection<Key>> dupleMap = new HashMap<Key, Collection<Key>>();
		Entity lastGroup = null;
		for (Entity group : allGroups) {
			if (equals(group, lastGroup)) {
				Collection<Key> duples = dupleMap.get(lastGroup.getKey());
				if (duples == null) {
					duples = new ArrayList<Key>();
					dupleMap.put(lastGroup.getKey(), duples);
				}
				duples.add(group.getKey());
				group = lastGroup;
			}
			lastGroup = group;
		}

		return dupleMap;
	}

	private boolean equals(Entity group, Entity lastGroup) {

		if (lastGroup == null) {
			return false;
		}
		return ObjectUtils.equals(getNoticeKey(group), getNoticeKey(lastGroup));
	}

	private Key getNoticeKey(Entity entity) {

		return (Key) entity.getProperty(NoticeGroup.BEOBACHTUNG);
	}

	private void delete(Key key) {

		getNoticeDao().deleteNotice(key);
	}
}
