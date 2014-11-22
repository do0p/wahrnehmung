package at.brandl.lws.notice.shared.service;

import at.brandl.lws.notice.shared.model.Authorization;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("auth")
public abstract interface AuthorizationService extends
		ModelService<Authorization>, RemoteService {

	boolean currentUserIsAdmin();

	Authorization getAuthorizationForCurrentUser(String paramString);
}