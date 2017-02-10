package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.toGwtAuthorization;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.KIND;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.common.base.Supplier;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.util.Constants.Authorization;
import at.brandl.lws.notice.shared.util.Constants.Authorization.Cache;
import at.brandl.lws.notice.shared.util.Constants.Authorization.Selector;

public class AuthorizationDsDao extends AbstractDsDao {

	public static class GwtAuthorizationSupplier implements Supplier<GwtAuthorization> {

		private final Key key;

		public GwtAuthorizationSupplier(String userId) {
			key = KeyFactory.createKey(KIND, userId);
		}

		@Override
		public GwtAuthorization get() {
			try {
				Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
				return toGwtAuthorization(entity);
			} catch (EntityNotFoundException e) {
				return null;
			}
		}
	};

	public static class GwtAuthorizationListSupplier implements Supplier<List<GwtAuthorization>> {

		@Override
		public List<GwtAuthorization> get() {
			List<GwtAuthorization> result = new ArrayList<GwtAuthorization>();
			Iterable<Entity> entities = DatastoreServiceFactory.getDatastoreService().prepare(new Query(KIND))
					.asIterable(withDefaults());
			for (Entity entity : entities) {
				result.add(toGwtAuthorization(entity));
			}
			Collections.sort(result);
			return result;
		}
	};

	public GwtAuthorization getAuthorization(User user) {

		if (user == null) {
			return null;
		}

		String userId = user.getEmail().toLowerCase();
		return getCached(userId, new GwtAuthorizationSupplier(userId));
	}

	public List<GwtAuthorization> queryAuthorizations() {

		return getCached(Cache.ALL_USERS, new GwtAuthorizationListSupplier());
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
			GwtAuthorization gwtAuthorization = toGwtAuthorization(authorization);
			addToCache(gwtAuthorization.getUserId(), gwtAuthorization);
			DsUtil.updateCachedResult(Cache.ALL_USERS, gwtAuthorization, new Selector(userId), getCache());

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

	private <T> T getCached(String key, Supplier<T> supplier) {
		Object lock = Authorization.class;
		T object = getFromCache(key);
		if (object == null) {
			synchronized (lock) {
				object = getFromCache(key);
				if (object == null) {
					object = supplier.get();
					addToCache(key, object);
				}
			}
		}
		return object;
	}

	private void removeFromCache(String userId) {
		getCache().delete(userId);
		DsUtil.removeFromCachedResult(Cache.ALL_USERS, new Selector(userId), getCache());
	}

	private void addToCache(String key, Object object) {
		getCache().put(key, object);
	}

	@SuppressWarnings("unchecked")
	private <T> T getFromCache(String key) {
		return (T) getCache().get(key);
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

}