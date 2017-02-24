package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.getEntityConverter;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.getStringToKeyConverter;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.toEntity;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.KIND;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.common.base.Function;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.CacheUtil;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.server.dao.ds.converter.GwtAuthorizationConverter.Selector;
import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.util.Constants.Authorization;
import at.brandl.lws.notice.shared.util.Constants.Authorization.Cache;

public class AuthorizationDsDao extends AbstractDsDao {

	private static final Function<String, Key> STRING_TO_KEY_CONVERTER = getStringToKeyConverter();
	private static final Function<Entity, GwtAuthorization> ENTITY_CONVERTER = getEntityConverter();
	private static final EntityListSupplier<GwtAuthorization> USERLIST_SUPPLIER = new EntityListSupplier<GwtAuthorization>(
			new Query(KIND), ENTITY_CONVERTER);

	public GwtAuthorization getAuthorization(User user) {

		if (user == null) {
			return null;
		}

		String userId = createUserId(user.getEmail());
		return getCachedUser(userId);
	}

	public List<GwtAuthorization> queryAuthorizations() {

		return getCachedUserList();
	}

	public void storeAuthorization(GwtAuthorization authorization) {

		assertCacheIsLoaded();
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction(TransactionOptions.Builder.withXG(true));

		try {

			String userId = createUserId(authorization.getEmail());
			if (Utils.isNotEmpty(authorization.getUserId()) && !authorization.getUserId().equals(userId)) {
				getDatastoreService().delete(STRING_TO_KEY_CONVERTER.apply(authorization.getUserId()));
				CacheUtil.removeFromCachedResult(Cache.ALL_USERS, new Selector(authorization.getUserId()), getCache());
			}

			authorization.setUserId(userId);
			Entity entity = toEntity(authorization);
			datastoreService.put(entity);
			transaction.commit();

			authorization.setKey(KeyFactory.keyToString(entity.getKey()));

			CacheUtil.updateCachedResult(Cache.ALL_USERS, authorization, new Selector(userId), getCache());

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
				getCache().clearAll();
			}
		}
	}

	public void deleteAuthorization(String email) {

		String userId = createUserId(email);
		assertCacheIsLoaded();
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();

		try {
			datastoreService.delete(KeyFactory.createKey(KIND, userId));
			transaction.commit();
			CacheUtil.removeFromCachedResult(Cache.ALL_USERS, new Selector(userId), getCache());
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private List<GwtAuthorization> getCachedUserList() {
		return CacheUtil.getCached(Cache.ALL_USERS, USERLIST_SUPPLIER, Authorization.class, getCache());
	}

	private GwtAuthorization getCachedUser(String userId) {
		return CacheUtil.getFirstFromCachedList(new Selector(userId),
				new EntitySupplier<GwtAuthorization>(STRING_TO_KEY_CONVERTER.apply(userId), ENTITY_CONVERTER),
				Cache.ALL_USERS, USERLIST_SUPPLIER, Authorization.class, getCache());
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

	private void assertCacheIsLoaded() {
		getCachedUserList();
	}

	private String createUserId(String email) {
		return email.toLowerCase();
	}

}