package at.brandl.lws.notice.server.service;

import java.util.Collection;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.server.dao.ds.AuthorizationDsDao;
import at.brandl.lws.notice.shared.service.AuthorizationService;

public class AuthorizationServiceImpl extends RemoteServiceServlet implements
		AuthorizationService {
	private static final long serialVersionUID = 3902038151789960035L;
	private final AuthorizationDsDao authorizationDao;
	private UserService userService;

	public AuthorizationServiceImpl() {
		this.authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
		this.userService = UserServiceFactory.getUserService();
	}

	public Collection<Authorization> queryAuthorizations() {
		return this.authorizationDao.queryAuthorizations();
	}

	public void storeAuthorization(Authorization aut) {
		assertCurrentUserIsAdmin();
		this.authorizationDao.storeAuthorization(aut);
	}

	public void deleteAuthorization(String email) {
		assertCurrentUserIsAdmin();
		this.authorizationDao.deleteAuthorization(email);
	}

	public void assertCurrentUserIsAdmin() {
		if (!currentUserIsAdmin())
			throw new IllegalStateException("current user is no admin: "
					+ this.userService.getCurrentUser());
	}

	public void assertCurrentUserIsSectionAdmin() {
		if (!currentUserIsSectionAdmin())
			throw new IllegalStateException(
					"current user is no section-admin: "
							+ this.userService.getCurrentUser());
	}

	public void assertCurrentUserIsTeacher() {
		if (!currentUserIsTeacher())
			throw new IllegalStateException("current user is no teacher: "
					+ this.userService.getCurrentUser());
	}

	public boolean currentUserIsAdmin() {

		Authorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null) && (authorization.isAdmin());
	}

	public boolean currentUserIsSectionAdmin() {
		Authorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null)
				&& ((authorization.isAdmin()) || (authorization
						.isEditSections()));
	}

	public boolean currentUserIsTeacher() {
		Authorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null) && (authorization.isSeeAll());
	}

	public Authorization getAuthorizationForCurrentUser(String followUpUrl) {
		Authorization authorization = getAuthorizationForCurrentUserInternal();
		if (authorization == null) {
			authorization = new Authorization();
			authorization.setLoginUrl(this.userService
					.createLoginURL(followUpUrl));
			authorization.setLoggedIn(false);
		} else {
			authorization.setLogoutUrl(this.userService
					.createLogoutURL(followUpUrl));
			authorization.setLoggedIn(true);
		}
		return authorization;
	}

	private Authorization getAuthorizationForCurrentUserInternal() {
		if (!userService.isUserLoggedIn()) {
			return null;
		}
		User user = this.userService.getCurrentUser();
		if (userService.isUserAdmin()) {
			return createSuperUserAuthorization(user);
		}
		return this.authorizationDao.getAuthorization(user);
	}

	private Authorization createSuperUserAuthorization(User user) {
		Authorization authorization = new Authorization();
		authorization.setAdmin(true);
		authorization.setEditDialogueDates(true);
		authorization.setEditSections(true);
		authorization.setSeeAll(true);
		authorization.setSuperUser(true);
		authorization.setUserId(user.getEmail().toLowerCase());
		authorization.setEmail(user.getEmail());
		return authorization;
	}

	public Authorization getAuthorization(User currentUser) {

		return getAuthorizationForCurrentUserInternal();
	}

}