package at.lws.wnm.server.service;

import java.util.Collection;

import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.server.dao.AuthorizationDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.Authorization;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthorizationServiceImpl extends RemoteServiceServlet implements
		AuthorizationService {

	private static final long serialVersionUID = 3902038151789960035L;
	private final AuthorizationDao authorizationDao;
	private UserService userService;

	public AuthorizationServiceImpl() {
		authorizationDao = DaoRegistry.get(AuthorizationDao.class);
		userService = UserServiceFactory.getUserService();
	}

	@Override
	public Collection<Authorization> queryAuthorizations() {

		return authorizationDao.queryAuthorizations();
	}

	@Override
	public void storeAuthorization(Authorization aut) {
		assertCurrentUserIsAdmin();
		authorizationDao.storeAuthorization(aut);
	}

	@Override
	public void deleteAuthorization(String email) {
		assertCurrentUserIsAdmin();
		authorizationDao.deleteAuthorization(email);
	}

	public void assertCurrentUserIsAdmin() {
		if (!currentUserIsAdmin()) {
			throw new IllegalStateException("current user is no admin: "
					+ userService.getCurrentUser());
		}
	}

	@Override
	public boolean currentUserIsAdmin() {
		final Authorization authorization = getAuthorizationForCurrentUserInternal();
		return authorization != null && authorization.isAdmin();
	}

	@Override
	public Authorization getAuthorizationForCurrentUser(String followUpUrl) {
		Authorization authorization = getAuthorizationForCurrentUserInternal();
		if (authorization == null) {
			authorization = new Authorization();
			authorization.setLoginUrl(userService.createLoginURL(followUpUrl));
			authorization.setLoggedIn(false);
		} else {
			authorization
					.setLogoutUrl(userService.createLogoutURL(followUpUrl));
			authorization.setLoggedIn(true);
		}
		return authorization;
	}

	private Authorization getAuthorizationForCurrentUserInternal() {
		return authorizationDao.getAuthorization(userService.getCurrentUser());
	}
}
