package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class WahrnehmungsServiceImpl extends RemoteServiceServlet implements
		WahrnehmungsService {

	private final BeobachtungDao beobachtungsDao;

	private final UserService userService;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = new BeobachtungDao();
		userService = UserServiceFactory.getUserService();
	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		beobachtungsDao.storeBeobachtung(beobachtung, userService.getCurrentUser());
	}



	@Override
	public GwtBeobachtung getBeobachtung(Long beobachtungsKey) {
		return beobachtungsDao.getBeobachtung(beobachtungsKey);
	}

	@Override
	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range) {
		return beobachtungsDao.getBeobachtungen(filter, range);
	}

	@Override
	public int getRowCount(BeobachtungsFilter filter) {
		return beobachtungsDao.getRowCount(filter);
	}

}
