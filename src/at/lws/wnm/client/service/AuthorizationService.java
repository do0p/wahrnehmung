package at.lws.wnm.client.service;

import java.util.List;

import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("auth")
public interface AuthorizationService extends RemoteService {

	List<Authorization> queryAuthorizations();

	void storeAuthorization(Authorization aut);

	void deleteAuthorization(String email);

}
