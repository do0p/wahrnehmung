package at.brandl.lws.notice.model;

import java.util.ArrayList;
import java.util.List;

public class GwtQuestionGroup {

	private String title;
	private List<GwtQuestion> questions = new ArrayList<GwtQuestion>();
	
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
}
