package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.Child;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChildServiceAsync {

	void queryChildren(AsyncCallback<List<Child>> callback);

	void storeChild(Child child, AsyncCallback<Void> callback);

}
