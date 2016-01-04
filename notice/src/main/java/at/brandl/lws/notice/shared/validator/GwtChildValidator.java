package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.shared.Utils;

public class GwtChildValidator {
	
	public static boolean valid(GwtChild child) {
	
		if (Utils.isEmpty(child.getFirstName())
				|| Utils.isEmpty(child.getLastName())
				|| child.getBirthDay() == null || child.getBeginGrade() == null
				|| child.getBeginYear() == null) {
			return false;
		}
		return true;
	}
}
