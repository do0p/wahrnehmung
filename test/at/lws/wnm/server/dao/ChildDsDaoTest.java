package at.lws.wnm.server.dao;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.lws.wnm.TestUtils;
import at.lws.wnm.server.dao.ds.ChildDsDao;
import at.lws.wnm.shared.model.GwtChild;

public class ChildDsDaoTest extends AbstractDsDaoTest {

	private static final String CHILD_FIRSTN = "Fránz";
	private static final String CHILD_LASTN = "Yößläer";
	private static final Date CHILD_BIRTHDAY = new Date();
	private GwtChild child;

	private ChildDsDao childDao;

	@Before
	public void setUp() {
		child = TestUtils.createGwtChild(null, CHILD_FIRSTN, CHILD_LASTN,
				CHILD_BIRTHDAY);
		childDao = DaoRegistry.get(ChildDsDao.class);
	}

	@Test
	public void crud() {
		// create
		childDao.storeChild(child);
		final String key = child.getKey();
		Assert.assertNotNull(key);
		assertServicesContains(key);

		// read
		GwtChild storedChild = childDao.getChild(child.getKey());
		Assert.assertNotNull(storedChild);
		assertChild(CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, storedChild);

		// read all
		final List<GwtChild> allChildren = childDao.getAllChildren();
		Assert.assertEquals(1, allChildren.size());

		// update
		final String updatedFirstName = "XYZ";
		child.setFirstName(updatedFirstName);
		childDao.storeChild(child);
		storedChild = childDao.getChild(child.getKey());
		Assert.assertNotNull(storedChild);
		assertChild(updatedFirstName, CHILD_LASTN, CHILD_BIRTHDAY, storedChild);
		assertServicesContains(key);

		// store duplicate
		child.setKey(null);
		try {
			childDao.storeChild(child);
			Assert.fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// success
		}
		assertServicesContains(key);
		child.setKey(key);

		// delete
		childDao.deleteChild(child);

		assertServicesContainsNot(key);

	}

	@Test
	public void worksWithCache() {
		childDao.storeChild(child);
		final String key = child.getKey();

		assertServicesContains(key);

		removeFromDatastore(key);

		final GwtChild storedChild = childDao.getChild(key);
		assertCacheContains(key);
		Assert.assertNotNull(storedChild);
		assertChild(CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, storedChild);

		removeFromCache(key);

		assertServicesContainsNot(key);
	}

	@Test
	public void worksWithoutCache() {
		childDao.storeChild(child);
		final String key = child.getKey();
		assertServicesContains(key);

		removeFromCache(key);

		final GwtChild storedChild = childDao.getChild(key);
		assertServicesContains(key);
		Assert.assertNotNull(storedChild);
		assertChild(CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, storedChild);

		removeFromCache(key);

		childDao.deleteChild(child);
		assertServicesContainsNot(key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return ChildDsDao.CHILD_DAO_MEMCACHE;
	}

	private void assertChild(String expectedFn, String expectedLn,
			Date expectedBd, GwtChild storedChild) {
		Assert.assertEquals(expectedFn, storedChild.getFirstName());
		Assert.assertEquals(expectedLn, storedChild.getLastName());
		Assert.assertEquals(expectedBd, storedChild.getBirthDay());
	}

}
