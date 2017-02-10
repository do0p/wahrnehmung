package at.brandl.lws.notice.server.service;

import java.util.Collection;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.server.dao.ds.AuthorizationDsDao;
import at.brandl.lws.notice.shared.service.AuthorizationService;

public class AuthorizationServiceImpl extends RemoteServiceServlet implements
		AuthorizationService {
	private static final long serialVersionUID = 3902038151789960035L;
	private final AuthorizationDsDao authorizationDao;
	private final UserService userService;

	public AuthorizationServiceImpl() {
		authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
		userService = UserServiceFactory.getUserService();
	}

	public Collection<GwtAuthorization> queryAuthorizations() {
		return authorizationDao.queryAuthorizations();
	}

	public void storeAuthorization(GwtAuthorization aut) {
		assertCurrentUserIsAdmin();
		authorizationDao.storeAuthorization(aut);
	}

	public void deleteAuthorization(String email) {
		assertCurrentUserIsAdmin();
		authorizationDao.deleteAuthorization(email);
	}

	public void assertCurrentUserIsAdmin() {
		if (!currentUserIsAdmin())
			throw new IllegalStateException("current user is no admin: "
					+ userService.getCurrentUser());
	}

	public void assertCurrentUserIsSectionAdmin() {
		if (!currentUserIsSectionAdmin())
			throw new IllegalStateException(
					"current user is no section-admin: "
							+ userService.getCurrentUser());
	}

	public void assertCurrentUserIsTeacher() {
		if (!currentUserIsTeacher())
			throw new IllegalStateException("current user is no teacher: "
					+ userService.getCurrentUser());
	}

	public boolean currentUserIsAdmin() {

		GwtAuthorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null) && (authorization.isAdmin());
	}

	public boolean currentUserIsSectionAdmin() {
		GwtAuthorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null)
				&& ((authorization.isAdmin()) || (authorization
						.isEditSections()));
	}

	public boolean currentUserIsTeacher() {
		GwtAuthorization authorization = getAuthorizationForCurrentUserInternal();
		return (authorization != null) && (authorization.isSeeAll());
	}

	public GwtAuthorization getAuthorizationForCurrentUser(String followUpUrl) {
		GwtAuthorization authorization = getAuthorizationForCurrentUserInternal();
		if (authorization == null) {
			authorization = new GwtAuthorization();
			authorization.setLoginUrl(userService
					.createLoginURL(followUpUrl));
			authorization.setLoggedIn(false);
		} else {
			authorization.setLogoutUrl(userService
					.createLogoutURL(followUpUrl));
			authorization.setLoggedIn(true);
		}
		return authorization;
	}

	private GwtAuthorization getAuthorizationForCurrentUserInternal() {
		if (!userService.isUserLoggedIn()) {
			return null;
		}
		User user = userService.getCurrentUser();
		if (userService.isUserAdmin()) {
			return createSuperUserAuthorization(user);
		}
		return authorizationDao.getAuthorization(user);
	}

	private GwtAuthorization createSuperUserAuthorization(User user) {
		GwtAuthorization authorization = new GwtAuthorization();
		authorization.setAdmin(true);
		authorization.setEditDialogueDates(true);
		authorization.setEditSections(true);
		authorization.setSeeAll(true);
		authorization.setSuperUser(true);
		authorization.setUserId(user.getEmail().toLowerCase());
		authorization.setEmail(user.getEmail());
		return authorization;
	}

	public GwtAuthorization getAuthorization(User currentUser) {

		return getAuthorizationForCurrentUserInternal();
	}

}