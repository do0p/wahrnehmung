package at.brandl.lws.notice.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Predicate;

public class DsUtil {

	public static String toString(Key key) {
		if (key == null) {
			return null;
		}
		return KeyFactory.keyToString(key);
	}

	public static Key toKey(String key) {
		if (key == null) {
			return null;
		}
		return KeyFactory.stringToKey(key);
	}

	public static Integer getInteger(Entity entity, String propertyName) {
		Long longValue = (Long) entity.getProperty(propertyName);
		if (longValue == null) {
			return null;
		}
		if (!isInIntRange(longValue)) {
			throw new RuntimeException(String.format("longValue %d is not in int range", longValue.longValue()));
		}
		return Integer.valueOf(longValue.intValue());
	}

	public static String getKeyString(Entity entity, String propertyName) {
		return toString((Key) entity.getProperty(propertyName));
	}



	private static boolean isInIntRange(Long longValue) {
		return longValue.longValue() == longValue.intValue();
	}

	public static <T> List<T> filterList(Predicate<T> selector, Collection<T> objects) {
		List<T> result = new ArrayList<T>();
		for (T object : objects) {
			if (selector.apply(object)) {
				result.add(object);
			}
		}
		return result;
	}
}
