package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;

public class GwtBeobachtungValidator {

	public static boolean validate(GwtBeobachtung beobachtung) {
		
		return beobachtung.getChildKey() != null
				&& beobachtung.getDate() != null
				&& beobachtung.getSectionKey() != null
				&& (beobachtung.isCountOnly() ? true : Utils
						.isNotEmpty(beobachtung.getText()));
	}
}