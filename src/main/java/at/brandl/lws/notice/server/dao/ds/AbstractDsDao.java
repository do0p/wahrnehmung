package at.brandl.lws.notice.server.dao.ds;



import at.brandl.lws.notice.server.dao.AbstractDao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
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

	protected String toString(Key key) {
		return KeyFactory.keyToString(key);
	}

	protected Key toKey(String key) {
		return KeyFactory.stringToKey(key);
	}

	protected void insertIntoCache(Entity entity) {
		Key key = entity.getKey();
		getCache().put(key, entity);
	}

	protected void deleteFromCache(Key key) {
		getCache().delete(key);
	}

	protected Entity getFromCache(Key key) {
		return (Entity) getCache().get(key);
	}

	protected DatastoreService getDatastoreService() {
		return DatastoreServiceFactory.getDatastoreService();
	}

	protected Entity getCachedEntity(Key key) {
		Entity entity = getFromCache(key);
		if (entity == null) {
			entity = getEntity(key);
			insertIntoCache(entity);
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


	protected MemcacheService getCache() {
//		System.err.println("getting data from cache");
		return MemcacheServiceFactory
				.getMemcacheService(getMemcacheServiceName());
	}

	protected void deleteEntity(Key key) {
		deleteEntity(key, getDatastoreService());
	}
	
	protected void deleteEntity(Key key, DatastoreService datastoreService) {
		datastoreService.delete(key);
		deleteFromCache(key);
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
	
	protected abstract String getMemcacheServiceName();



}