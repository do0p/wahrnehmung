package at.lws.wnm.client.service;

import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {

	void storeBeobachtung(GwtBeobachtung beobachtung,
			AsyncCallback<Long> callback);

	void getBeobachtungen(BeobachtungsFilter filter, Range range,
			AsyncCallback<BeobachtungsResult> callback);

	void getBeobachtung(Long beobachtungsKey,
			AsyncCallback<GwtBeobachtung> callback);

}
