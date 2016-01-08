package at.brandl.lws.notice.shared;


public class Utils {

	public static boolean isNotEmpty(String text) {
		return !isEmpty(text);
	}

	public static boolean isEmpty(String text) {
		return text == null || text.trim().isEmpty();
	}

}
