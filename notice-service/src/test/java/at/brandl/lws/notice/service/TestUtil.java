package at.brandl.lws.notice.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class TestUtil {

	private static final String KIND = "test";
	private static int entityId = 1;

	public static Key key(String name) {
		return KeyFactory.createKey(KIND, name);
	}

	public static Object[] pair(String key, Object value) {
		return new Object[] { key, value };
	}

	public static Map<String, Object> values(Object[]... pairs) {
		Map<String, Object> values = new HashMap<String, Object>();
		for (Object[] pair : pairs) {
			values.put((String) pair[0], pair[1]);
		}
		return values;
	}

	public static Entity entity(Map<String, Object> values) {
		Entity entity = new Entity(KIND, entityId++);
		
		if (values != null) {
			for (Entry<String, Object> value : values.entrySet()) {
				entity.setProperty(value.getKey(), value.getValue());
			}
		}
		return entity;
	}

}
