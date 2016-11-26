package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.TestUtils.createBeobachtung;
import static at.brandl.lws.notice.TestUtils.createBeobachtungEntity;
import static at.brandl.lws.notice.TestUtils.createChildEntity;
import static at.brandl.lws.notice.TestUtils.createFilterWithSummaries;
import static at.brandl.lws.notice.TestUtils.createSectionEntity;
import static at.brandl.lws.notice.TestUtils.createUser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.TestUtils;
import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.gwt.view.client.Range;

public class BeobachtungDsDaoWithSummariesTest extends AbstractDsDaoTest {

	private static final long NOW = System.currentTimeMillis();
	private static final String USER1 = "test1@example.com";
	private static final String USER2 = "test2@example.com";
	private static final String SECTION_NAME1 = "Gewerkschaft";
	private static final String SECTION_NAME2 = "Regierung";
	private static final String SECTION_NAME3 = "Organisationen";
	private static final String FIRST_NAME1 = "Fritz";
	private static final String LAST_NAME1 = "Neugebauer";
	private static final String FIRST_NAME2 = "Franz";
	private static final String LAST_NAME2 = "Altgebauer";
	private static final long HOUR = 3600000;

	private String child1Key;
	private String child2Key;
	private String section1Key;
	private String section2Key;
	private String section3Key;
	private String section4Key;
	private String section5Key;

	private BeobachtungDsDao beobachtungsDao;

	private User user1;
	private User user2;
	private Range range;
	private GwtBeobachtung beobachtung1;
	private GwtBeobachtung beobachtung2;
	private GwtBeobachtung beobachtung3;
	private GwtBeobachtung beobachtung4;
	private GwtBeobachtung beobachtung5;

	@Before
	public void setUp() {

		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		user1 = createUser(USER1);
		user2 = createUser(USER2);
		range = new Range(0, 10);

		final Entity child1 = createChildEntity(FIRST_NAME1, LAST_NAME1);
		final Entity child2 = createChildEntity(FIRST_NAME2, LAST_NAME2);
		insertIntoDatastore(child1);
		insertIntoDatastore(child2);
		child1Key = TestUtils.toString(child1.getKey());
		child2Key = TestUtils.toString(child2.getKey());

		final Entity section3 = createSectionEntity(SECTION_NAME3, null);
		insertIntoDatastore(section3);
		section3Key = TestUtils.toString(section3.getKey());

		final Entity section1 = createSectionEntity(SECTION_NAME1, section3Key);
		final Entity section2 = createSectionEntity(SECTION_NAME2, section3Key);
		insertIntoDatastore(section1);
		insertIntoDatastore(section2);
		section1Key = TestUtils.toString(section1.getKey());
		section2Key = TestUtils.toString(section2.getKey());


		beobachtung1 = createBeobachtung(child1Key, section2Key, user1,
				new Date(NOW - 3 * HOUR), "beobachtung1");
		beobachtung2 = createBeobachtung(child1Key, section1Key, user2,
				new Date(NOW - 2 * HOUR), "beobachtung2");
		beobachtung3 = createBeobachtung(child1Key, section3Key, user1,
				new Date(NOW - HOUR), "beobachtung3");
		beobachtung4 = createBeobachtung(child2Key, section1Key, user1,
				new Date(NOW), "beobachtung4");

		final Entity beobachtung1Entity = createBeobachtungEntity(beobachtung1,
				user1);
		insertIntoDatastore(beobachtung1Entity);
		beobachtung1.setKey(TestUtils.toString(beobachtung1Entity.getKey()));
		insertIntoDatastore(createBeobachtungEntity(beobachtung2, user2));
		insertIntoDatastore(createBeobachtungEntity(beobachtung3, user1));
		insertIntoDatastore(createBeobachtungEntity(beobachtung4, user1));

		final Entity section4 = createSectionEntity(SECTION_NAME3, null);
		insertIntoDatastore(section4);
		section4Key = TestUtils.toString(section4.getKey());
		final Entity section5 = createSectionEntity(SECTION_NAME2, section4Key);
		insertIntoDatastore(section5);
		section5Key = TestUtils.toString(section5.getKey());
		beobachtung5 = createBeobachtung(child1Key, section5Key, user1,
				new Date(NOW - 100 * HOUR), "beobachtung5");
		beobachtung5.setArchived(true);
		insertIntoDatastore(createBeobachtungEntity(beobachtung5, user1));

	}

