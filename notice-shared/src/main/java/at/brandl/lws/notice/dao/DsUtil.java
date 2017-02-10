package at.brandl.lws.notice.dao;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
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



	public static <T extends Comparable<T>> void updateCachedResult(String cacheKey, T object, Predicate<T> selector,
			MemcacheService cache) {
	
		boolean success = false;
		while (!success) {
	
			IdentifiableValue value = cache.getIdentifiable(cacheKey);
			if (value == null) {
				break;
			}
	
			@SuppressWarnings("unchecked")
			List<T> allObjects = (List<T>) value.getValue();
			Iterator<T> iterator = allObjects.iterator();
			while (iterator.hasNext()) {
				T cachedObject = iterator.next();
				if (selector.apply(cachedObject)) {
					iterator.remove();
					break;
				}
			}
	
			if (object != null) {
				allObjects.add(object);
				Collections.sort(allObjects);
			}
	
			success = cache.putIfUntouched(cacheKey, value, allObjects);
		}
	}

	public static <T extends Comparable<T>> void removeFromCachedResult(String cacheKey, Predicate<T> equals, MemcacheService cache) {
	
		updateCachedResult(cacheKey, null, equals, cache);
	}


	private static boolean isInIntRange(Long longValue) {
		return longValue.longValue() == longValue.intValue();
	}
}
