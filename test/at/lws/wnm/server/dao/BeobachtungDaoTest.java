package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.lws.wnm.TestUtils;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.server.dao.EMF;
import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gwt.view.client.Range;

public class BeobachtungDaoTest {

	private static final LocalServiceTestHelper HELPER = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());
	private static final long NOW = System.currentTimeMillis();
	private static final String USER1 = "test1@example.com";
	private static final String USER2 = "test2@example.com";
	private static final Long SECTION_KEY1 = Long.valueOf(1);
	private static final Long SECTION_KEY2 = Long.valueOf(2);
	private static final Long SECTION_KEY3 = Long.valueOf(3);
	private static final String SECTION_NAME1 = "Gewerkschaft";
	private static final String SECTION_NAME2 = "Regierung";
	private static final String SECTION_NAME3 = "Organisationen";
	private static final Long CHILD_KEY1 = Long.valueOf(1);
	private static final String FIRST_NAME1 = "Fritz";
	private static final String LAST_NAME1 = "Neugebauer";
	private static final Long CHILD_KEY2 = Long.valueOf(2);
	private static final String FIRST_NAME2 = "Franz";
	private static final String LAST_NAME2 = "Altgebauer";
	private static final long HOUR = 3600000;

	private BeobachtungDao beobachtungsDao;

	private User user1;
	private User user2;
	private Range range;
	private EntityManager em;
	private EntityTransaction transaction;
	private Beobachtung beobachtung1;
	private Beobachtung beobachtung2;
	private Beobachtung beobachtung3;
	private Beobachtung beobachtung4;

	@Before
	public void setUp() {
		HELPER.setUp();
		em = EMF.get().createEntityManager();
		transaction = em.getTransaction();
		beobachtungsDao = DaoRegistry.get(BeobachtungDao.class);
		user1 = TestUtils.createUser(USER1);
		user2 = TestUtils.createUser(USER2);
		range = new Range(0, 10);
		persist(TestUtils.createChild(CHILD_KEY1, FIRST_NAME1, LAST_NAME1));
		persist(TestUtils.createChild(CHILD_KEY2, FIRST_NAME2, LAST_NAME2));
		persist(TestUtils.createSection(SECTION_KEY3, SECTION_NAME3, null));
		persist(TestUtils.createSection(SECTION_KEY1, SECTION_NAME1,
				SECTION_KEY3));
		persist(TestUtils.createSection(SECTION_KEY2, SECTION_NAME2,
				SECTION_KEY3));
		beobachtung1 = TestUtils.createBeobachtung(CHILD_KEY1, SECTION_KEY2,
				user1, new Date(NOW));
		beobachtung2 = TestUtils.createBeobachtung(CHILD_KEY1, SECTION_KEY1,
				user2, new Date(NOW + HOUR));
		beobachtung3 = TestUtils.createBeobachtung(CHILD_KEY1, SECTION_KEY3,
				user1, new Date(NOW + 2 * HOUR));
		beobachtung4 = TestUtils.createBeobachtung(CHILD_KEY2, SECTION_KEY1,
				user1, new Date(NOW + 3 * HOUR));
		persist(createBeobachtung(beobachtung1, user1));
		persist(createBeobachtung(beobachtung2, user2));
		persist(createBeobachtung(beobachtung3, user1));
		persist(createBeobachtung(beobachtung4, user1));
	}

	@After
	public void tearDown() {
		HELPER.tearDown();
	}

	@Test
	public void testDelete() {
		Assert.assertEquals(
				3,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(CHILD_KEY1, null), null));

		beobachtungsDao.deleteAllFromChild(CHILD_KEY1);

		Assert.assertEquals(
				0,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(CHILD_KEY1, null), null));
	}

	@Test
	public void testGetForKey() {
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao
				.getBeobachtungen(new BeobachtungsFilter(), range, null);
		for (GwtBeobachtung beobachtung : beobachtungen) {
			Assert.assertEquals(beobachtung,
					beobachtungsDao.getBeobachtung(beobachtung.getKey()));
		}
	}

	@Test
	public void testGetForFilter() {
		assertEquals(beobachtungsDao.getBeobachtungen(new BeobachtungsFilter(),
				range, null), beobachtung4, beobachtung3, beobachtung2,
				beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(new BeobachtungsFilter(),
				range, user1), beobachtung4, beobachtung3, beobachtung1);
		assertEquals(
				beobachtungsDao.getBeobachtungen(
						TestUtils.createFilter(CHILD_KEY1, null), range, null),
				beobachtung3, beobachtung2, beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(
				TestUtils.createFilter(null, SECTION_KEY3), range, null),
				beobachtung4, beobachtung3, beobachtung2, beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(
				TestUtils.createFilter(CHILD_KEY1, SECTION_KEY1), range, null),
				beobachtung2);
		assertEquals(
				beobachtungsDao.getBeobachtungen(
						TestUtils.createFilter(CHILD_KEY1, null), range, user1),
				beobachtung3, beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(
				TestUtils.createFilter(null, SECTION_KEY2), range, user1),
				beobachtung1);
		assertEquals(
				beobachtungsDao.getBeobachtungen(
						TestUtils.createFilter(CHILD_KEY1, SECTION_KEY2),
						range, user1), beobachtung1);

	}

	@Test
	public void testGetForSection() {
		assertContains(beobachtungsDao.getBeobachtungen(
				Arrays.asList(SECTION_KEY1, SECTION_KEY2, SECTION_KEY3), em),
				beobachtung4, beobachtung3, beobachtung2, beobachtung1);
		assertContains(beobachtungsDao.getBeobachtungen(
				Arrays.asList(SECTION_KEY3), em), beobachtung3);

	}

	@Test
	public void testRange() {
		assertEquals(beobachtungsDao.getBeobachtungen(new BeobachtungsFilter(),
				new Range(1, 2), null), beobachtung3, beobachtung2);
		assertEquals(beobachtungsDao.getBeobachtungen(new BeobachtungsFilter(),
				new Range(2, 10), null), beobachtung2, beobachtung1);
		assertEquals(beobachtungsDao.getBeobachtungen(new BeobachtungsFilter(),
				new Range(0, 1), null), beobachtung4);
	}

	@Test
	public void testRowCount() {
		Assert.assertEquals(4,
				beobachtungsDao.getRowCount(new BeobachtungsFilter(), null));
		Assert.assertEquals(3,
				beobachtungsDao.getRowCount(new BeobachtungsFilter(), user1));
		Assert.assertEquals(
				3,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(CHILD_KEY1, null), null));
		Assert.assertEquals(
				4,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(null, SECTION_KEY3), null));
		Assert.assertEquals(
				1,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(CHILD_KEY1, SECTION_KEY1), null));
		Assert.assertEquals(
				2,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(CHILD_KEY1, null), user1));
		Assert.assertEquals(
				1,
				beobachtungsDao.getRowCount(
						TestUtils.createFilter(null, SECTION_KEY2), user1));
		Assert.assertEquals(1, beobachtungsDao.getRowCount(
				TestUtils.createFilter(CHILD_KEY1, SECTION_KEY2), user1));
	}

	@Test
	public void testStoreBeobachtung() {
		final GwtBeobachtung beobachtung = TestUtils.createBeobachtung(
				CHILD_KEY1, SECTION_KEY1, user1, new Date()).toGwt();

		transaction.begin();
		beobachtungsDao.storeBeobachtung(beobachtung, user1, null);
		transaction.commit();

		final BeobachtungsFilter filter = TestUtils.createFilter(CHILD_KEY1,
				SECTION_KEY1);
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao.getBeobachtungen(filter, range, user1, em);
		Assert.assertEquals(1, beobachtungen.size());
		Assert.assertEquals(beobachtung, beobachtungen.get(0));
	}

	@Test
	public void testStoreBeobachtungTwoTimes() {
		final GwtBeobachtung beobachtung1 = TestUtils.createBeobachtung(
				CHILD_KEY1, SECTION_KEY1, user1, new Date(NOW)).toGwt();
		final GwtBeobachtung beobachtung2 = TestUtils.createBeobachtung(
				CHILD_KEY1, SECTION_KEY1, user1, new Date(NOW)).toGwt();

		transaction.begin();
		beobachtungsDao.storeBeobachtung(beobachtung1, user1, null);
		transaction.commit();
		transaction.begin();
		beobachtungsDao.storeBeobachtung(beobachtung2, user1, null);
		transaction.commit();

		final BeobachtungsFilter filter = TestUtils.createFilter(CHILD_KEY1,
				SECTION_KEY1);
		final List<GwtBeobachtung> beobachtungen = beobachtungsDao
				.getBeobachtungen(filter, range, user1, em);
		Assert.assertEquals(2, beobachtungen.size());
		Assert.assertEquals(beobachtung1, beobachtungen.get(0));
		Assert.assertEquals(beobachtung2, beobachtungen.get(1));
	}

	private void persist(Object object) {
		transaction.begin();
		em.persist(object);
		transaction.commit();
	}

	private Beobachtung createBeobachtung(Beobachtung beobachtung,
			User user) {
		beobachtung.setUser(user);
		return beobachtung;
	}

	private void assertEquals(List<GwtBeobachtung> beobachtungen,
			Beobachtung... beobachtung) {
		Assert.assertEquals(convertToGwtList(beobachtung), beobachtungen);
	}

	private List<GwtBeobachtung> convertToGwtList(Beobachtung[] beobachtungen) {
		final List<GwtBeobachtung> list = new ArrayList<GwtBeobachtung>();
		for(Beobachtung beobachtung : beobachtungen)
		{
			list.add(beobachtung.toGwt());
		}
		return list;
	}

	private void assertContains(List<GwtBeobachtung> beobachtungen,
			Beobachtung... beobachtung) {
		Assert.assertEquals(beobachtung.length, beobachtungen.size());
		Assert.assertTrue(beobachtungen.containsAll(convertToGwtList(beobachtung)));
	}

}
