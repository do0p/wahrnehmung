package at.brandl.lws.notice.shared.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DocsServiceAsync {

	void printDocumentation(String childKey, boolean overwrite, int year, AsyncCallback<String> callback);
}
