package at.lws.wnm.client.service;

import at.lws.wnm.shared.model.Authorization;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Collection;

@RemoteServiceRelativePath("auth")
public abstract interface AuthorizationService extends RemoteService
{
  Collection<Authorization> queryAuthorizations();

  void storeAuthorization(Authorization paramAuthorization);

  void deleteAuthorization(String paramString);

  boolean currentUserIsAdmin();

  Authorization getAuthorizationForCurrentUser(String paramString);
}