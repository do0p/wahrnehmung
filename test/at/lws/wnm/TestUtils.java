package at.lws.wnm;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.server.model.Child;
import at.lws.wnm.server.model.Section;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

public class TestUtils {

	public static Beobachtung createBeobachtung(Long key, Long childKey,
			Long sectionKey, Date date, DurationEnum duration,
			SocialEnum social, String text, String userName) {
		final Beobachtung beobachtung = new Beobachtung();
		beobachtung.setKey(key);
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setDuration(duration.name());
		beobachtung.setSocial(social.name());
		beobachtung.setText(new Text(text));
		return beobachtung;
	}

	@Test
	public void testContenation() {
		int width = 440;
		Assert.assertEquals("440px", width + "px");
	}

	public static Beobachtung createBeobachtung(Long childKey, Long sectionkey,
			User user, Date date) {
		return createBeobachtung(null, childKey, sectionkey, date,
				DurationEnum.LONG, SocialEnum.ALONE, TestUtils.TEXT, user
						.getEmail().toLowerCase());

	}

	public static Child createChild(Long childKey, String firstName,
			String lastName) {
		final Child child = new Child();
		child.setFirstName(firstName);
		child.setLastName(lastName);
		child.setKey(childKey);
		return child;
	}

	public static User createUser(String email) {
		return new User(email, "authDomain");
	}

	public static final String TEXT = "Standard Text";

	public static Section createSection(Long key, String sectionName,
			Long parentKey) {
		final Section section = new Section();
		section.setKey(key);
		section.setSectionName(sectionName);
		section.setParentKey(parentKey);
		return section;
	}

	public static BeobachtungsFilter createFilter(Long childKey, Long sectionKey) {
		final BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(childKey);
		filter.setSectionKey(sectionKey);
		return filter;
	}

}
