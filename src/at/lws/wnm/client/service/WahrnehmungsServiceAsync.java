package at.lws.wnm.client.service;

import java.util.List;

import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {

	void storeBeobachtung(GwtBeobachtung beobachtung,
			AsyncCallback<Void> callback);

	void getBeobachtungen(BeobachtungsFilter filter, Range range,
			AsyncCallback<List<GwtBeobachtung>> callback);

	void getBeobachtung(Long beobachtungsKey,
			AsyncCallback<GwtBeobachtung> callback);

	void getRowCount(BeobachtungsFilter filter, AsyncCallback<Integer> callback);

}
