package at.brandl.lws.notice;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.server.dao.ds.SectionDsDao;
import at.brandl.lws.notice.shared.model.Authorization;
import at.brandl.lws.notice.shared.model.BeobachtungsFilter;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;
import at.brandl.lws.notice.shared.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.shared.model.GwtBeobachtung.SocialEnum;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.model.GwtSection;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

public class TestUtils {

	public static GwtBeobachtung createBeobachtung(String key, String childKey,
			String sectionKey, Date date, DurationEnum duration,
			SocialEnum social, String text, User user) {
		final GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(key);
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setDuration(duration);
		beobachtung.setSocial(social);
		beobachtung.setText(text);
		beobachtung.setUser(user.getEmail());
		return beobachtung;
	}

	@Test
	public void testContenation() {
		int width = 440;
		Assert.assertEquals("440px", width + "px");
	}

	public static String toString(Key key) {
		return KeyFactory.keyToString(key);
	}

	public static GwtBeobachtung createBeobachtung(String childKey,
			String sectionkey, User user, Date date, String text) {
		return createBeobachtung(null, childKey, sectionkey, date,
				DurationEnum.LONG, SocialEnum.ALONE, text, user);

	}

	public static User createUser(String email) {
		return new User(email, "authDomain");
	}

	public static final String TEXT = "Standard Text";

	public static GwtSection createSection(String key, String sectionName,
			String parentKey) {
		final GwtSection section = new GwtSection();
		section.setKey(key);
		section.setSectionName(sectionName);
		section.setParentKey(parentKey);
		return section;
	}

	public static BeobachtungsFilter createFilter(String childKey,
			String sectionKey, Date... timeRange) {
		final BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(childKey);
		filter.setSectionKey(sectionKey);
		filter.setTimeRange(timeRange);
		return filter;
	}
	
	public static BeobachtungsFilter createFilterWithSummaries(String childKey, String sectionKey, Date ...timeRange) {
		BeobachtungsFilter filter = createFilter(childKey, sectionKey, timeRange);
		filter.setShowSummaries(true);
		return filter;
	}

	public static GwtChild createGwtChild(String key, String firstName,
			String lastName, Date birthday, Date... dialogueDates) {
		final GwtChild child = new GwtChild();
		child.setKey(key);
		child.setFirstName(firstName);
		child.setLastName(lastName);
		child.setBirthDay(birthday);
		if (dialogueDates != null && dialogueDates.length > 0) {
			child.setDevelopementDialogueDates(Arrays.asList(dialogueDates));
		}
		return child;
	}

	public static Entity createChildEntity(String firstName, String lastName) {
		final Entity child = new Entity(ChildDsDao.CHILD_KIND);
		child.setProperty(ChildDsDao.FIRSTNAME_FIELD, firstName);
		child.setProperty(ChildDsDao.LASTNAME_FIELD, firstName);
		child.setProperty(ChildDsDao.BIRTHDAY_FIELD, new Date());
		return child;
	}

	public static Entity createSectionEntity(String sectionName,
			String parentKey) {

		final Entity entity;
		if (parentKey == null) {
			entity = new Entity(SectionDsDao.SECTION_KIND);
		} else {
			entity = new Entity(SectionDsDao.SECTION_KIND, toKey(parentKey));
		}
		entity.setProperty(SectionDsDao.SECTION_NAME_FIELD, sectionName);
		return entity;
	}

	public static Entity createBeobachtungEntity(GwtBeobachtung gwtBeobachtung,
			User user) {

		final Entity entity = new Entity(BeobachtungDsDao.BEOBACHTUNG_KIND,
				toKey(gwtBeobachtung.getChildKey()));

		entity.setProperty(BeobachtungDsDao.SECTION_KEY_FIELD,
				toKey(gwtBeobachtung.getSectionKey()));
		entity.setProperty(BeobachtungDsDao.DATE_FIELD,
				gwtBeobachtung.getDate());
		entity.setProperty(BeobachtungDsDao.TEXT_FIELD,
				new Text(gwtBeobachtung.getText()));
		entity.setProperty(BeobachtungDsDao.USER_FIELD, user);
		final DurationEnum duration = gwtBeobachtung.getDuration();
		if (duration != null) {
			entity.setProperty(BeobachtungDsDao.DURATION_FIELD, duration.name());
		}
		final SocialEnum social = gwtBeobachtung.getSocial();
		if (social != null) {
			entity.setProperty(BeobachtungDsDao.SOCIAL_FIELD, social.name());
		}
		return entity;
	}

	private static Key toKey(String key) {
		return KeyFactory.stringToKey(key);
	}

	public static Authorization createAuthorization(String email,
			boolean admin, Boolean editSections, boolean seeAll) {
		final Authorization authorization = new Authorization();
		authorization.setEmail(email);
		authorization.setAdmin(admin);
		authorization.setEditSections(editSections);
		authorization.setSeeAll(seeAll);
		return authorization;
	}
}
