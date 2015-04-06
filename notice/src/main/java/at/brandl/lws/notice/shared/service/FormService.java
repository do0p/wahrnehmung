package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtQuestionnaire;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("questionnaire")
public interface FormService extends RemoteService {

	String storeFormAsString(String formText);
	
	String getFormAsString();
	
	List<GwtQuestionnaire> getAllForms();
}
