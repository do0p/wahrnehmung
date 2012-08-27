package at.lws.wnm.client.service;

import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.view.client.Range;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface WahrnehmungsService extends RemoteService {
	
	void storeBeobachtung(GwtBeobachtung beobachtung);

	BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter, Range range);
	
	GwtBeobachtung getBeobachtung(Long beobachtungsKey);
	
}
