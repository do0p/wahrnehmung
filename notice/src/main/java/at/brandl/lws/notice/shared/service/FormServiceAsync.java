package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtQuestionnaire;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

	void storeFormAsString(String formText, String sectionKey, AsyncCallback<GwtQuestionnaire> callback);

	void getAllForms(String childKey, AsyncCallback<List<GwtQuestionnaire>> callback);
	
	void getAllForms(AsyncCallback<List<GwtQuestionnaire>> callback);
	
}
