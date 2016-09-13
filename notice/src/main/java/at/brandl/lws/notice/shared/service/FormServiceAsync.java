package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtQuestionnaire;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

	void storeForm(GwtQuestionnaire form, AsyncCallback<GwtQuestionnaire> callback);

	void getAllForms(String childKey, AsyncCallback<List<GwtQuestionnaire>> callback);

//	void deleteForm(String formKey, AsyncCallback<Void> callback);
}