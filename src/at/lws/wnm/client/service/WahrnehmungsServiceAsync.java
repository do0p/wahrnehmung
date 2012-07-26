package at.lws.wnm.client.service;

import java.util.List;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>WahrnehmungsService</code>.
 */
public interface WahrnehmungsServiceAsync {

	void storeBeobachtung(GwtBeobachtung beobachtung,
			AsyncCallback<Void> callback);

	void getBeobachtungen(Long childNo, Long sectionNo,
			AsyncCallback<List<GwtBeobachtung>> callback);

	void getBeobachtung(Long beobachtungsKey,
			AsyncCallback<GwtBeobachtung> callback);

}
