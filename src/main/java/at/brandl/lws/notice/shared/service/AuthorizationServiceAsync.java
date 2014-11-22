package at.brandl.lws.notice.shared.service;

import at.brandl.lws.notice.shared.model.Authorization;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthorizationServiceAsync extends ModelServiceAsync<Authorization>{

	
	void currentUserIsAdmin(AsyncCallback<Boolean> callback);

	void getAuthorizationForCurrentUser(String followUpUrl,
			AsyncCallback<Authorization> callback);
	

}
