package at.brandl.lws.notice.shared.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.model.GwtInteraction;

public interface InteractionServiceAsync {
	
	void getInteractions(String childKey, AsyncCallback<List<GwtInteraction>> callback);
}
