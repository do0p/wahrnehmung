package at.lws.wnm.client;

import at.lws.wnm.shared.model.GwtUserInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserServiceAsync {

	void getUserInfo(String url, AsyncCallback<GwtUserInfo> callback);

}
