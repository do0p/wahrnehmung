package at.lws.wnm.server.service;

import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.server.dao.AuthorizationDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.Authorization;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.Collection;

public class AuthorizationServiceImpl extends RemoteServiceServlet
  implements AuthorizationService
{
  private static final long serialVersionUID = 3902038151789960035L;
  private final AuthorizationDao authorizationDao;
  private UserService userService;

  public AuthorizationServiceImpl()
  {
    this.authorizationDao = ((AuthorizationDao)DaoRegistry.get(AuthorizationDao.class));
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