package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.GwtAuthorization;

@RemoteServiceRelativePath("auth")
public abstract interface AuthorizationService extends RemoteService
{
  Collection<GwtAuthorization> queryAuthorizations();

  void storeAuthorization(GwtAuthorization paramAuthorization);

  void deleteAuthorization(String paramString);

  boolean currentUserIsAdmin();

  GwtAuthorization getAuthorizationForCurrentUser(String paramString);

}