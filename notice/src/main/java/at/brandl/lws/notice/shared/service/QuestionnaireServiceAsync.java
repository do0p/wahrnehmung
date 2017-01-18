package at.brandl.lws.notice.shared.service;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

public interface QuestionnaireServiceAsync {

	void getQuestionnaireAnswers(String childKey, AsyncCallback<Collection<GwtQuestionnaireAnswers>> callback);
	
	void storeQuestionnaireAnswers(GwtQuestionnaireAnswers answers, AsyncCallback<GwtQuestionnaireAnswers> callback);
	
}
