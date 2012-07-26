package at.lws.wnm.client.service;

import java.util.List;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface WahrnehmungsService extends RemoteService {
	void storeBeobachtung(GwtBeobachtung beobachtung);

	List<GwtBeobachtung> getBeobachtungen(Long childNo, Long sectionNo);
	
	GwtBeobachtung getBeobachtung(Long beobachtungsKey);
}
