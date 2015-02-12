package at.brandl.lws.notice.server.service;

import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ChildServiceImpl extends RemoteServiceServlet implements
		ChildService {

	private static final long serialVersionUID = -6319980504490088717L;
	private final ChildDsDao childDao;
	private final BeobachtungDsDao beobachtungsDao;
	private final AuthorizationServiceImpl authorizationService;

	public ChildServiceImpl() {
		childDao = DaoRegistry.get(ChildDsDao.class);
		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	}

	@Override
	public List<GwtChild> getAll() {

		return childDao.getAllChildren();

	}

	@Override
	public void store(GwtChild child) {
		authorizationService.assertCurrentUserIsAdmin();
		childDao.storeChild(child);
	}

	@Override
	public void delete(GwtChild child) throws IllegalArgumentException {
		authorizationService.assertCurrentUserIsAdmin();
		beobachtungsDao.deleteAllFromChild(child.getKey());
		childDao.deleteChild(child);
	}

	@Override
	public GwtChild get(String key) {
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
