package at.brandl.lws.notice.shared;

public class Utils {

	public static final String GS_BUCKET_NAME = "wahrnehmung-test.appspot.com";

	public static boolean isEmpty(String text) {
		return text == null || text.trim().isEmpty();
	}

	public static boolean isNotEmpty(String text) {
		return !isEmpty(text);
	}

}
