package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.service.ChildService;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.server.dao.ChildDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ChildServiceImpl extends RemoteServiceServlet implements
		ChildService {

	private final ChildDao childDao;
	private final BeobachtungDao beobachtungsDao;

	public ChildServiceImpl() {
		childDao = DaoRegistry.get(ChildDao.class);
		beobachtungsDao = DaoRegistry.get(BeobachtungDao.class);
	}

	@Override
	public List<GwtChild> queryChildren() {

		return childDao.getAllChildren();

	}

	@Override
	public void storeChild(GwtChild child) {
		childDao.storeChild(child);
	}

	@Override
	public void deleteChild(GwtChild child) throws IllegalArgumentException {
		beobachtungsDao.deleteAllFromChild(child.getKey());
		childDao.deleteChild(child);
	}

	@Override
	public GwtChild getChild(Long key) {
		return childDao.getChild(key);
	}

}
