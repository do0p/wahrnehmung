package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtQuestionnaire;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

	void getFormAsString(AsyncCallback<String> callback);

	void storeFormAsString(String formText, AsyncCallback<String> callback);

	void getAllForms(AsyncCallback<List<GwtQuestionnaire>> callback);
	
}
