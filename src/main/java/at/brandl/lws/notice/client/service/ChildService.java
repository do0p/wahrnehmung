package at.brandl.lws.notice.client.service;

import java.util.Date;

import at.brandl.lws.notice.shared.model.GwtChild;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("child")
public interface ChildService extends ModelService<GwtChild>, RemoteService {
	
	void addDevelopementDialogueDate(String key, Date date);

	void deleteDevelopementDialogueDate(String key, Date date);

}
