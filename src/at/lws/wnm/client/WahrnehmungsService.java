package at.lws.wnm.client;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface WahrnehmungsService extends RemoteService {
	void storeBeobachtung(GwtBeobachtung beobachtung);
}
