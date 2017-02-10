package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.model.GwtAuthorization;

public interface AuthorizationServiceAsync {

	void queryAuthorizations(
			AsyncCallback<Collection<GwtAuthorization>> asyncCallback);

	void storeAuthorization(GwtAuthorization aut, AsyncCallback<Void> asyncCallback);

	void deleteAuthorization(String email, AsyncCallback<Void> asyncCallback);

	void currentUserIsAdmin(AsyncCallback<Boolean> callback);

	void getAuthorizationForCurrentUser(String followUpUrl,
			AsyncCallback<GwtAuthorization> callback);
	

}
