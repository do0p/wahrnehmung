package at.lws.wnm.client.service;

import java.util.Collection;

import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthorizationServiceAsync {

	void queryAuthorizations(
			AsyncCallback<Collection<Authorization>> asyncCallback);

	void storeAuthorization(Authorization aut, AsyncCallback<Void> asyncCallback);

	void deleteAuthorization(String email, AsyncCallback<Void> asyncCallback);
	

}
