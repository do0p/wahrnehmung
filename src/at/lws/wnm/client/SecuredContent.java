package at.lws.wnm.client;

import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.client.service.AuthorizationServiceAsync;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class SecuredContent
  implements EntryPoint
{
  private final AuthorizationServiceAsync authService = (AuthorizationServiceAsync)GWT.create(AuthorizationService.class);
  private Authorization authorization;

  public final void onModuleLoad()
  {
    executeSecured(this);
  }

  public void executeSecured(final SecuredContent securedContent) {
    this.authService.getAuthorizationForCurrentUser(
      Window.Location.createUrlBuilder().buildString(), 
      new AsyncCallback<Authorization>()
    {
      public void onFailure(Throwable caught)
      {
      }

      public void onSuccess(Authorization authorization)
      {
        SecuredContent.this.authorization = authorization;
        if (authorization.isLoggedIn())
          securedContent.onLogin(authorization);
        else
          securedContent.onLogOut(authorization);
      }
    });
  }

  protected abstract void onLogin(Authorization paramAuthorization);

  protected abstract void onLogOut(Authorization paramAuthorization);

  protected Authorization getAuthorization()
  {
    return this.authorization;
  }
}