package at.brandl.lws.notice.service.servlet;

import static com.google.appengine.api.datastore.KeyFactory.stringToKey;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class RemoveOrphanedGroups extends HttpServlet {

	private static final long serialVersionUID = -7318489147891141902L;
	private NoticeArchiveDsDao noticeDao;
	private Collection<Key> allNoticeKeys;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Key groupKey = stringToKey(req.getParameter(CleanUpServlet.KEY_PARAM));
		boolean archived = Boolean.parseBoolean(req.getParameter(CleanUpServlet.ARCHIVED_PARAM));

		Entity group = getGroup(groupKey);
		if (isOrphaned(group, archived)) {
			System.err.println("deleting orphaned group " + group);
			delete(group.getKey());
		}
	}

	private boolean isOrphaned(Entity group, boolean archived) {
		if (group == null) {
			return false;
		}

		return !(noticeExists(group.getParent(), archived) && noticeExists(
				(Key) group.getProperty(NoticeGroup.BEOBACHTUNG), archived));
	}

	private Entity getGroup(Key groupKey) {
		return getNoticeDao().getGroup(groupKey);
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

	private boolean noticeExists(Key key, boolean archived) {

		if (allNoticeKeys == null) {
			allNoticeKeys = getAllNoticeKeys(archived);
		}
		return allNoticeKeys.contains(key);
	}

	private Collection<Key> getAllNoticeKeys(boolean archived) {

		return getNoticeDao().getAllNoticeKeys(archived);
	}

	private void delete(Key key) {

		getNoticeDao().deleteNotice(key);
	}

}
