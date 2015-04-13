package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtQuestionnaire;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("form")
public interface FormService extends RemoteService {

	GwtQuestionnaire storeFormAsString(String formText, String sectionKey);

	List<GwtQuestionnaire> getAllForms(String childKey);
}
