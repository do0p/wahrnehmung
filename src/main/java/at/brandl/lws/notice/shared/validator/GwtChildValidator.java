package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.model.GwtChild;

public class GwtChildValidator {
	public static boolean validate(GwtChild child) {
		return Utils.isNotEmpty(child.getFirstName())
				&& Utils.isNotEmpty(child.getLastName())
				&& child.getBirthDay() != null;

	}
}
