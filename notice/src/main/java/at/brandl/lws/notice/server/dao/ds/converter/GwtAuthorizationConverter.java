package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.shared.util.Constants.Authorization.ADMIN;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EDIT_DIALOGUE_DATES;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EDIT_SECTIONS;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.EMAIL;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.KIND;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.SEE_ALL;
import static at.brandl.lws.notice.shared.util.Constants.Authorization.USER_ID;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import at.brandl.lws.notice.model.GwtAuthorization;

public class GwtAuthorizationConverter {

	public static GwtAuthorization toGwtAuthorization(Entity entity) {
		final GwtAuthorization authorization = new GwtAuthorization();
		authorization.setKey(KeyFactory.keyToString(entity.getKey()));
		authorization.setUserId((String) entity.getProperty(USER_ID));
		authorization.setEmail((String) entity.getProperty(EMAIL));
		authorization.setAdmin((Boolean) entity.getProperty(ADMIN));
		authorization.setEditSections((Boolean) entity.getProperty(EDIT_SECTIONS));
		authorization.setEditDialogueDates((Boolean) entity.getProperty(EDIT_DIALOGUE_DATES));
		authorization.setSeeAll((Boolean) entity.getProperty(SEE_ALL));
		return authorization;
	}

	public static Entity toEntity(GwtAuthorization aut) {
		final Key key = KeyFactory.createKey(KIND, aut.getUserId());
		final Entity authorization = new Entity(key);
		authorization.setProperty(USER_ID, aut.getUserId());
		authorization.setProperty(EMAIL, aut.getEmail());
		authorization.setProperty(ADMIN, aut.isAdmin());
		authorization.setProperty(EDIT_SECTIONS, aut.isEditSections());
		authorization.setProperty(EDIT_DIALOGUE_DATES, aut.isEditDialogueDates());
		authorization.setProperty(SEE_ALL, aut.isSeeAll());
		return authorization;
	}

}
