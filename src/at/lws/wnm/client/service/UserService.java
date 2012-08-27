package at.lws.wnm.client.service;

import at.lws.wnm.shared.model.GwtUserInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {
	GwtUserInfo getUserInfo(String url);
}
