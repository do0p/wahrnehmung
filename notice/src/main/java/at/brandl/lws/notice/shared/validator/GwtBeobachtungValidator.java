package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.shared.Utils;

public class GwtBeobachtungValidator {

	public static boolean valid(GwtBeobachtung beobachtung) {
		
		String text = beobachtung.getText();
		return beobachtung.getChildKey() != null
				&& beobachtung.getDate() != null
				&& beobachtung.getSectionKey() != null
				&& (beobachtung.isCountOnly() ? true : Utils.isNotEmpty(text));
	}
}
