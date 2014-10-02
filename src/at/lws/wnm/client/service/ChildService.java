package at.lws.wnm.client.service;

import java.util.Date;
import java.util.List;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("child")
public interface ChildService extends RemoteService {
	GwtChild getChild(String key) throws IllegalArgumentException;
	
	List<GwtChild> queryChildren();
	
	void storeChild(GwtChild child) throws IllegalArgumentException;
	
	void deleteChild(GwtChild child) throws IllegalArgumentException;
	
	void addDevelopementDialogueDate(String key, Date date);
}
