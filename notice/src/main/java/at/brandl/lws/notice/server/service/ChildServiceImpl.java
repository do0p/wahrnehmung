package at.brandl.lws.notice.server.service;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.util.Constants;

public class ChildServiceImpl extends RemoteServiceServlet implements
		ChildService {

	private static final long serialVersionUID = -6319980504490088717L;
	private static final String INTERACTION_SERVICE_PATH = "/storeInteraction";
	private final ChildDsDao childDao;
	private final BeobachtungDsDao beobachtungsDao;
	private final AuthorizationServiceImpl authorizationService;

	public ChildServiceImpl() {
		childDao = DaoRegistry.get(ChildDsDao.class);
		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	}

	@Override
	public List<GwtChild> queryChildren() {

		return childDao.getAllChildren();

	}

	@Override
	public void storeChild(GwtChild child) {
		authorizationService.assertCurrentUserIsAdmin();
		childDao.storeChild(child);
	}

	@Override
	public void deleteChild(String childKey) throws IllegalArgumentException {
		authorizationService.assertCurrentUserIsAdmin();
		
		Queue queue = QueueFactory.getQueue(Constants.INTERACTION_QUEUE_NAME);
		queue.add(TaskOptions.Builder.withUrl(INTERACTION_SERVICE_PATH).method(Method.DELETE).param("childKey", childKey).header("Host", ModulesServiceFactory.getModulesService().getVersionHostname("interaction-service" ,null)));

		beobachtungsDao.deleteAllFromChild(childKey);
		childDao.deleteChild(childKey);
	}

	@Override
	public GwtChild getChild(String key) {
		return childDao.getChild(key);
	}

	@Override
	public void addDevelopementDialogueDate(String key, Date date)  throws IllegalArgumentException {
		GwtChild child = childDao.getChild(key);
		child.addDevelopementDialogueDate(date);
		childDao.storeChild(child);
	}

	@Override
	public void deleteDevelopementDialogueDate(String key, Date date) {
		GwtChild child = childDao.getChild(key);
		child.removeDevelopementDialogueDate(date);
		childDao.storeChild(child);
		
	}

}
