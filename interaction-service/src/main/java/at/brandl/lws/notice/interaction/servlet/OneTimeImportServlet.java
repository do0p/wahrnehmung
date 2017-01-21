package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants;

public class OneTimeImportServlet extends HttpServlet {

	private static final long serialVersionUID = -7835770028266126310L;
	private static final String INTERACTION_SERVICE_URL = "/storeInteraction";
	private static final String INTERACTION_QUEUE_NAME = "interaction";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		System.err.println("fetching distinct groups");
		Set<Key> distinctNoticeGroupParents = getDistinctNoticeGroupParents(ds);
		System.err.println(String.format("found %s distinct groups", distinctNoticeGroupParents.size()));
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		System.err.println("fetching distinct groups");
		Set<Key> distinctNoticeGroupParents = getDistinctNoticeGroupParents(ds);
		System.err.println(String.format("found %s distinct groups", distinctNoticeGroupParents.size()));
		for (Key parent : distinctNoticeGroupParents) {
			try {
				Date interactionDate = getNoticeDate(parent, ds);
				List<String> involvedChildren = getInvolvedChildren(parent, ds);
				registerInteractions(involvedChildren, interactionDate);
			} catch (EntityNotFoundException e) {
				System.err.println("no notice found for key " + parent);
			}
		}
	}

	private void registerInteractions(List<String> involvedChildren, Date interactionDate) {
		while (involvedChildren.size() > 1) {
			String child1 = involvedChildren.remove(0);
			for (String child2 : involvedChildren) {
				registerInteraction(child1, child2, interactionDate);
			}
		}
	}

	private Date getNoticeDate(Key noticeKey, DatastoreService ds) throws EntityNotFoundException {
		return (Date) ds.get(noticeKey).getProperty(Constants.Notice.DATE);
	}

	private List<String> getInvolvedChildren(Key parent, DatastoreService ds) {

		List<String> childKeys = new ArrayList<>();
		childKeys.add(DsUtil.toString(parent.getParent()));
		
		Query query = new Query(Constants.NoticeGroup.KIND, parent).addProjection(new PropertyProjection(Constants.NoticeGroup.BEOBACHTUNG, Key.class));

		Iterable<Entity> noticeGroups = ds.prepare(query).asIterable();
		for (Entity noticeGroup : noticeGroups) {
			Key noticeKey = (Key) noticeGroup.getProperty(Constants.NoticeGroup.BEOBACHTUNG);
			Key childKey = noticeKey.getParent();
			childKeys.add(DsUtil.toString(childKey));
		}

		return childKeys;
	}

	private Set<Key> getDistinctNoticeGroupParents(DatastoreService ds) {

		Set<Key> noticeGroupKeys = new HashSet<>();
		Query query = new Query(Constants.NoticeGroup.KIND).setKeysOnly();
		Iterable<Entity> noticeGroups = ds.prepare(query).asIterable();
		for (Entity noticeGroup : noticeGroups) {
			noticeGroupKeys.add(noticeGroup.getKey().getParent());
		}
		return noticeGroupKeys;
	}

	private void registerInteraction(String child1, String child2, Date date) {
		Queue queue = QueueFactory.getQueue(INTERACTION_QUEUE_NAME);
		queue.add(TaskOptions.Builder.withUrl(INTERACTION_SERVICE_URL).param("childKey", child1)
				.param("childKey", child2).param("date", new SimpleDateFormat("yyyy-MM-dd").format(date)));
	}

}
