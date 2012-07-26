package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class WahrnehmungsServiceImpl extends RemoteServiceServlet implements
		WahrnehmungsService {

	private final BeobachtungDao beobachtungsDao;

	// private final UserService userService;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = new BeobachtungDao();
		// userService = UserServiceFactory.getUserService();
	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		// beobachtung.setUser(userService.getCurrentUser());
		beobachtungsDao.storeBeobachtung(beobachtung);
	}

	@Override
	public List<GwtBeobachtung> getBeobachtungen(Long childNo, Long sectionNo) {
		return beobachtungsDao.getBeobachtungen(childNo, sectionNo);
	}

	@Override
	public GwtBeobachtung getBeobachtung(Long beobachtungsKey) {
		return beobachtungsDao.getBeobachtung(beobachtungsKey);
	}

}
