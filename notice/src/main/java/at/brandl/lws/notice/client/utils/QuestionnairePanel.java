package at.brandl.lws.notice.client.utils;

import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import com.google.gwt.user.client.ui.VerticalPanel;

public class QuestionnairePanel extends VerticalPanel {

	private final FormFactory formFactory;
	private final GwtQuestionnaireAnswers answers;
	private GwtQuestionnaire questionnaire;;

	public QuestionnairePanel(FormFactory formFactory) {
		this.formFactory = formFactory;
		this.answers = new GwtQuestionnaireAnswers();
	}
	
	public void setQuestionnaire(GwtQuestionnaire questionnaire) {
		this.questionnaire = questionnaire;
		answers.setQuestionnaireKey(questionnaire.getKey());
		answers.clear();
		
		clear();
		redraw();
	}
	
	
	
	private void redraw() {
		add(formFactory.createTitle(questionnaire.getTitle()));
		for(GwtQuestionGroup group : questionnaire.getGroups()) {
			add(formFactory.createGroup(group));
		}
	}



	public void setChildKey(String childKey) {
		answers.setChildKey(childKey);
	}
	
	public GwtQuestionnaireAnswers getAnswers() {
		return answers;
	}
	
}
