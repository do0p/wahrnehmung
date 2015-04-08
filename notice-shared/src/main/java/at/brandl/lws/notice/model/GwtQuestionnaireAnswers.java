package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GwtQuestionnaireAnswers implements Serializable {

	private static final long serialVersionUID = 6052075693740291611L;
	private String key;
	private String childKey;
	private String questionnaireKey;
	private Map<String, GwtAnswer> answers = new HashMap<String, GwtAnswer>();

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
		
		return answers.values();
	}

	public void setAnswers(Collection<GwtAnswer> answers) {
		clear();
		for(GwtAnswer answer : answers) {
			addAnswer(answer);
		}
	}

	public void clear() {
		
		answers.clear();
	}

	public GwtAnswer getAnswer(String questionKey) {

		return answers.get(questionKey);
	}

	public void addAnswer(GwtAnswer answer) {
		
		answers.put(answer.getQuestionKey(), answer);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Answers for Questionnaire: " + questionnaireKey);
		builder.append(answers.toString());
		return builder.toString();
	}
}
