package at.brandl.lws.notice.service.servlet;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class MoveAllServlet extends HttpServlet {

	public static final String KEY_PARAM = "key";
	private static final int AUGUST = 7;
	private static final long serialVersionUID = -7318489147891141902L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		System.err.println("Creating move tasks");
		Date date = calcEndLastSchoolYear();
		int count = archive(date);

		System.err.println("Queued " + count + " tasks");
	}

	int archive(Date date) {
		int count = 0;
		NoticeArchiveDsDao archiveDao = new NoticeArchiveDsDao();

		Set<Key> allNoticeKeys = archiveDao.getAllNoticeKeysBefore(date);
		Set<Key> allGroups = archiveDao.getAllGroupParentKeys(allNoticeKeys);

		count += createTasks(allGroups, "/moveGroups");
		count += createTasks(allNoticeKeys, "/moveNotice");
		return count;
	}

	private int createTasks(Set<Key> keys, String taskUrl) {

		System.err.println("Creating " + keys.size() + " tasks for " + taskUrl);
		int count = 0;
		Queue queue = QueueFactory.getDefaultQueue();
		for (Key noticeKey : keys) {

			String keyToString = KeyFactory.keyToString(noticeKey);
			try {
				TaskOptions moveNotice = TaskOptions.Builder.withUrl(taskUrl)
						.param(KEY_PARAM, keyToString)
						.method(Method.GET);
				queue.add(moveNotice);
				count++;
			} catch (TaskAlreadyExistsException e) {
				System.err.println("task with name " + keyToString + " already exists.");
				// thats fine, go one
			}
		}
		System.err.println("Created " + count + " tasks for " + taskUrl);
		return count;
	}

	private Date calcEndLastSchoolYear() {

		Calendar calendar = new GregorianCalendar();
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		if (month < AUGUST) {
			year--;
		}
		calendar.set(year, AUGUST, 1, 0, 0, 0);
		return calendar.getTime();
	}

}
