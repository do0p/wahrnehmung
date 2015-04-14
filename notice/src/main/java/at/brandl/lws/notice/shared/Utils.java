package at.brandl.lws.notice.shared;

import java.util.Collection;

import at.brandl.lws.notice.model.GwtBeobachtung;

import com.google.gwt.i18n.client.DateTimeFormat;

public class Utils {

	public static final String GS_BUCKET_NAME = "wahrnehmung-test.appspot.com";


	public static boolean isNotEmpty(String text) {
		return !isEmpty(text);
	}

	public static boolean isEmpty(String text) {
		return text == null || text.trim().isEmpty();
	}

}
