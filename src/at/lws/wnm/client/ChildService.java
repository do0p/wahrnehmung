package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("child")
public interface ChildService extends RemoteService {
	List<GwtChild> queryChildren();
	
	void storeChild(GwtChild child) throws IllegalArgumentException;
}
