package at.brandl.lws.notice.server.dao.ds;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.brandl.lws.notice.TestUtils;
import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.shared.util.Constants.Child.Cache;

public class ChildDsDaoTest extends AbstractDsDaoTest {

	private static final String CHILD_FIRSTN = "Fránz";
	private static final String CHILD_LASTN = "Yößläer";
	private static final Date CHILD_BIRTHDAY = new Date();
	private static final long YEAR = 365 * 24 * 60 * 60 * 1000;
	private static final Date DIALOGUE_DATE1 = new Date(System.currentTimeMillis() - YEAR);
	private static final Date DIALOGUE_DATE2 = new Date(System.currentTimeMillis() - 2 * YEAR);
	private GwtChild child;

	private ChildDsDao childDao;

	@Before
	public void setUp() {
		child = TestUtils.createGwtChild(null, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, DIALOGUE_DATE1,
				DIALOGUE_DATE2);
		childDao = DaoRegistry.get(ChildDsDao.class);
	}

	@Test
	public void crud() {
		// create
		childDao.storeChild(child);
		String key = child.getKey();
		Assert.assertNotNull(key);

		// read
		GwtChild storedChild = childDao.getChild(child.getKey());
		Assert.assertNotNull(storedChild);
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, DIALOGUE_DATE1, DIALOGUE_DATE2);

		// read all
		List<GwtChild> allChildren = childDao.getAllChildren();
		Assert.assertEquals(1, allChildren.size());

		// update
		String updatedFirstName = "XYZ";
		child.setFirstName(updatedFirstName);
		childDao.storeChild(child);
		storedChild = childDao.getChild(child.getKey());
		Assert.assertNotNull(storedChild);
		assertChild(storedChild, updatedFirstName, CHILD_LASTN, CHILD_BIRTHDAY, DIALOGUE_DATE1, DIALOGUE_DATE2);

		// store duplicate
		child.setKey(null);
		try {
			childDao.storeChild(child);
			Assert.fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// success
		}
		child.setKey(key);

		// delete
		childDao.deleteChild(child.getKey());
		Assert.assertNull(childDao.getChild(child.getKey()));

	}

	@Test
	public void worksWithCache() {
		childDao.storeChild(child);
		String key = child.getKey();

		removeFromDatastore(key);

		GwtChild storedChild = childDao.getChild(key);
		Assert.assertNotNull(storedChild);
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, DIALOGUE_DATE1, DIALOGUE_DATE2);

	}

	@Test
	public void worksWithoutCache() {
		childDao.storeChild(child);
		String key = child.getKey();

		clearCache();

		GwtChild storedChild = childDao.getChild(key);
		Assert.assertNotNull(storedChild);
		assertChild(storedChild, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY, DIALOGUE_DATE1, DIALOGUE_DATE2);

	}

	@Test
	public void childrenUnderTwelve() {
		childDao.storeChild(child);
		childDao.storeChild(TestUtils.createGwtChild(null, "Franz", "Achner", nowBefore12Years()));

		List<GwtChild> allChildrenUnder12 = childDao.getAllChildrenUnder12();
		Assert.assertEquals(1, allChildrenUnder12.size());
		Assert.assertEquals(child.getKey(), allChildrenUnder12.get(0).getKey());
	}

	@Test
	public void childrenOverTwelve() {
		childDao.storeChild(child);
		GwtChild oldChild = TestUtils.createGwtChild(null, "Franz", "Achner", nowBefore12Years());
		childDao.storeChild(oldChild);

		List<GwtChild> children = childDao.getAllChildrenOver12();
		Assert.assertEquals(1, children.size());
		Assert.assertEquals(oldChild.getKey(), children.get(0).getKey());

	}

	private Date nowBefore12Years() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.roll(Calendar.YEAR, -12);
		return calendar.getTime();
	}

	@Test
	@Ignore("testframework does not work in threading mode")
	public void storeRaceCondition() throws InterruptedException {

		int numThreads = 5;
		final CountDownLatch countDown = new CountDownLatch(numThreads);
		final AtomicInteger errorCount = new AtomicInteger(0);
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread() {
				@Override
				public void run() {
					try {
						childDao.storeChild(TestUtils.createGwtChild(null, CHILD_FIRSTN, CHILD_LASTN, CHILD_BIRTHDAY));
					} catch(Throwable e) {
						errorCount.incrementAndGet();
						e.printStackTrace();
					} finally {
						countDown.countDown();
					}
				}
			};
		}

		for (int i = 0; i < numThreads; i++) {
			threads[i].start();
		}
		
		countDown.await();

		Assert.assertEquals(numThreads - 1, errorCount.get());
		Assert.assertEquals(1, childDao.getAllChildren().size());
	}

	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	private void assertChild(GwtChild child, String expectedFn, String expectedLn, Date expectedBd,
			Date... dialogueDates) {
		Assert.assertEquals(expectedFn, child.getFirstName());
		Assert.assertEquals(expectedLn, child.getLastName());
		Assert.assertEquals(expectedBd, child.getBirthDay());
		List<Date> dialogueDateList = Arrays.asList(dialogueDates);
		Collections.sort(dialogueDateList);
		for (int i = 0; i < dialogueDateList.size(); i++) {
			Assert.assertEquals(dialogueDateList.get(i), child.getDevelopementDialogueDates().get(i));
		}
	}

}
