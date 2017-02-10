package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.shared.util.Constants.Authorization.ADMIN;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EDIT_DIALOGUE_DATES;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EDIT_SECTIONS;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EMAIL;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.KIND;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.SEE_ALL;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.USER_ID;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.common.base.Predicate;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.util.Constants.Authorization.Cache;

public class AuthorizationDsDao extends AbstractDsDao {

	private static class UserEquals implements Predicate<GwtAuthorization> {

		private final String userId;

		private UserEquals(String userId) {
			this.userId = userId;
		}

		@Override
		public boolean apply(GwtAuthorization user) {
			return userId.equals(user.getUserId());
		}

	}

	private static final Object LOCK = new Object();

	public GwtAuthorization getAuthorization(User user) {

		if (user == null) {
			return null;
		}

		String userId = user.getEmail().toLowerCase();
		GwtAuthorization authorization = getUserFromCache(userId);
		if (authorization == null) {
			synchronized (LOCK) {
				authorization = getUserFromCache(userId);
				if (authorization == null) {
					try {
						Key key = KeyFactory.createKey(KIND, userId);
						Entity entity = getDatastoreService().get(key);
						authorization = toGwt(entity);
						addUserToCache(authorization);
					} catch (EntityNotFoundException e) {
						authorization = null;
					}
				}
			}
		}
		return authorization;
	}

	public List<GwtAuthorization> queryAuthorizations() {

		List<GwtAuthorization> result = getUserListFromCache();
		if (result == null) {
			synchronized (LOCK) {
				result = getUserListFromCache();
				if (result == null) {
					result = new ArrayList<GwtAuthorization>();
					for (Entity entity : execute(new Query(KIND), withDefaults())) {
						result.add(toGwt(entity));
					}
					Collections.sort(result);
					addUserListToCache(result);
				}
			}
		}
		return result;
	}

	public void storeAuthorization(GwtAuthorization aut) {

		final String userId = createUserId(aut.getEmail());
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction(TransactionOptions.Builder.withXG(true));

		try {
			if (Utils.isNotEmpty(aut.getUserId()) && !aut.getUserId().equals(userId)) {
				Key key = KeyFactory.createKey(KIND, aut.getUserId());
				getDatastoreService().delete(key);
				removeFromCache(aut.getUserId());
			}

			aut.setUserId(userId);
			Entity authorization = toEntity(aut);
			aut.setKey(KeyFactory.keyToString(authorization.getKey()));
			getDatastoreService().put(authorization);
			transaction.commit();
			GwtAuthorization gwtAuthorization = toGwt(authorization);
			addUserToCache(gwtAuthorization);
			DsUtil.updateCachedResult(Cache.ALL_USERS, gwtAuthorization, new UserEquals(userId), getCache());

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
				getCache().clearAll();
			}
		}
	}



	public void deleteAuthorization(String email) {
		String userId = createUserId(email);
		Key key = KeyFactory.createKey(KIND, userId);
		getDatastoreService().delete(key);
		removeFromCache(userId);
	}

	private String createUserId(String email) {
		return email.toLowerCase();
	}

	private void removeFromCache(String userId) {
		getCache().delete(userId);
		DsUtil.removeFromCachedResult(Cache.ALL_USERS, new UserEquals(userId), getCache());
	}
	
	private void addUserListToCache(List<GwtAuthorization> result) {
		getCache().put(Cache.ALL_USERS, result);
	}

	@SuppressWarnings("unchecked")
	private List<GwtAuthorization> getUserListFromCache() {
		return (List<GwtAuthorization>) getCache().get(Cache.ALL_USERS);
	}

	private void addUserToCache(GwtAuthorization authorization) {
		getCache().put(authorization.getUserId(), authorization);
	}

	private GwtAuthorization getUserFromCache(String userId) {
		return (GwtAuthorization) getCache().get(userId);
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

	private GwtAuthorization toGwt(Entity entity) {
		if (entity == null) {
			return null;
		}
		final GwtAuthorization authorization = new GwtAuthorization();
		authorization.setKey(KeyFactory.keyToString(entity.getKey()));
		authorization.setUserId((String) entity.getProperty(USER_ID));
		authorization.setEmail((String) entity.getProperty(EMAIL));
		authorization.setAdmin((Boolean) entity.getProperty(ADMIN));
		authorization.setEditSections((Boolean) entity.getProperty(EDIT_SECTIONS));
		authorization.setEditDialogueDates((Boolean) entity.getProperty(EDIT_DIALOGUE_DATES));
		authorization.setSeeAll((Boolean) entity.getProperty(SEE_ALL));
		return authorization;
	}

	private Entity toEntity(GwtAuthorization aut) {
		final Key key = KeyFactory.createKey(KIND, aut.getUserId());
		final Entity authorization = new Entity(key);
		authorization.setProperty(USER_ID, aut.getUserId());
		authorization.setProperty(EMAIL, aut.getEmail());
		authorization.setProperty(ADMIN, aut.isAdmin());
		authorization.setProperty(EDIT_SECTIONS, aut.isEditSections());
		authorization.setProperty(EDIT_DIALOGUE_DATES, aut.isEditDialogueDates());
		authorization.setProperty(SEE_ALL, aut.isSeeAll());
		return authorization;
	}

}