package at.brandl.lws.notice.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public abstract class AbstractDsDao extends AbstractDao {

	
	

	protected Filter createEqualsPredicate(String fieldName, Entity entity) {
		return new Query.FilterPredicate(fieldName, FilterOperator.EQUAL,
				entity.getProperty(fieldName));
	}

	protected Filter createEqualsPredicate(String fieldName, Object value) {
		return new Query.FilterPredicate(fieldName, FilterOperator.EQUAL, value);
	}

	protected void insertIntoCache(Entity entity, String cacheName) {
		Key key = entity.getKey();
		getCache(cacheName).put(key, entity);
	}

	
	protected <T> void insertIntoCache(CacheKey cacheKey, T result, DatastoreService ds, Transaction tx,
			String cacheName) {
		getCache(cacheName).put(cacheKey,
				new CacheEntity<T>(result, getEntityGroupVersion(ds, tx, cacheKey.getKey())));
	}
	
	protected void deleteFromCache(Key key, String cacheName) {
		getCache(cacheName).delete(key);
	}

	protected Entity getFromCache(Key key, String cacheName) {
		return (Entity) getCache(cacheName).get(key);
	}

	protected <T> T getFromCache(CacheKey cacheKey, DatastoreService ds, String cacheName) {
		@SuppressWarnings("unchecked")
		CacheEntity<T> cacheEntity = (CacheEntity<T>) getCache(cacheName).get(cacheKey);

		if (cacheEntity != null && cacheEntity.getVersion() == getEntityGroupVersion(ds, null, cacheKey.getKey())) {
			return cacheEntity.getEntity();
		}

		return null;
	}
	

	private static long getEntityGroupVersion(DatastoreService ds, Transaction tx, Key entityKey) {
		try {
			return Entities.getVersionProperty(ds.get(tx, Entities.createEntityGroupKey(entityKey)));
		} catch (EntityNotFoundException e) {
			// No entity group information, return a value strictly smaller than
			// any
			// possible version
			return 0;
		}
	}
	
	protected DatastoreService getDatastoreService() {
		return DatastoreServiceFactory.getDatastoreService();
	}

	protected Entity getCachedEntity(Key key, String cacheName) {
		Entity entity = getFromCache(key, cacheName);
		if (entity == null) {
			entity = getEntity(key);
			insertIntoCache(entity, cacheName);
		}
		return entity;
	}

	protected Entity getEntity(Key key) {
		try {
			return getDatastoreService().get(key);
		} catch (EntityNotFoundException e) {
			throw new IllegalArgumentException("no entity with key " + key);
		}
	}

	protected MemcacheService getCache(String cacheName) {
		// System.err.println("getting data from cache");
		return MemcacheServiceFactory
				.getMemcacheService(cacheName);
	}

	protected void deleteEntity(Key key, String cacheName) {
		deleteEntity(key, getDatastoreService(), cacheName);
	}

	protected void deleteEntity(Key key, DatastoreService datastoreService, String cacheName) {
		datastoreService.delete(key);
		deleteFromCache(key, cacheName);
	}

	protected Iterable<Entity> execute(Query query, FetchOptions fetchOptions) {
		return execute(query, fetchOptions, getDatastoreService());
	}

	protected Iterable<Entity> execute(Query query, FetchOptions fetchOptions,
			final DatastoreService datastoreService) {
		return datastoreService.prepare(query).asIterable(fetchOptions);
	}

	protected int count(Query query, FetchOptions fetchOptions) {
		return count(query, fetchOptions, getDatastoreService());
	}

	protected int count(Query query, FetchOptions fetchOptions,
			DatastoreService datastoreService) {
		return datastoreService.prepare(query).countEntities(fetchOptions);
	}


}