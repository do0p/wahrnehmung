package at.lws.wnm.server.service;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.server.dao.AuthorizationDao;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.users.User;
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

	private AuthorizationDao authorizationDao;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = DaoRegistry.get(BeobachtungDao.class);
		authorizationDao = DaoRegistry.get(AuthorizationDao.class);
		userService = UserServiceFactory.getUserService();
	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		final User currentUser = userService.getCurrentUser();
		final Long masterBeobachtungsKey = beobachtungsDao.storeBeobachtung(beobachtung,
				currentUser, null);
		for(Long additionalChildKey : beobachtung.getAdditionalChildKeys())
		{
			beobachtung.setChildKey(additionalChildKey);
			beobachtungsDao.storeBeobachtung(beobachtung, currentUser, masterBeobachtungsKey);
		}
	}

	@Override
	public GwtBeobachtung getBeobachtung(Long beobachtungsKey) {
		return beobachtungsDao.getBeobachtung(beobachtungsKey);
	}

	@Override
	public BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter,
			Range range) {
		final User user = getUserForQuery();
		final BeobachtungsResult result = new BeobachtungsResult();
		if(filter.getChildKey() != null)
		{
		result.setBeobachtungen(beobachtungsDao.getBeobachtungen(filter, range,
				user));
		result.setRowCount(beobachtungsDao.getRowCount(filter, user));
		}
		
		return result;
	}

	private User getUserForQuery() {
		final User currentUser = userService.getCurrentUser();
		final Authorization authorization = authorizationDao
				.getAuthorization(currentUser);
		final User user = authorization.isSeeAll() ? null : currentUser;
		return user;
	}

	@Override
	public void deleteBeobachtung(Long beobachtungsKey) {
		beobachtungsDao.deleteBeobachtung(beobachtungsKey);

	}

}
