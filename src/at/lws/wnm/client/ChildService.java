package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.Child;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("child")
public interface ChildService extends RemoteService {
	List<Child> queryChildren();
}
