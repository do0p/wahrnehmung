package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.Date;

public class GwtChild implements Serializable, Comparable<GwtChild>{

	private static final long serialVersionUID = 5870082887319396186L;
	private String firstName;
	private String lastName;
	private Date birthDay;
	private Long key;

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setBirthDay(Date birthDay) {
		this.birthDay = birthDay;
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public int compareTo(GwtChild other) {
		int result = lastName.compareTo(other.lastName);
		if(result == 0)
		{
			result = firstName.compareTo(other.firstName);
			if(result == 0)
			{
				result = birthDay.compareTo(other.birthDay);
			}
		}
		return result;
	}
}
