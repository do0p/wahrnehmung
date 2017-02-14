package at.brandl.wahrnehmung.it.selenium.util;

public class User {
	
	private final String email;
	private final boolean admin;
	private final boolean teacher;
	private final boolean sectionAdmin;
	
	public User(String email, boolean admin, boolean teacher, boolean sectionAdmin) {
		this.email = email;
		this.admin = admin;
		this.teacher = teacher;
		this.sectionAdmin = sectionAdmin;
	}

	public String getEmail() {
		return email;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isTeacher() {
		return teacher;
	}

	public boolean isSectionAdmin() {
		return sectionAdmin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (admin ? 1231 : 1237);
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (sectionAdmin ? 1231 : 1237);
		result = prime * result + (teacher ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (admin != other.admin)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (sectionAdmin != other.sectionAdmin)
			return false;
		if (teacher != other.teacher)
			return false;
		return true;
	}
}
