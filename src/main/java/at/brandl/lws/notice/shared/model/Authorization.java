package at.brandl.lws.notice.shared.model;

import at.brandl.lws.notice.shared.validator.AuthorizationValidator;

public class Authorization extends GwtModel  {

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

	private Boolean editDialogueDates;

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
		if(seeAll != this.seeAll) {
			setChanged(true);
			this.seeAll = seeAll;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if(!equals(email ,this.email)) {
		setChanged(true);
			this.email = email;
		}
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		if(!equals(userId, this.userId)) {
			setChanged(true);
			this.userId = userId;
		}
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		if(admin != this.admin){
			setChanged(true);
			this.admin = admin;
		}
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
		if(editSections != this.editSections) {
			setChanged(true);
			this.editSections = editSections;
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isEditDialogueDates() {
		return editDialogueDates == null ? false : editDialogueDates.booleanValue();
	}

	public void setEditDialogueDates(Boolean editDialogueDates) {
if(editDialogueDates != this.editDialogueDates) {
	setChanged(true);
	this.editDialogueDates = editDialogueDates;
}
	}

	@Override
	public boolean isNew() {
		return key == null;
	}

	@Override
	public boolean isValid() {
		return AuthorizationValidator.validate(this);
	}

}
