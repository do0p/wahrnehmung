package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtQuestionGroup implements Serializable {

	private static final long serialVersionUID = -2091436004837897401L;
	private String title;
	private List<GwtQuestion> questions = new ArrayList<GwtQuestion>();
	private String key;
	private Date archiveDate;

	public Date getArchiveDate() {
		return archiveDate;
	}

	public void setArchiveDate(Date archived) {
		this.archiveDate = archived;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<GwtQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<GwtQuestion> questions) {
		this.questions = questions;
	}

	public void addQuestion(GwtQuestion question) {
		questions.add(question);
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public boolean isArchived() {
		return archiveDate != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GwtQuestionGroup)) {
			return false;
		}
		GwtQuestionGroup other = (GwtQuestionGroup) obj;
		boolean result = ObjectUtils.equals(title, other.title);
		result &= ObjectUtils.equals(questions, other.questions);
		return result;
	}

	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(questions);
		result = result * 17 + ObjectUtils.hashCode(title);
		return result;
	}

	public void replaceQuestion(String toBeReplacedKey, GwtQuestion replacement) {
		for (int i = 0; i < questions.size(); i++) {
			if (toBeReplacedKey.equals(questions.get(i).getKey())) {
				questions.set(i, replacement);
				return;
			}
		}
		System.err.println("Question with key " + toBeReplacedKey
				+ " is not contained in group with key " + this.key);
		questions.add(replacement);

	}

	public void clear() {
		title = null;
		questions.clear();
	}

	public Map<String, GwtQuestion> getAllQuestions() {
		Map<String, GwtQuestion> result = new HashMap<String, GwtQuestion>();
		for(GwtQuestion question : questions) {
			result.put(question.getKey(), question);
		}
		return result;
	}

}
