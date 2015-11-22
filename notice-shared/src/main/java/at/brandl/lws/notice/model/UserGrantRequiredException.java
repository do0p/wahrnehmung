package at.brandl.lws.notice.model;

import java.io.Serializable;


public class UserGrantRequiredException extends Exception implements Serializable {

	private static final long serialVersionUID = -89669404812185807L;

	private String authorizationUrl;

	/**
	 * This ctor is needed to enable sending to gwt
	 */
	public UserGrantRequiredException() {
	}

	public UserGrantRequiredException(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

}
