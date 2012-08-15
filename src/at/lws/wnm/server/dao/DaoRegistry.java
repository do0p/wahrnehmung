package at.lws.wnm.server.dao;

import java.util.HashMap;
import java.util.Map;

public class DaoRegistry {

	private static final Map<Class<? extends AbstractDao>, AbstractDao> REGISTER = new HashMap<Class<? extends AbstractDao>, AbstractDao>();

	static
	{
		new AuthorizationDao();
		new BeobachtungDao();
		new ChildDao();
		new SectionDao();
	}
	
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
			throw new IllegalStateException("dao " + clazz.getCanonicalName()
					+ " not registered");
		}
		return (T) REGISTER.get(clazz);
	}

}
