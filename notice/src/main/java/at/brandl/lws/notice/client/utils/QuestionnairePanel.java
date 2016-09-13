package at.brandl.lws.notice.client.utils;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

import com.google.gwt.user.client.ui.VerticalPanel;

public class QuestionnairePanel extends VerticalPanel implements
		QuestionnaireAnswerListener {

	private final FormFactory formFactory;

	private GwtQuestionnaireAnswers answers;
	private GwtQuestionnaire questionnaire;

	private ChangeListener changeListener;

	public QuestionnairePanel(ChangeListener changeListener) {

		this.changeListener = changeListener;
		this.formFactory = new FormFactory(this);
	}

	public void setQuestionnaire(GwtQuestionnaire questionnaire, GwtQuestionnaireAnswers answers) {
		
		if (!questionnaire.getKey().equals(answers.getQuestionnaireKey())) {
			throw new IllegalArgumentException(
					"answers do not match questionnaire");
		}
		
		this.questionnaire = questionnaire;
		this.answers = answers;
		
		updateForm();
	}

	public void setAnswers(GwtQuestionnaireAnswers answers) {
		
		if (!questionnaire.getKey().equals(answers.getQuestionnaireKey())) {
			throw new IllegalArgumentException(
					"answers do not match questionnaire");
		}

		this.answers = answers;
		
		updateForm();
	}
	
	private void updateForm() {
		
		formFactory.setAnswers(answers);
		redraw();
	}

	private void redraw() {
		
		clear();
		add(formFactory.createTitle(questionnaire.getTitle()));
		for (GwtQuestionGroup group : questionnaire.getGroups()) {
			add(formFactory.createGroup(group));
		}
		if(!(questionnaire.getArchivedQuestionGroup() == null || questionnaire.getArchivedQuestionGroup().getQuestions().isEmpty())) {
			add(formFactory.createGroup(questionnaire.getArchivedQuestionGroup()));
		}
	}

	public void reset() {
		
		clear();
		answers = null;
		questionnaire = null;
	}
	

	@Override
	public void notifyAnswer(String questionKey, Object value) {

		changeListener.notifyChange();
		GwtAnswer answer = answers.getAnswer(questionKey);
		if (answer == null) {
			answer = questionnaire.getQuestion(questionKey).getAnswerTemplate()
					.createAnswer();
			answer.setQuestionKey(questionKey);
			answers.addAnswer(answer);
		}
		answer.setValue(value);
		answer.setUpdated(true);
	}

	public GwtQuestionnaireAnswers getAnswers() {
		
		return answers;
	}

	public GwtQuestionnaire getQuestionnaire() {
		
		return questionnaire;
	}


}
