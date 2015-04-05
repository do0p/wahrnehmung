package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Collection;

public class GwtQuestionnaireAnswers implements Serializable{

	private static final long serialVersionUID = 6052075693740291611L;
	private GwtChild child;
	private GwtQuestionnaire questionnaire;
	private Collection<GwtAnswer> answers;
	
}
