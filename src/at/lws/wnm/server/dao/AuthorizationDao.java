package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Authorization;

import com.google.appengine.api.users.User;

public class AuthorizationDao extends AbstractDao {

	private static final Set<String> SUPER_USER_IDS = new HashSet<String>();
	static {
		SUPER_USER_IDS.add("dbrandl72@gmail.com");
	}

	private volatile boolean needCacheUpdate = true;
	private Map<String, Authorization> cache = new HashMap<String, Authorization>();

	AuthorizationDao() {
		initCache();
	}

	public Authorization getAuthorization(User user) {
		refreshCache();
		return cache.get(createUserId(user));
	}

	public boolean isAuthorized(User user) {
		refreshCache();
		return cache.containsKey(createUserId(user));
	}

	public Collection<Authorization> queryAuthorizations() {
		refreshCache();
		return new ArrayList<Authorization>(cache.values());
	}

	public void storeAuthorization(Authorization aut) {
		aut.setUserId(aut.getEmail().toLowerCase());
		final EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(aut);
			needCacheUpdate = true;
		} finally {
			em.close();
		}
	}

	public void deleteAuthorization(String email) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em
					.createQuery("delete from Authorization a where a.userId = :userId");
			query.setParameter("userId", email.toLowerCase());
			query.executeUpdate();
			needCacheUpdate = true;
		} finally {
			em.close();
		}
	}

	private Authorization createSuperUser(String email) {
		final Authorization superUser = new Authorization();
		superUser.setUserId(email);
		superUser.setEmail(email);
		superUser.setAdmin(true);
		superUser.setSeeAll(false);
		return superUser;
	}

	private void initCache() {
		updateCache();
	}

	private void refreshCache() {
		if (needCacheUpdate) {
			synchronized (this) {
				if (needCacheUpdate) {
					updateCache();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateCache() {
		final Map<String, Authorization> tmpCache = new HashMap<String, Authorization>();
		for (String superUserId : SUPER_USER_IDS) {
			tmpCache.put(superUserId, createSuperUser(superUserId));
		}
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from Authorization");
			for (Authorization auth : (List<Authorization>) query
					.getResultList()) {
				tmpCache.put(auth.getUserId(), auth);
			}
		} finally {
			em.close();
		}
		cache = tmpCache;
		needCacheUpdate = false;
	}

	private String createUserId(User user) {
		return user == null ? null : user.getEmail().toLowerCase();
	}

}
