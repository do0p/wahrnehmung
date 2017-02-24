package at.brandl.lws.notice.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class CacheUtil {

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
				}
			}

			if (object != null) {
				allObjects.add(object);
				Collections.sort(allObjects);
			}

			success = cache.putIfUntouched(cacheKey, value, allObjects);
		}
	}

	public static <T extends Comparable<T>> void removeFromCachedResult(String cacheKey, Predicate<T> equals,
			MemcacheService cache) {

		updateCachedResult(cacheKey, null, equals, cache);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getCached(String key, Supplier<T> supplier, Object lock, MemcacheService cache) {
		T object = (T) cache.get(key);
		if (object == null) {
			synchronized (lock) {
				object = (T) cache.get(key);
				if (object == null) {
					object = supplier.get();
					if (object != null) {
						cache.put(key, object);
					}
				}
			}
		}
		return object;
	}

	public static <T extends Comparable<T>> T getFirstFromCachedList(Predicate<T> selector, Supplier<T> entitySupplier,
			String listCacheKey, Supplier<List<T>> entityListSupplier, Object lock, MemcacheService cache) {

		List<T> objects = getCached(listCacheKey, entityListSupplier, lock, cache);
		for (T object : objects) {
			if (selector.apply(object)) {
				return object;
			}
		}

		T object = null;
		if (entitySupplier != null) {
			object = entitySupplier.get();
			if (object != null) {
				updateCachedResult(listCacheKey, object, selector, cache);
			}
		}
		return object;
	}

	public static <T extends Comparable<T>> List<T> getAllFromCachedList(Predicate<T> selector, 
			String listCacheKey, Supplier<List<T>> entityListSupplier, Object lock, MemcacheService cache) {

		List<T> result = new ArrayList<T>();
		List<T> objects = getCached(listCacheKey, entityListSupplier, lock, cache);
		for (T object : objects) {
			if (selector.apply(object)) {
				result.add(object);
			}
		}
		return result;
	}
}
