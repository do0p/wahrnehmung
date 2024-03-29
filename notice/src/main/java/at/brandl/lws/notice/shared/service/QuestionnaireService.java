package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

@RemoteServiceRelativePath("questionnaire")
public interface QuestionnaireService extends RemoteService {

	Collection<GwtQuestionnaireAnswers> getQuestionnaireAnswers(String childKey);
	
	GwtQuestionnaireAnswers storeQuestionnaireAnswers(GwtQuestionnaireAnswers answers);
}
