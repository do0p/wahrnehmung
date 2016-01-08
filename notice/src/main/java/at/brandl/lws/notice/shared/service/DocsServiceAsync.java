package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtDocumentation;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DocsServiceAsync {

	void createDocumentation(String childKey, int year, AsyncCallback<GwtDocumentation> callback);

	void deleteDocumentation(String fileId, AsyncCallback<Void> callback);
	
	void getDocumentations(String childKey, AsyncCallback<List<GwtDocumentation>> callback);
}
