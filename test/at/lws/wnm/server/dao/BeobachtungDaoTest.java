package at.lws.wnm.server.dao;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.lws.wnm.TestUtils;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class BeobachtungDaoTest {

	private static final Long SECTION_KEY = Long.valueOf(0);
	private static final Long CHILD_KEY = Long.valueOf(0);

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());

	private BeobachtungDao beobachtungsDao;

	private GwtBeobachtung beobachtung1;

	private User user;

	@Before
	public void setUp() {
		helper.setUp();
		beobachtungsDao = DaoRegistry.get(BeobachtungDao.class);
		beobachtung1 = TestUtils.createBeobachtung(CHILD_KEY, SECTION_KEY);
		user = TestUtils.createUser();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testBeobachtungsDao() {
		beobachtungsDao.storeBeobachtung(beobachtung1, user);
		//beobachtungsDao.getBeobachtungen(Arrays.asList(SECTION_KEY));
	}
}
