package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthorizationServiceAsync {

	void queryAuthorizations(AsyncCallback<List<Authorization>> asyncCallback);

	void storeAuthorization(Authorization aut, AsyncCallback<Void> asyncCallback);

	void deleteAuthorization(String email, AsyncCallback<Void> asyncCallback);
	

}
