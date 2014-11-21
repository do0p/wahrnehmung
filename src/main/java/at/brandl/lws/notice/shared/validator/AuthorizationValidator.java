package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.shared.model.Authorization;

public class AuthorizationValidator {

	public static boolean validate(Authorization authorization) {
		return authorization.getEmail() != null;
	}

}
