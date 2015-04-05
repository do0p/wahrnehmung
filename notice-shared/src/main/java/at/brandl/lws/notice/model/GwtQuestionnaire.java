package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GwtQuestionnaire implements Serializable {

	private static final long serialVersionUID = -7569171050607154080L;
	private String title;
	private String sectionKey;
	private List<GwtQuestionGroup> groups = new ArrayList<GwtQuestionGroup>();
	private String key;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSection() {
		return sectionKey;
	}

	public void setSection(String sectionKey) {
		this.sectionKey = sectionKey;
	}

	public List<GwtQuestionGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<GwtQuestionGroup> groups) {
		this.groups = groups;
	}

	public void addQuestionGroup(GwtQuestionGroup group) {
		groups.add(group);

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GwtQuestionnaire)) {
			return false;
		}
		GwtQuestionnaire other = (GwtQuestionnaire) obj;
		boolean result = ObjectUtils.equals(title, other.title);
		result &= ObjectUtils.equals(sectionKey, other.sectionKey);
		result &= ObjectUtils.equals(groups, other.groups);
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(sectionKey);
		result = result * 17 + ObjectUtils.hashCode(groups);
		result = result * 17 + ObjectUtils.hashCode(title);
		return result;
	}

}
