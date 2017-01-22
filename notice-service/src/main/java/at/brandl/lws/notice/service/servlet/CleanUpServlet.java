package at.brandl.lws.notice.service.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;

public class CleanUpServlet extends HttpServlet {

	public static final String KEY_PARAM = "key";
	public static final String ARCHIVED_PARAM = "archived";
	private static final long serialVersionUID = -7318489147891141902L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		System.err.println("Cleaning up all notices and groups" );

		int count = 0 ;
		
		NoticeArchiveDsDao archiveDao = new NoticeArchiveDsDao();
		
		boolean archived = true;
		Collection<Key> childKeys = archiveDao.getAllChildKeys();
		count += createTasks(childKeys, "/removeDuplicateNotices", archived);
		
		Collection<Key> parentKeys = archiveDao.getAllGroupParentKeys(archived);
		count += createTasks(parentKeys, "/removeDuplicateGroups", archived);
		
		Collection<Key> groupKeys = archiveDao.getAllGroupKeys(archived);
		count += createTasks(groupKeys, "/removeOrphanedGroups", archived);
		
		System.err.println("Queued " + count + " tasks");
	}

	

	private int createTasks(Collection<Key> keys, String taskUrl, boolean archived) {

		int count = 0;
		Queue queue = QueueFactory.getDefaultQueue();
		for (Key key : keys) {

			queue.add(createTask(taskUrl, key, archived));
			count++;
		}
		return count;
	}

	private TaskOptions createTask(String taskUrl, Key noticeKey, boolean archived) {

		String keyToString = KeyFactory.keyToString(noticeKey);
		TaskOptions moveNotice = TaskOptions.Builder.withUrl(taskUrl)
				.param(KEY_PARAM, keyToString).param(ARCHIVED_PARAM, Boolean.toString(archived)).method(Method.GET);
		return moveNotice;
	}


}
