package at.brandl.lws.notice.service.dao;

import java.util.HashMap;
import java.util.Map;

public class DaoRegistry {

	private static final Map<Class<? extends AbstractDao>, AbstractDao> REGISTER = new HashMap<Class<? extends AbstractDao>, AbstractDao>();
	
	public static void register(AbstractDao dao) {
		if (REGISTER.containsKey(dao.getClass())) {
			throw new IllegalStateException("dao "
					+ dao.getClass().getCanonicalName() + " already registered");
		}
		REGISTER.put(dao.getClass(), dao);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractDao> T get(Class<T> clazz) {
		if (!REGISTER.containsKey(clazz)) {
			synchronized(DaoRegistry.class)
			{
				if (!REGISTER.containsKey(clazz)) {
					{
						try {
							 clazz.newInstance();
						} catch (Exception e) {
							throw new IllegalArgumentException("could not instantiate class " + clazz, e);
						} 
					}
				}
			}
		}
		return (T) REGISTER.get(clazz);
	}

}
