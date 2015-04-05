package at.brandl.lws.notice.shared.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("questionnaire")
public interface FormService extends RemoteService {

	void storeFormAsString(String formText);
	
	String getFormAsString();
}
