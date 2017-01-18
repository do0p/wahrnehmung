package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Collection;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.model.Authorization;

public class AuthorizationDsDao extends AbstractDsDao {


//	public static final Set<String> SUPER_USER_IDS = new HashSet<String>();
	public static final String AUTH_DAO_MEMCACHE = "authDao";

	public static final String AUTHORIZATION_KIND = "AuthorizationDs";
	public static final String USER_ID_FIELD = "userId";
	public static final String EMAIL_FIELD = "email";
	public static final String ADMIN_FIELD = "admin";
	public static final String SEE_ALL_FIELD = "seeAll";
	public static final String EDIT_SECTIONS_FIELD = "editSections";
	public static final String EDIT_DIALOGUE_DATES_FIELD = "editDialogueDates";

//	static {
//		SUPER_USER_IDS.add("dbrandl72@gmail.com");
//	}

	public AuthorizationDsDao() {
		initCache();
	}

	public Authorization getAuthorization(User user) {
		if (user == null) {
			return null;
		}

		final String userId = createUserId(user);
		final Key key = KeyFactory.createKey(AUTHORIZATION_KIND, userId);
		Entity authorization = getFromCache(key, AUTH_DAO_MEMCACHE);
		if (authorization == null) {
//			if (SUPER_USER_IDS.contains(userId)) {
//				authorization = createSuperUser(key, userId);
//			} else {
				try {
					authorization = getDatastoreService().get(key);
				} catch (EntityNotFoundException e) {
					authorization = null;
				}
//			}
			getCache(AUTH_DAO_MEMCACHE).put(key, authorization);
		}
		Authorization gwtAuthorization = toGwt(authorization);
		return gwtAuthorization;
	}

	public Collection<Authorization> queryAuthorizations() {
		final Collection<Authorization> result = new ArrayList<Authorization>();
		for (Entity entity : queryAuthorizationsInternal()) {
			result.add(toGwt(entity));
		}
		return result;
	}

	public void storeAuthorization(Authorization aut) {
		
		final String userId = createUserId(aut.getEmail());
//		if(SUPER_USER_IDS.contains(userId))
//		{
//			return;
//		}
		aut.setUserId(userId);
		final Entity authorization = toEntity(aut);
		aut.setKey(KeyFactory.keyToString(authorization.getKey()));
		getDatastoreService().put(authorization);
		insertIntoCache(authorization, AUTH_DAO_MEMCACHE);
	}

	public void deleteAuthorization(String email) {
		final String userId = createUserId(email);
		final Key key = KeyFactory.createKey(AUTHORIZATION_KIND, userId);
		deleteEntity(key, AUTH_DAO_MEMCACHE);
	}

	private String createUserId(String email) {
		return email.toLowerCase();
	}

	private void initCache() {
		for (Entity entity : queryAuthorizationsInternal()) {
			insertIntoCache(entity, AUTH_DAO_MEMCACHE);
		}

//		for (String superUserId : SUPER_USER_IDS) {
//			final Key key = KeyFactory.createKey(AUTHORIZATION_KIND,
//					superUserId);
//			insertIntoCache(createSuperUser(key, superUserId), AUTH_DAO_MEMCACHE);
//		}
	}

	private Iterable<Entity> queryAuthorizationsInternal() {
		final Query query = new Query(AUTHORIZATION_KIND);
		final Iterable<Entity> entities = execute(query, withDefaults());
		return entities;
	}

	private String createUserId(User user) {
		return user == null ? null : user.getEmail().toLowerCase();
	}

	private Authorization toGwt(Entity entity) {
		if (entity == null) {
			return null;
		}
		final Authorization authorization = new Authorization();
		authorization.setKey(KeyFactory.keyToString(entity.getKey()));
		authorization.setUserId((String) entity.getProperty(USER_ID_FIELD));
		authorization.setEmail((String) entity.getProperty(EMAIL_FIELD));
		authorization.setAdmin((Boolean) entity.getProperty(ADMIN_FIELD));
		authorization.setEditSections((Boolean) entity
				.getProperty(EDIT_SECTIONS_FIELD));
		authorization.setEditDialogueDates((Boolean) entity
				.getProperty(EDIT_DIALOGUE_DATES_FIELD));
		authorization.setSeeAll((Boolean) entity.getProperty(SEE_ALL_FIELD));
		return authorization;
	}

	private Entity toEntity(Authorization aut) {
		final Key key = KeyFactory.createKey(AUTHORIZATION_KIND,
				aut.getUserId());
		final Entity authorization = new Entity(key);
		authorization.setProperty(USER_ID_FIELD, aut.getUserId());
		authorization.setProperty(EMAIL_FIELD, aut.getEmail());
		authorization.setProperty(ADMIN_FIELD, aut.isAdmin());
		authorization.setProperty(EDIT_SECTIONS_FIELD, aut.isEditSections());
		authorization.setProperty(EDIT_DIALOGUE_DATES_FIELD, aut.isEditDialogueDates());
		authorization.setProperty(SEE_ALL_FIELD, aut.isSeeAll());
		return authorization;
	}

//	private Entity createSuperUser(Key key, String email) {
//		final Entity superUser = new Entity(key);
//		superUser.setProperty(USER_ID_FIELD, email);
//		superUser.setProperty(EMAIL_FIELD, email);
//		superUser.setProperty(ADMIN_FIELD, true);
//		superUser.setProperty(SEE_ALL_FIELD, true);
//		superUser.setProperty(EDIT_SECTIONS_FIELD, true);
//		superUser.setProperty(EDIT_DIALOGUE_DATES_FIELD, true);
//		return superUser;
//	}
}