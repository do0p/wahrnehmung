package at.lws.wnm.shared.model;

public class ObjectUtils {
	
	public static boolean equals(Object obj1, Object obj2) {
		return obj1 == null ? obj2 == null : obj1.equals(obj2);
	}

	public static int hashCode(Object obj) {
		return obj == null ? 0 : obj.hashCode();
	}
}
