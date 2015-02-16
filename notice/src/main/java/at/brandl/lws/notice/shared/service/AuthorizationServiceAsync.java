package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import at.brandl.lws.notice.model.Authorization;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthorizationServiceAsync {

	void queryAuthorizations(
			AsyncCallback<Collection<Authorization>> asyncCallback);

	void storeAuthorization(Authorization aut, AsyncCallback<Void> asyncCallback);

	void deleteAuthorization(String email, AsyncCallback<Void> asyncCallback);

	void currentUserIsAdmin(AsyncCallback<Boolean> callback);

	void getAuthorizationForCurrentUser(String followUpUrl,
			AsyncCallback<Authorization> callback);
	

}
