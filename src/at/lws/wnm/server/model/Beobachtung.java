package at.lws.wnm.server.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

@Entity
public class Beobachtung implements Serializable {

	private static final long serialVersionUID = -2625046453601678465L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;

	private Long childKey;

	private Long sectionKey;

	private Text text;

	private Date date;

	private String duration;

	private String social;

	private User user;

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getChildKey() {
		return childKey;
	}

	public void setChildKey(Long child) {
		this.childKey = child;
	}

	public Long getSectionKey() {
		return sectionKey;
	}

	public void setSectionKey(Long section) {
		this.sectionKey = section;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public void setDate(Date date) {
		this.date = date;

	}

	public Date getDate() {
		return date;
	}

	public static Beobachtung valueOf(GwtBeobachtung gwtBeobachtung) {
		final Beobachtung beobachtung = new Beobachtung();
		beobachtung.setChildKey(gwtBeobachtung.getChildKey());
		beobachtung.setSectionKey(gwtBeobachtung.getSectionKey());
		beobachtung.setDate(gwtBeobachtung.getDate());
		beobachtung.setText(new Text(gwtBeobachtung.getText()));
		beobachtung.setKey(gwtBeobachtung.getKey());
		final DurationEnum gwtDuration = gwtBeobachtung.getDuration();
		if (gwtDuration != null) {
			beobachtung.setDuration(gwtDuration.name());
		}
		final SocialEnum gwtSocial = gwtBeobachtung.getSocial();
		if (gwtSocial != null) {
			beobachtung.setSocial(gwtSocial.name());
		}
	//	beobachtung.setUser(gwtBeobachtung.getUser());
		return beobachtung;
	}


	public GwtBeobachtung toGwt() {
		final GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(key);
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setText(text.getValue());
		if (duration != null) {
			beobachtung.setDuration(DurationEnum.valueOf(duration));
		}
		if (social != null) {
			beobachtung.setSocial(SocialEnum.valueOf(social));
		}
		beobachtung.setUser(user.getEmail());
		return beobachtung;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSocial() {
		return social;
	}

	public void setSocial(String social) {
		this.social = social;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
