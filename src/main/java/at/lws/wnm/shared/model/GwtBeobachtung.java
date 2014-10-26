package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtBeobachtung implements Serializable {

	public static enum SocialEnum {
		ALONE("alleine"), TWO_BY_TWO("zu zweit"), IN_GROUP("in Gruppe");
		private static final Map<String, SocialEnum> SOCIAL_FORMS = new HashMap<String, GwtBeobachtung.SocialEnum>();

		static {
			for (SocialEnum socialForm : SocialEnum.values()) {
				SOCIAL_FORMS.put(socialForm.text, socialForm);
			}
		}

		private final String text;

		private SocialEnum(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public static SocialEnum valueOfText(String text) {
			return SOCIAL_FORMS.get(text);
		}
	}

	public static enum DurationEnum {
		SHORT("kurz"), MEDIUM("mittel"), LONG("lang");
		private static final Map<String, DurationEnum> DURATIONS = new HashMap<String, GwtBeobachtung.DurationEnum>();

		static {
			for (DurationEnum socialForm : DurationEnum.values()) {
				DURATIONS.put(socialForm.text, socialForm);
			}
		}

		private final String text;

		private DurationEnum(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public static DurationEnum valueOfText(String text) {
			return DURATIONS.get(text);
		}
	}

	private static final long serialVersionUID = -2732611746367965750L;
	private String text;
	private String childKey;
	private String sectionKey;
	private Date date;
	private String key;
	private String childName;
	private String sectionName;
	private SocialEnum social;
	private DurationEnum duration;
	private String user;
	private List<String> additionalChildKeys = new ArrayList<String>();
	private Map<String, String> filenames = new HashMap<String, String>();

	public void setText(String text) {
		this.text = text;
	}

	public void setChildKey(String childKey) {
		this.childKey = childKey;
	}

	public void setSectionKey(String sectionKey) {
		this.sectionKey = sectionKey;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getChildKey() {
		return childKey;
	}

	public String getSectionKey() {
		return sectionKey;
	}

	public String getText() {
		return text;
	}

	public Date getDate() {
		return date;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getChildName() {
		return childName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public SocialEnum getSocial() {
		return social;
	}

	public void setSocial(SocialEnum social) {
		this.social = social;
	}

	public DurationEnum getDuration() {
		return duration;
	}

	public void setDuration(DurationEnum duration) {
		this.duration = duration;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<String> getAdditionalChildKeys() {
		return additionalChildKeys;
	}

	public Map<String, String> getFilenames() {
		return filenames;
	}

	public void setFilenames(Map<String, String> filenames) {
		this.filenames = filenames;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GwtBeobachtung)) {
			return false;
		}
		final GwtBeobachtung other = (GwtBeobachtung) obj;
		return childKey.equals(other.childKey)
				&& sectionKey.equals(other.sectionKey)
				&& text.equals(other.text)
				&& ObjectUtils.equals(social, other.social)
				&& ObjectUtils.equals(duration, other.duration)
				&& date.equals(other.date) && user.equals(other.user)
				&& additionalChildKeys.equals(other.additionalChildKeys)
				&& filenames.equals(other.filenames);

	}

	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + childKey.hashCode();
		result = result * 17 + sectionKey.hashCode();
		result = result * 17 + text.hashCode();
		result = result * 17 + ObjectUtils.hashCode(social);
		result = result * 17 + ObjectUtils.hashCode(duration);
		result = result * 17 + date.hashCode();
		result = result * 17 + additionalChildKeys.hashCode();
		result = result * 17 + filenames.hashCode();
		return result * 17 + user.hashCode();
	}

	@Override
	public String toString() {
		return text;
	}

}
