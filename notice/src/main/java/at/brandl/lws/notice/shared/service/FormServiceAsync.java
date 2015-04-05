package at.brandl.lws.notice.shared.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

	void getFormAsString(AsyncCallback<String> callback);

	void storeFormAsString(String formText, AsyncCallback<String> callback);
	
}
