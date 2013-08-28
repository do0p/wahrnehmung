package at.lws.wnm.server.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import at.lws.wnm.shared.model.GwtChild;

@Entity
public class Child implements Serializable, Comparable<Child> {

	private static final long serialVersionUID = -112994610784102648L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;

	private String firstName;
	private String lastName;
	private Date birthDay;

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(Date birthDay) {
		this.birthDay = birthDay;
	}

	public static Child valueOf(GwtChild gwtChild) {
		final Child child = new Child();
		final String childKey = gwtChild.getKey();
		if (childKey != null) {
			child.key = Long.valueOf(childKey);
		}
		child.firstName = gwtChild.getFirstName();
		child.lastName = gwtChild.getLastName();
		child.birthDay = gwtChild.getBirthDay();
		return child;
	}

	public GwtChild toGwt() {
		final GwtChild child = new GwtChild();
		child.setKey(key == null ? null : key.toString());
		child.setFirstName(firstName);
		child.setLastName(lastName);
		child.setBirthDay(birthDay);
		return child;
	}

	@Override
	public int compareTo(Child other) {
		int result = lastName.compareTo(other.lastName);
		if (result == 0) {
			result = firstName.compareTo(other.firstName);
			if (result == 0) {
				result = birthDay.compareTo(other.birthDay);
			}
		}
		return result;
	}
}
