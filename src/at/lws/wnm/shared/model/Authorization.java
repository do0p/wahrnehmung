package at.lws.wnm.shared.model;

import java.io.Serializable;

public class Authorization implements Serializable {

	private static final long serialVersionUID = -5057466504583370610L;

	private String userId; // lowercase Email

	private String email;

	private boolean admin;

	private boolean seeAll;

	private Boolean editSections;

	private String loginUrl;

	private String logoutUrl;

	private boolean loggedIn;

	private String key;

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public boolean isSeeAll() {
		return seeAll;
	}

	public void setSeeAll(boolean seeAll) {
		this.seeAll = seeAll;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Authorization)) {
			return false;
		}
		final Authorization other = (Authorization) obj;
		return userId.equals(other.userId);
	}

	@Override
	public int hashCode() {
		return userId.hashCode();
	}

	public boolean isEditSections() {
		return editSections == null ? false : editSections.booleanValue();
	}

	public void setEditSections(Boolean editSections) {
		this.editSections = editSections;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
