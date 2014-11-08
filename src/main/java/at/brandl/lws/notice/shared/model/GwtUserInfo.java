package at.brandl.lws.notice.shared.model;

import java.io.Serializable;


public class GwtUserInfo implements Serializable {

	private static final long serialVersionUID = -4432504398209071078L;
	private String logoutUrl;
	private String loginUrl;
	private boolean loggedIn;
	

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

}
