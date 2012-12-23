package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Authorization;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;

public class AuthorizationDao extends AbstractDao {
	private static final Set<String> SUPER_USER_IDS = new HashSet<String>();

	private final MemcacheService cache = MemcacheServiceFactory
			.getMemcacheService("authDao");

	static {
		SUPER_USER_IDS.add("dbrandl72@gmail.com");
	}

	AuthorizationDao() {
		initCache();
	}

	public Authorization getAuthorization(User user) {
		if (user == null) {
			return null;
		}
		String userId = createUserId(user);
		Authorization authorization = (Authorization) this.cache.get(userId);
		if (authorization == null) {
			if (SUPER_USER_IDS.contains(userId)) {
				authorization = createSuperUser(userId);
			} else {
				EntityManager em = EMF.get().createEntityManager();
				try {
					authorization = (Authorization) em.find(
							Authorization.class, userId);
				} finally {
					em.close();
				}
			}
			this.cache.put(userId, authorization);
		}
		return authorization;
	}

	public Collection<Authorization> queryAuthorizations() {
		return new ArrayList<Authorization>(queryAuthInternal().values());
	}

	public void storeAuthorization(Authorization aut) {
		aut.setUserId(createUserId(aut.getEmail()));
		this.cache.put(aut.getUserId(), aut);
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(aut);
		} finally {
			em.close();
		}
	}

	public void deleteAuthorization(String email) {
		String userId = createUserId(email);
		this.cache.delete(userId);
		EntityManager em = EMF.get().createEntityManager();
		try {
			Query query = em
					.createQuery("delete from Authorization a where a.userId = :userId");
			query.setParameter("userId", userId);
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

	private String createUserId(String email) {
		return email.toLowerCase();
	}

	private Authorization createSuperUser(String email) {
		Authorization superUser = new Authorization();
		superUser.setUserId(email);
		superUser.setEmail(email);
		superUser.setAdmin(true);
		superUser.setSeeAll(false);
		return superUser;
	}

	private void initCache() {
		this.cache.putAll(queryAuthInternal());
	}

	private Map<String, Authorization> queryAuthInternal() {
		Map<String,Authorization> tmpCache = new HashMap<String,Authorization>();
		for (String superUserId : SUPER_USER_IDS) {
			tmpCache.put(superUserId, createSuperUser(superUserId));
		}
		EntityManager em = EMF.get().createEntityManager();
		try {
			Query query = em.createQuery("select from Authorization");

			@SuppressWarnings("unchecked")
			Iterator<Authorization> localIterator2 = query.getResultList().iterator();

			while (localIterator2.hasNext()) {
				Authorization auth = (Authorization) localIterator2.next();
				tmpCache.put(auth.getUserId(), auth);
			}
		} finally {
			em.close();
		}
		return tmpCache;
	}

	private String createUserId(User user) {
		return user == null ? null : user.getEmail().toLowerCase();
	}
}