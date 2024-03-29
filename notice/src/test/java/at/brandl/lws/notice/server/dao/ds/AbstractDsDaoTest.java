package at.brandl.lws.notice.server.dao.ds;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public abstract class AbstractDsDaoTest {

	private LocalServiceTestHelper helper;
	private MemcacheService cache;
	private DatastoreService datastore;

	protected DatastoreService getDatastore() {
		return datastore;
	}

	@Before
	public void begin() {
		helper = new LocalServiceTestHelper(createDsConfig(),
				new LocalMemcacheServiceTestConfig());
		helper.setUp();
		cache = MemcacheServiceFactory
				.getMemcacheService(getMemCacheServiceName());
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	protected LocalDatastoreServiceTestConfig createDsConfig() {
		return new LocalDatastoreServiceTestConfig();
	}

	@After
	public void end() {
		helper.tearDown();
	}

	protected abstract String getMemCacheServiceName();

	protected void assertCacheEqualsDataStore(String key) {
		final Entity cacheObj = (Entity) cache.get(toKey(key));
		try {
			final Entity dsObj = datastore.get(toKey(key));
			Assert.assertEquals(dsObj, cacheObj);
			Assert.assertEquals(dsObj.getProperties(), cacheObj.getProperties());
		} catch (EntityNotFoundException e) {
			Assert.assertNull(cacheObj);
		}
	}

	protected void clearCache() {
		cache.clearAll();
	}
	
	protected void removeFromCache(String key) {
		cache.delete(toKey(key));
	}

	protected void removeFromCacheStringKey(String key) {
		cache.delete(key);
	}

	protected void removeFromDatastore(String key) {
		datastore.delete(toKey(key));
	}

	protected void assertCacheContains(String key) {
		Assert.assertNotNull(cache.get(toKey(key)));
	}

	protected void assertCacheContainsStringKey(String key) {
		Assert.assertNotNull(cache.get(key));
	}

	protected void assertDatastoreContains(String key) {
		try {
			Assert.assertNotNull(datastore.get(toKey(key)));
		} catch (EntityNotFoundException e) {
			Assert.fail("datastore does not contain " + key);
		}
	}

	protected void assertCacheContainsNot(String key) {
		Assert.assertNull(cache.get(toKey(key)));
	}

	protected void assertCacheContainsNotStringKey(String key) {
		Assert.assertNull(cache.get(key));
	}

	protected Object getCacheEntry(String key) {
		return cache.get(key);
	}
	
	protected void putCacheEntry(String key, Object object) {
		cache.put(key, object);
	}
	
	protected void assertDatastoreContainsNot(String key) {
		try {
			Assert.assertNull(datastore.get(toKey(key)));
		} catch (EntityNotFoundException e) {
			// success
		}
	}

	protected Key toKey(String key) {
		return KeyFactory.stringToKey(key);
	}

	protected void assertServicesContainsNot(final String key) {
		assertCacheContainsNot(key);
		assertDatastoreContainsNot(key);
	}

	protected void assertServicesContains(final String key) {
		assertDatastoreContains(key);
		assertCacheContains(key);
		assertCacheEqualsDataStore(key);
	}

	protected void insertIntoDatastore(Entity entity) {
		datastore.put(entity);
	}

}
