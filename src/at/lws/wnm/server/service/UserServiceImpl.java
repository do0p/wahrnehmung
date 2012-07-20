package at.lws.wnm.server.service;

import at.lws.wnm.client.UserService;
import at.lws.wnm.shared.model.GwtUserInfo;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class UserServiceImpl extends RemoteServiceServlet implements UserService {


	private static final long serialVersionUID = -1185740300371729964L;

	@Override
	public GwtUserInfo getUserInfo(String url) {
		final com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();
		final User currentUser = userService.getCurrentUser();
		final GwtUserInfo userInfo = new GwtUserInfo();
		if(currentUser != null)
		{
			userInfo.setLoggedIn(true);
			userInfo.setLogoutUrl(userService.createLogoutURL(url));
		}
		else
		{
			userInfo.setLoginUrl(userService.createLoginURL(url));
		}
		return userInfo;
	}

}
