package at.brandl.lws.notice.server.service;

import java.util.Collection;

import at.brandl.lws.notice.client.service.AuthorizationService;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.AuthorizationDsDao;
import at.brandl.lws.notice.shared.model.Authorization;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthorizationServiceImpl extends RemoteServiceServlet
  implements AuthorizationService
{
  private static final long serialVersionUID = 3902038151789960035L;
  private final AuthorizationDsDao authorizationDao;
  private UserService userService;

  public AuthorizationServiceImpl()
  {
    this.authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
    this.userService = UserServiceFactory.getUserService();
  }

  public Collection<Authorization> queryAuthorizations()
  {
    return this.authorizationDao.queryAuthorizations();
  }

  public void storeAuthorization(Authorization aut)
  {
    assertCurrentUserIsAdmin();
    this.authorizationDao.storeAuthorization(aut);
  }

  public void deleteAuthorization(String email)
  {
    assertCurrentUserIsAdmin();
    this.authorizationDao.deleteAuthorization(email);
  }

  public void assertCurrentUserIsAdmin() {
    if (!currentUserIsAdmin())
      throw new IllegalStateException("current user is no admin: " + 
        this.userService.getCurrentUser());
  }

  public void assertCurrentUserIsSectionAdmin() {
    if (!currentUserIsSectionAdmin())
      throw new IllegalStateException("current user is no section-admin: " + 
        this.userService.getCurrentUser());
  }

  public boolean currentUserIsAdmin()
  {
    Authorization authorization = getAuthorizationForCurrentUserInternal();
    return (authorization != null) && (authorization.isAdmin());
  }

  public boolean currentUserIsSectionAdmin() {
    Authorization authorization = getAuthorizationForCurrentUserInternal();
    return (authorization != null) && ((authorization.isAdmin()) || (authorization.isEditSections()));
  }

  public Authorization getAuthorizationForCurrentUser(String followUpUrl)
  {
    Authorization authorization = getAuthorizationForCurrentUserInternal();
    if (authorization == null) {
      authorization = new Authorization();
      authorization.setLoginUrl(this.userService.createLoginURL(followUpUrl));
      authorization.setLoggedIn(false);
    } else {
      authorization
        .setLogoutUrl(this.userService.createLogoutURL(followUpUrl));
      authorization.setLoggedIn(true);
    }
    return authorization;
  }

  private Authorization getAuthorizationForCurrentUserInternal() {
    return this.authorizationDao.getAuthorization(this.userService.getCurrentUser());
  }
}