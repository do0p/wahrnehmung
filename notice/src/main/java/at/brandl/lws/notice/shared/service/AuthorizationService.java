package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import at.brandl.lws.notice.model.Authorization;

import com.google.appengine.api.users.User;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("auth")
public abstract interface AuthorizationService extends RemoteService
{
  Collection<Authorization> queryAuthorizations();

  void storeAuthorization(Authorization paramAuthorization);

  void deleteAuthorization(String paramString);

  boolean currentUserIsAdmin();

  Authorization getAuthorizationForCurrentUser(String paramString);

}