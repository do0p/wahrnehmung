package at.brandl.lws.notice.interaction.dao;

import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Child;

public class InteractionDsDaoTest {

	private String childKey1;
	private String childKey2;
	private static final Integer COUNT1 = 1;
	private static final Date DATE1 = new Date();
	private static final Integer COUNT2 = 2;
	private static final Date DATE2 = new Date(System.currentTimeMillis() + 86400000);
	private static final Integer COUNT3 = 3;
	private InteractionDsDao interactionDao;

	private LocalServiceTestHelper helper;
	private DatastoreService datastore;
	private String childKey3;

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
		childKey1 = DsUtil.toString(datastore.put(new Entity(Child.KIND)));
		childKey2 = DsUtil.toString(datastore.put(new Entity(Child.KIND)));
		childKey3 = DsUtil.toString(datastore.put(new Entity(Child.KIND)));
	}

	@After
	public void end() {
		helper.tearDown();
	}

	@Test
	public void getInteractionsForNull() {

		Map<String, Integer> interactions = interactionDao.getInteractions(null, null, null);
		Assert.assertNotNull(interactions);
		Assert.assertEquals(0, interactions.size());
	}

	@Test
	public void storeInteraction() {
		
		interactionDao.storeInteraction(childKey1, childKey2, DATE1, COUNT1);
		interactionDao.storeInteraction(childKey1, childKey2, DATE2, COUNT2);
		interactionDao.storeInteraction(childKey1, childKey2, DATE2, COUNT3);
		interactionDao.storeInteraction(childKey1, childKey3, DATE1, COUNT3);

		// without filter
		Map<String, Integer> interactions = interactionDao.getInteractions(childKey1, null, null);
		assertInteraction(interactions, childKey2, COUNT1 + COUNT2 + COUNT3);
		assertInteraction(interactions, childKey3, COUNT3);
		interactions = interactionDao.getInteractions(childKey2, null, null);
		assertInteraction(interactions, childKey1, COUNT1 + COUNT2 + COUNT3);
		interactions = interactionDao.getInteractions(childKey3, null, null);
		assertInteraction(interactions, childKey1,  COUNT3);
		
		// with date filter
		interactions = interactionDao.getInteractions(childKey1, null, DATE1);
		assertInteraction(interactions, childKey2, COUNT1 );
		interactions = interactionDao.getInteractions(childKey1, DATE1, DATE1);
		assertInteraction(interactions, childKey2, COUNT1 );
		interactions = interactionDao.getInteractions(childKey1, DATE2, null);
		assertInteraction(interactions, childKey2, COUNT2 +  COUNT3 );
	}

	private void assertInteraction(Map<String, Integer> interactions, String partnerKey,
			Integer count) {

		Assert.assertNotNull(interactions);
		Assert.assertEquals(count, interactions.get(partnerKey));
	}
}
