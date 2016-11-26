package at.brandl.lws.notice.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class DsUtil {

	public static String toString(Key key) {
		if(key == null) {
			return null;
		}
		return KeyFactory.keyToString(key);
	}

	public static Key toKey(String key) {
		if(key == null) {
			return null;
		}
		return KeyFactory.stringToKey(key);
	}

}
