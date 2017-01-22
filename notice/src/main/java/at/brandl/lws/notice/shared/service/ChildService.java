package at.brandl.lws.notice.shared.service;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.GwtChild;

@RemoteServiceRelativePath("child")
public interface ChildService extends RemoteService {
	GwtChild getChild(String key) throws IllegalArgumentException;
	
	List<GwtChild> queryChildren();
	
	void storeChild(GwtChild child) throws IllegalArgumentException;
	
	void deleteChild(String key) throws IllegalArgumentException;
	
	void addDevelopementDialogueDate(String key, Date date);

	void deleteDevelopementDialogueDate(String key, Date date);

}
