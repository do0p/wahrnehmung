package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtQuestionnaire implements Serializable {

	private static final long serialVersionUID = -7569171050607154080L;
	private String title;
	private String sectionKey;
	private List<GwtQuestionGroup> groups = new ArrayList<GwtQuestionGroup>();
	private Map<String, GwtQuestion> questions = new HashMap<String, GwtQuestion>();
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
		
		this.groups.clear();
		for(GwtQuestionGroup group : groups) {
			addQuestionGroup(group);
		}
	}

	public void addQuestionGroup(GwtQuestionGroup group) {
		
		groups.add(group);
		addQuestions(group);
	}

	private void addQuestions(GwtQuestionGroup group) {
		
		for(GwtQuestion question : group.getQuestions()) {
			questions.put(question.getKey(), question);
		}
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

	public GwtQuestion getQuestion(String questionKey) {

		return questions.get(questionKey);
	}

}
