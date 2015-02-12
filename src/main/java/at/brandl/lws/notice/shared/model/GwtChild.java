package at.brandl.lws.notice.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.validator.GwtChildValidator;

public class GwtChild extends GwtModel implements Comparable<GwtChild> {

	private static final long serialVersionUID = 5870082887319396186L;
	private String firstName;
	private String lastName;
	private Date birthDay;
	private String key;
	private List<Date> developementDialogueDates;

	public List<Date> getDevelopementDialogueDates() {
		return developementDialogueDates;
	}

	public void setDevelopementDialogueDates(List<Date> developementDialogues) {
		this.developementDialogueDates = developementDialogues;
	}

	public void addDevelopementDialogueDate(Date date) {
		if (developementDialogueDates == null) {
			developementDialogueDates = new ArrayList<Date>();
		}
		if (!developementDialogueDates.contains(date)) {
			setChanged(true);
			developementDialogueDates.add(date);
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setFirstName(String firstName) {
		if (!equals(firstName, this.firstName)) {
			setChanged(true);
			this.firstName = firstName;
		}
	}

	public void setLastName(String lastName) {
		if (!equals(lastName, this.lastName)) {
			setChanged(true);
			this.lastName = lastName;
		}
	}

	public void setBirthDay(Date birthDay) {
		if (!equals(birthDay, this.birthDay)) {
			setChanged(true);
			this.birthDay = birthDay;
		}
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public int compareTo(GwtChild other) {
		int result = lastName.compareTo(other.lastName);
		if (result == 0) {
			result = firstName.compareTo(other.firstName);
			if (result == 0) {
				result = birthDay.compareTo(other.birthDay);
			}
		}
		return result;
	}

	public void removeDevelopementDialogueDate(Date date) {
		if (developementDialogueDates != null) {
			developementDialogueDates.remove(date);
		}
	}

	public Date getLastDevelopementDialogueDate() {
		return developementDialogueDates == null
				|| developementDialogueDates.isEmpty() ? null
				: developementDialogueDates.get(0);
	}

	@Override
	public boolean isNew() {
		return Utils.isEmpty(key);
	}

	@Override
	public boolean isValid() {
		return GwtChildValidator.validate(this);
	}

}
