package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GwtQuestionnaire implements Serializable {

	private static final long serialVersionUID = -7569171050607154080L;
	private String title;
	private String sectionKey;
	private List<GwtQuestionGroup> groups = new ArrayList<>();
	private GwtQuestionGroup archivedQuestions;
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
		for (GwtQuestionGroup group : groups) {
			addQuestionGroup(group);
		}
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

	public GwtQuestion getQuestion(String questionKey) {

		for (GwtQuestionGroup group : groups) {
			for (GwtQuestion question : group.getQuestions()) {
				if (questionKey.equals(question.getKey())) {
					return question;
				}
			}
		}
		return null;
	}

	public String getNewestVersion(String questionKey) {

		for (GwtQuestionGroup group : groups) {
			for (GwtQuestion question : group.getQuestions()) {
				String newestKey = question.getKey();
				if (questionKey.equals(newestKey)) {
					return newestKey;
				}
				for (String archived : question.getArchived()) {
					if (questionKey.equals(archived)) {
						return newestKey;
					}
				}
			}
		}
		return null;
	}

	public void addArchivedQuestion(GwtQuestion archivedQuestion) {
		if (archivedQuestions == null) {
			archivedQuestions = new GwtQuestionGroup();
		}
		archivedQuestions.addQuestion(archivedQuestion);
	}

	public void replace(String toBeReplacedKey, GwtQuestion replacement) {

		for (GwtQuestionGroup group : groups) {
			for (GwtQuestion question : group.getQuestions()) {
				if (toBeReplacedKey.equals(question.getKey())) {
					group.replaceQuestion(toBeReplacedKey, replacement);
					return;
				}
			}
		}
		System.err.println(String.format(
				"Question with key %s is not in any group", toBeReplacedKey));
	}

}
