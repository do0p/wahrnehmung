package at.lws.wnm.client.service;

import java.util.Collection;

import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("auth")
public interface AuthorizationService extends RemoteService {

	Collection<Authorization> queryAuthorizations();

	void storeAuthorization(Authorization aut);

	void deleteAuthorization(String email);

	boolean currentUserIsAdmin();

	Authorization getAuthorizationForCurrentUser(String followUpUrl);

}
