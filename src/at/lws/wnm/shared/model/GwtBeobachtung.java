package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtBeobachtung implements Serializable, Comparable<GwtBeobachtung> {

	private static final long serialVersionUID = -2732611746367965750L;
	private String text;
	private Long childKey;
	private Long sectionKey;
	private Date date;
	private Long key;
	private String childName;
	private String sectionName;
	private SocialEnum social;
	private DurationEnum duration;
	private String user;
	private List<Long> additionalChildKeys = new ArrayList<Long>();

	public void setText(String text) {
		this.text = text;
	}

	public void setChildKey(Long childKey) {
		this.childKey = childKey;
	}

	public void setSectionKey(Long sectionKey) {
		this.sectionKey = sectionKey;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Long getChildKey() {
		return childKey;
	}

	public Long getSectionKey() {
		return sectionKey;
	}

	public String getText() {
		return text;
	}

	public Date getDate() {
		return date;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<Long> getAdditionalChildKeys() {
		return additionalChildKeys;
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
				&& date.equals(other.date) 
				&& user.equals(other.user)
				&& additionalChildKeys.equals(other.additionalChildKeys);
		
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
		return result * 17 + user.hashCode();
	}

	@Override
	public int compareTo(GwtBeobachtung other) {
		if(date == null)
		{
			return 1;
		}
		if(other.date == null)
		{
			return -1;
		}
		return other.date.compareTo(date);
	}

	

	

}