	@Test
	public void testDelete() {
		Assert.assertEquals(6, beobachtungsDao
				.getRowCount(createFilterWithSummaries(child1Key, null)));

		beobachtungsDao.deleteAllFromChild(child1Key.toString());

		Assert.assertEquals(0, beobachtungsDao
				.getRowCount(createFilterWithSummaries(child1Key, null)));
	}

	@Test
	public void testGetForKey() {
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao
				.getBeobachtungen(createFilterWithSummaries(child1Key, null),
						range);
		for (GwtBeobachtung beobachtung : beobachtungen) {
			if (beobachtung.getKey() != null) {
				Assert.assertEquals(beobachtung, beobachtungsDao
						.getBeobachtung(beobachtung.getKey(), false));
			}
		}
	}

	@Test
	public void searchInSecondLevelSection() {
		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setShowSummaries(true);
		filter.setSectionKey(section1Key);
		List<GwtBeobachtung> beobachtungen = beobachtungsDao.getBeobachtungen(
				filter, range);
		Assert.assertEquals(4, beobachtungen.size());
	}

	@Test
	public void testGetForSectionAggregate() {

		System.out.println("aggregate test");
		BeobachtungsFilter filter = createFilterWithSummaries(null, section3Key);
		filter.setOver12(true);
		filter.setUnder12(true);
		filter.setAggregateSectionEntries(true);
		List<GwtBeobachtung> beobachtungen = beobachtungsDao.getBeobachtungen(
				filter, range);
		System.out.println("aggregate test end");
		Assert.assertEquals(beobachtungen.toString(), 8, beobachtungen.size());
	}

	@Test
	public void testGetForFilter() {

		Assert.assertEquals(
				6,
				beobachtungsDao.getBeobachtungen(
						createFilterWithSummaries(child1Key, null), range)
						.size());
		Assert.assertEquals(
				2,
				beobachtungsDao.getBeobachtungen(
						createFilterWithSummaries(child1Key, section1Key),
						range).size());
		BeobachtungsFilter filter = createFilterWithSummaries(child1Key, null);
		filter.setUser(user1.getEmail());
		Assert.assertEquals(4, beobachtungsDao.getBeobachtungen(filter, range)
				.size());
		filter = createFilterWithSummaries(child1Key, section2Key);
		filter.setUser(user1.getEmail());
		Assert.assertEquals(2, beobachtungsDao.getBeobachtungen(filter, range)
				.size());

		Assert.assertEquals(
				2,
				beobachtungsDao.getBeobachtungen(
						createFilterWithSummaries(child1Key, null, new Date(NOW
								- HOUR), new Date(NOW)), range).size());
	}

	@Test
	public void testGetForSection() {
		Assert.assertTrue(beobachtungsDao.beobachtungenExist(
				Arrays.asList(section1Key, section2Key, section3Key),
				getDatastore()));
		Assert.assertTrue(beobachtungsDao.beobachtungenExist(
				Arrays.asList(section3Key), getDatastore()));

	}

	@Test
	public void testRange() {
		assertEquals(beobachtungsDao.getBeobachtungen(
				createFilterWithSummaries(child1Key, null), new Range(4, 2)),
				beobachtung2, beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(
				createFilterWithSummaries(child1Key, null), new Range(5, 10)),
				beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(
				createFilterWithSummaries(child1Key, null), new Range(3, 1)),
				beobachtung3);
	}

	@Test
	public void testRowCount() {
		BeobachtungsFilter filter = createFilterWithSummaries(child1Key, null);
		filter.setUser(user1.getEmail());
		Assert.assertEquals(4, beobachtungsDao.getRowCount(filter));
		Assert.assertEquals(6, beobachtungsDao
				.getRowCount(createFilterWithSummaries(child1Key, null)));
		Assert.assertEquals(2, beobachtungsDao
				.getRowCount(createFilterWithSummaries(child1Key, section1Key)));
		filter = createFilterWithSummaries(child1Key, section2Key);
		filter.setUser(user1.getEmail());
		Assert.assertEquals(2, beobachtungsDao.getRowCount(filter));
	}

