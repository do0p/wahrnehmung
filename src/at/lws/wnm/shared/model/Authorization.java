package at.lws.wnm.shared.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Authorization implements Serializable {

	private static final long serialVersionUID = -5057466504583370610L;

	@Id
	private String userId; // lowercase Email

	private String email;

	private boolean admin;

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

}
