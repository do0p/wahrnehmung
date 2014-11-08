package at.brandl.lws.notice.server.dao.ds;
import static at.brandl.lws.notice.TestUtils.createAuthorization;
import static at.brandl.lws.notice.TestUtils.createUser;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.shared.model.Authorization;

import com.google.appengine.api.users.User;

public class AuthorizationDsDaoTest extends AbstractDsDaoTest {

	private static final String USER_EMAIL = "User@lws.at";
	private AuthorizationDsDao authorizationDao;
	private Authorization authorizationUser;
	private User user;

	@Before
	public void setUp() {
		user = createUser(USER_EMAIL);
		authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
		authorizationUser = createAuthorization(USER_EMAIL, false, true, false);
	}

	@Test
	public void crud() {
		// create
		authorizationDao.storeAuthorization(authorizationUser);
		final String key = authorizationUser.getKey();
		Assert.assertNotNull(key);
		assertServicesContains(key);

		// read

		Authorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);

		// read all
		final Collection<Authorization> allAuthorizations = authorizationDao
				.queryAuthorizations();
		Assert.assertEquals(1, allAuthorizations.size());

		// update
		authorizationUser.setSeeAll(true);
		authorizationDao.storeAuthorization(authorizationUser);
		storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, true, storedAuth);
		assertServicesContains(key);

		// delete
		authorizationDao.deleteAuthorization(USER_EMAIL);

		assertServicesContainsNot(key);

	}

	private void assertAuthorization(String email, boolean admin,
			boolean editSections, boolean seeAll, Authorization auth) {
		Assert.assertEquals(email, auth.getEmail());
		Assert.assertEquals(email.toLowerCase(), auth.getUserId());
		Assert.assertEquals(admin, auth.isAdmin());
		Assert.assertEquals(editSections, auth.isEditSections());
		Assert.assertEquals(seeAll, auth.isSeeAll());
	}

	@Test
	public void worksWithCache() {
		authorizationDao.storeAuthorization(authorizationUser);
		final String key = authorizationUser.getKey();

		assertServicesContains(key);

		removeFromDatastore(key);

		Authorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);
		assertCacheContains(key);

		removeFromCache(key);

		assertServicesContainsNot(key);
	}

	@Test
	public void worksWithoutCache() {
		authorizationDao.storeAuthorization(authorizationUser);
		final String key = authorizationUser.getKey();

		assertServicesContains(key);

		removeFromCache(key);

		Authorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);
		assertServicesContains(key);

		removeFromCache(key);

		authorizationDao.deleteAuthorization(USER_EMAIL);
		assertServicesContainsNot(key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return AuthorizationDsDao.AUTH_DAO_MEMCACHE;
	}

}
