package at.lws.wnm;

import java.util.Date;

import com.google.appengine.api.users.User;

import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

public class TestUtils {

	public static GwtBeobachtung createBeobachtung(Long key, Long childKey,
			Long sectionKey, Date date, DurationEnum duration,
			SocialEnum social, String text) {
		final GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(key);
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setDuration(duration);
		beobachtung.setSocial(social);
		beobachtung.setText(text);
		return beobachtung;
	}

	public static GwtBeobachtung createBeobachtung(Long childKey, Long sectionkey) {
		return createBeobachtung(null, childKey, sectionkey, new Date(),
				DurationEnum.LONG, SocialEnum.ALONE, TestUtils.TEXT);
	}

	public static User createUser() {
		return new User("test@example.com", "authDomain");
	}

	public static final String TEXT = "Standard Text";

}
