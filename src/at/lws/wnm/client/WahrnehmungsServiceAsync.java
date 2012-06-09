package at.lws.wnm.client;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {

	void storeBeobachtung(GwtBeobachtung beobachtung,
			AsyncCallback<Void> callback);

}