	@Test
	public void testStoreBeobachtung() {
		final GwtBeobachtung beobachtung = createBeobachtung(child1Key,
				section1Key, user1, new Date(), "beobachtung");
		beobachtungsDao.storeBeobachtung(beobachtung, user1, null);
		assertServicesContains(beobachtung.getKey());

		final BeobachtungsFilter filter = createFilterWithSummaries(child1Key,
				section1Key);
		filter.setUser(user1.getEmail());
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao
				.getBeobachtungen(filter, range);
		Assert.assertEquals(2, beobachtungen.size());
		Assert.assertEquals(beobachtung, beobachtungen.get(1));
	}

	@Test
	public void testStoreBeobachtungTwoTimes() {
		final GwtBeobachtung beobachtung1 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW), "beobachtung1");
		final GwtBeobachtung beobachtung2 = createBeobachtung(child1Key,
				section1Key, user1, new Date(NOW + 1), "beobachtung2");

		beobachtungsDao.storeBeobachtung(beobachtung1, user1, null);
		assertServicesContains(beobachtung1.getKey());
		beobachtungsDao.storeBeobachtung(beobachtung2, user1, null);
		assertServicesContains(beobachtung2.getKey());

		final BeobachtungsFilter filter = createFilterWithSummaries(child1Key,
				section1Key);
		filter.setUser(user1.getEmail());
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao
				.getBeobachtungen(filter, range);
		Assert.assertEquals(3, beobachtungen.size());
		Assert.assertEquals(beobachtung1, beobachtungen.get(2));
		Assert.assertEquals(beobachtung2, beobachtungen.get(1));
	}

	@Test
	public void worksWithCache() {
		final String key = beobachtung1.getKey();

		GwtBeobachtung beobachtung = beobachtungsDao.getBeobachtung(key, false);
		assertServicesContains(key);
		Assert.assertNotNull(beobachtung);

		removeFromDatastore(key);

		beobachtung = beobachtungsDao.getBeobachtung(key, false);
		assertCacheContains(key);
		Assert.assertNotNull(beobachtung);

		removeFromCache(key);

		assertServicesContainsNot(key);
	}

	@Test
	public void worksWithoutCache() {
		final String key = beobachtung1.getKey();

		GwtBeobachtung beobachtung = beobachtungsDao.getBeobachtung(key, false);
		assertServicesContains(key);
		Assert.assertNotNull(beobachtung);

		removeFromCache(key);

		beobachtung = beobachtungsDao.getBeobachtung(key, false);
		assertServicesContains(key);
		Assert.assertNotNull(beobachtung);

		removeFromCache(key);

		beobachtungsDao.deleteBeobachtung(key);
		assertServicesContainsNot(key);
	}

	@Test
	public void getArchived() {
		BeobachtungsFilter filter = createFilterWithSummaries(child1Key, null);
		filter.setArchived(true);
		List<GwtBeobachtung> beobachtungen = beobachtungsDao.getBeobachtungen(
				filter, range);
		Assert.assertEquals(2, beobachtungen.size());
		Assert.assertEquals(beobachtungen.get(1), beobachtung5);
	}

	@Test
	public void getSinceLastDevelopementDialogue() {
		BeobachtungsFilter filter = createFilterWithSummaries(child1Key, null);
		filter.setSinceLastDevelopmementDialogue(true);
		List<GwtBeobachtung> beobachtungen = beobachtungsDao.getBeobachtungen(
				filter, range);
		Assert.assertEquals(7, beobachtungen.size());
	}

	@Override
	protected String getMemCacheServiceName() {
		return BeobachtungDsDao.getCacheName(false);
	}

	private void assertEquals(List<GwtBeobachtung> beobachtungen,
			GwtBeobachtung... beobachtung) {
		Assert.assertEquals(Arrays.asList(beobachtung), beobachtungen);
	}

}
