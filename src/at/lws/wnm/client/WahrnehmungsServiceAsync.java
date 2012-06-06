package at.lws.wnm.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {

	void storeText(String text, AsyncCallback<String> callback);

	void getSections(AsyncCallback<List<String>> callback);

}
