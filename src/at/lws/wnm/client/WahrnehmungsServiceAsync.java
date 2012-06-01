package at.lws.wnm.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
}
