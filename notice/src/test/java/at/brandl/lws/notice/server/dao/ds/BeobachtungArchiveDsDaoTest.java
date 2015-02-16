package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.TestUtils.createBeobachtung;
import static at.brandl.lws.notice.TestUtils.createChildEntity;
import static at.brandl.lws.notice.TestUtils.createSectionEntity;
import static at.brandl.lws.notice.TestUtils.createUser;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.TestUtils;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.server.dao.DaoRegistry;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.gwt.view.client.Range;

public class BeobachtungArchiveDsDaoTest extends AbstractDsDaoTest {

	private static final long NOW = System.currentTimeMillis();
	private static final String USER1 = "test1@example.com";
	private static final String SECTION_NAME1 = "Gewerkschaft";
	private static final String FIRST_NAME1 = "Fritz";
	private static final String LAST_NAME1 = "Neugebauer";
	private static final long HOUR = 3600000;

	private String child1Key;
	private String section1Key;
	private String section3Key;

	private BeobachtungDsDao beobachtungsDao;

	private User user1;
	private Range range;

	@Before
	public void setUp() {

		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		user1 = createUser(USER1);
		range = new Range(0, 10);

		final Entity child1 = createChildEntity(FIRST_NAME1, LAST_NAME1);
		insertIntoDatastore(child1);

		child1Key = TestUtils.toString(child1.getKey());
		final Entity section1 = createSectionEntity(SECTION_NAME1, section3Key);
		insertIntoDatastore(section1);
		section1Key = TestUtils.toString(section1.getKey());

	}

	@Test
	public void testArchiveNoEntries() {

		final GwtBeobachtung beobachtung1 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW), "beobachtung1");
		beobachtungsDao.storeBeobachtung(beobachtung1, user1, null);

		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(child1Key);

		assertFilterMatches(filter, 1);
		beobachtungsDao.moveAllToArchiveBefore(new Date(NOW - HOUR));
		assertFilterMatches(filter, 1);

		beobachtungsDao.deleteAllFromChild(child1Key);
	}

	@Test
	public void testArchive() {

		final GwtBeobachtung beobachtung1 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW - 2 * HOUR), "beobachtung1");
		beobachtungsDao.storeBeobachtung(beobachtung1, user1, null);

		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(child1Key);

		assertFilterMatches(filter, 1);
		beobachtungsDao.moveAllToArchiveBefore(new Date(NOW - HOUR));
		assertFilterMatches(filter, 0);

		filter.setArchived(true);
		assertFilterMatches(filter, 1);

		beobachtungsDao.deleteAllFromChild(child1Key);
	}

	@Test
	public void testArchiveWithGroups() {

		final GwtBeobachtung beobachtung1 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW - 2 * HOUR), "beobachtung1");
		final GwtBeobachtung beobachtung2 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW - 2 * HOUR), "beobachtung2");
		beobachtungsDao.storeBeobachtung(beobachtung1, user1, null);
		beobachtungsDao.storeBeobachtung(beobachtung2, user1,
				beobachtung1.getKey());

		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(child1Key);

		assertFilterMatches(filter, 2);
		beobachtungsDao.moveAllToArchiveBefore(new Date(NOW - HOUR));
		assertFilterMatches(filter, 0);

		filter.setArchived(true);
		assertFilterMatches(filter, 2);

		beobachtungsDao.deleteAllFromChild(child1Key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return BeobachtungDsDao.getCacheName(true);
	}

	@Override
	protected LocalDatastoreServiceTestConfig createDsConfig() {
		return super.createDsConfig()
				.setDefaultHighRepJobPolicyUnappliedJobPercentage(100);
	}

	private void assertFilterMatches(BeobachtungsFilter filter, int times) {
		Assert.assertEquals(times,
				beobachtungsDao.getBeobachtungen(filter, range).size());
	}

}
