package at.brandl.lws.notice.interaction.dao;

import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Child;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class InteractionDsDaoTest {

	private String key;
	private String keyOther;
	private static final Integer COUNT = 0;
	private static final Date DATE = new Date();
	private InteractionDsDao interactionDao;
	
	
	private LocalServiceTestHelper helper;
	private DatastoreService datastore;

	protected DatastoreService getDatastore() {
		return datastore;
	}

	@Before
	public void begin() {
		helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
				new LocalMemcacheServiceTestConfig());
		helper.setUp();
		datastore = DatastoreServiceFactory.getDatastoreService();
		interactionDao = DaoRegistry.get(InteractionDsDao.class);
		key = DsUtil.toString(datastore.put(new Entity(Child.KIND)));
		keyOther = DsUtil.toString(datastore.put(new Entity(Child.KIND)));
	}

	@After
	public void end() {
		helper.tearDown();
	}
	

	@Test
	public void getInteractionsForNull() {
		Map<String, Integer> interactions = interactionDao.getInteractions(null);
		Assert.assertNotNull(interactions);
		Assert.assertEquals(0, interactions.size());
	}

	@Test
	public void storeInteraction() {
		interactionDao.storeInteraction(key, keyOther, COUNT, DATE);
		
		Map<String, Integer> interactions = interactionDao.getInteractions(keyOther);
		Assert.assertNotNull(interactions);
		Assert.assertEquals(0, interactions.size());
		
		interactions = interactionDao.getInteractions(key);
		Assert.assertNotNull(interactions);
		Assert.assertEquals(1, interactions.size());
		Assert.assertEquals(COUNT, interactions.get(keyOther));
	}
	
	@After
	public void tearDown() throws Exception {
	}
}
