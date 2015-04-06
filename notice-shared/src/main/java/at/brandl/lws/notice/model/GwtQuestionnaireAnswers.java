package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class GwtQuestionnaireAnswers implements Serializable {

	private static final long serialVersionUID = 6052075693740291611L;
	private String childKey;
	private String questionnaireKey;
	private Collection<GwtAnswer> answers = new ArrayList<GwtAnswer>();

	public String getChildKey() {
		return childKey;
	}

	public void setChildKey(String childKey) {
		this.childKey = childKey;
	}

	public String getQuestionnaireKey() {
		return questionnaireKey;
	}

	public void setQuestionnaireKey(String questionnaireKey) {
		this.questionnaireKey = questionnaireKey;
	}

	public Collection<GwtAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(Collection<GwtAnswer> answers) {
		this.answers = answers;
	}

	public void clear() {
		answers.clear();
	}
}
