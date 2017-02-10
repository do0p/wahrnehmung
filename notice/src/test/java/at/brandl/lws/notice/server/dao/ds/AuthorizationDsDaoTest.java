package at.brandl.lws.notice.server.dao.ds;
import static at.brandl.lws.notice.TestUtils.createAuthorization;
import static at.brandl.lws.notice.TestUtils.createUser;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.users.User;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.shared.util.Constants;

public class AuthorizationDsDaoTest extends AbstractDsDaoTest {

	private static final String USER_EMAIL = "User@lws.at";
	private AuthorizationDsDao authorizationDao;
	private GwtAuthorization authorizationUser;
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
		String key = authorizationUser.getKey();
		String userId = authorizationUser.getUserId();
		Assert.assertNotNull(key);
		assertDatastoreContains(key);
		assertCacheContainsStringKey(userId);

		// read
		GwtAuthorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);

		// read all
		final Collection<GwtAuthorization> allAuthorizations = authorizationDao
				.queryAuthorizations();
		Assert.assertEquals(1, allAuthorizations.size());

		// update
		authorizationUser.setSeeAll(true);
		String updatedEmail = "Updated@gmail.com";
		authorizationUser.setEmail(updatedEmail);
		authorizationDao.storeAuthorization(authorizationUser);
		
		storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNull(storedAuth);
		assertCacheContainsNotStringKey(userId);
		assertDatastoreContainsNot(key);
		
		storedAuth = authorizationDao.getAuthorization(createUser(updatedEmail));
		Assert.assertNotNull(storedAuth);
		key = storedAuth.getKey();
		userId = storedAuth.getUserId();
		assertDatastoreContains(key);
		assertCacheContainsStringKey(userId);
		assertAuthorization(updatedEmail, false, true, true, storedAuth);
		
		// delete
		authorizationDao.deleteAuthorization(updatedEmail);

		assertCacheContainsNotStringKey(userId);
		assertDatastoreContainsNot(key);
	}

	private void assertAuthorization(String email, boolean admin,
			boolean editSections, boolean seeAll, GwtAuthorization auth) {
		Assert.assertEquals(email, auth.getEmail());
		Assert.assertEquals(email.toLowerCase(), auth.getUserId());
		Assert.assertEquals(admin, auth.isAdmin());
		Assert.assertEquals(editSections, auth.isEditSections());
		Assert.assertEquals(seeAll, auth.isSeeAll());
	}

	@Test
	public void worksWithCache() {
		authorizationDao.storeAuthorization(authorizationUser);
		String key = authorizationUser.getKey();
		String userId = authorizationUser.getUserId();

		assertDatastoreContains(key);
		assertCacheContainsStringKey(userId);

		removeFromDatastore(key);

		GwtAuthorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);
		assertCacheContainsStringKey(userId);

		removeFromCacheStringKey(userId);

		assertCacheContainsNotStringKey(userId);
		assertDatastoreContainsNot(key);
	}

	@Test
	public void worksWithoutCache() {
		authorizationDao.storeAuthorization(authorizationUser);
		String key = authorizationUser.getKey();
		String userId = authorizationUser.getUserId();

		
		assertDatastoreContains(key);
		assertCacheContainsStringKey(userId);

		removeFromCacheStringKey(userId);

		GwtAuthorization storedAuth = authorizationDao.getAuthorization(user);
		Assert.assertNotNull(storedAuth);
		assertAuthorization(USER_EMAIL, false, true, false, storedAuth);

		assertDatastoreContains(key);
		assertCacheContainsStringKey(userId);

		authorizationDao.deleteAuthorization(USER_EMAIL);
		assertCacheContainsNotStringKey(userId);
		assertDatastoreContainsNot(key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return Constants.Authorization.Cache.NAME;
	}

}
