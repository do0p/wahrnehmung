package at.lws.wnm.client.service;

import java.util.List;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChildServiceAsync {

	void queryChildren(AsyncCallback<List<GwtChild>> callback);

	void storeChild(GwtChild child, AsyncCallback<Void> callback);

	void deleteChild(GwtChild child, AsyncCallback<Void> callback);

	void getChild(Long key, AsyncCallback<GwtChild> callback);

}
