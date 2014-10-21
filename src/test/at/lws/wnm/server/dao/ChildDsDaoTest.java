package at.lws.wnm.server.dao;

import java.util.Arrays;
import java.util.Collections;
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
	private static final long YEAR = 365 * 24 * 60 * 60 * 1000;
	private static final Date DIALOGUE_DATE1 = new Date(
			System.currentTimeMillis() - YEAR);
	private static final Date DIALOGUE_DATE2 = new Date(
			System.currentTimeMillis() - 2 * YEAR);
	private GwtChild child;

	private ChildDsDao childDao;

	@Before
	public void setUp() {
		child = TestUtils.createGwtChild(null, CHILD_FIRSTN, CHILD_LASTN,
				CHILD_BIRTHDAY, DIALOGUE_DATE1, DIALOGUE_DATE2);
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
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY,
				DIALOGUE_DATE1, DIALOGUE_DATE2);

		// read all
		final List<GwtChild> allChildren = childDao.getAllChildren();
		Assert.assertEquals(1, allChildren.size());

		// update
		final String updatedFirstName = "XYZ";
		child.setFirstName(updatedFirstName);
		childDao.storeChild(child);
		storedChild = childDao.getChild(child.getKey());
		Assert.assertNotNull(storedChild);
		assertChild(storedChild, updatedFirstName, CHILD_LASTN, CHILD_BIRTHDAY,
				DIALOGUE_DATE1, DIALOGUE_DATE2);
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
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY,
				DIALOGUE_DATE1, DIALOGUE_DATE2);

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
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY,
				DIALOGUE_DATE1, DIALOGUE_DATE2);

		removeFromCache(key);

		childDao.deleteChild(child);
		assertServicesContainsNot(key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return ChildDsDao.CHILD_DAO_MEMCACHE;
	}

	private void assertChild(GwtChild child, String expectedFn,
			String expectedLn, Date expectedBd, Date... dialogueDates) {
		Assert.assertEquals(expectedFn, child.getFirstName());
		Assert.assertEquals(expectedLn, child.getLastName());
		Assert.assertEquals(expectedBd, child.getBirthDay());
		List<Date> dialogueDateList = Arrays.asList(dialogueDates);
		Collections.sort(dialogueDateList);
		for (int i = 0; i < dialogueDateList.size(); i++) {
			Assert.assertEquals(dialogueDateList.get(i), child
					.getDevelopementDialogueDates().get(i));
		}
	}

}
