package at.brandl.lws.notice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.shared.service.AuthorizationService;
import at.brandl.lws.notice.shared.service.AuthorizationServiceAsync;

public abstract class SecuredContent implements EntryPoint {

	private final AuthorizationServiceAsync authService = (AuthorizationServiceAsync) GWT
			.create(AuthorizationService.class);
	private GwtAuthorization authorization;

	public final void onModuleLoad() {
		executeSecured(this);
	}

	public void executeSecured(final SecuredContent securedContent) {
		this.authService.getAuthorizationForCurrentUser(Window.Location.createUrlBuilder().buildString(),
				new AsyncCallback<GwtAuthorization>() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess(GwtAuthorization authorization) {
						SecuredContent.this.authorization = authorization;
						if (authorization.isLoggedIn())
							securedContent.onLogin(authorization);
						else
							securedContent.onLogOut(authorization);
					}
				});
	}

	protected abstract void onLogin(GwtAuthorization paramAuthorization);

	protected abstract void onLogOut(GwtAuthorization paramAuthorization);

	protected GwtAuthorization getAuthorization() {
		return this.authorization;
	}
}