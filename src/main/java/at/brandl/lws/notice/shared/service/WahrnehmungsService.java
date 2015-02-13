package at.brandl.lws.notice.shared.service;

import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.BeobachtungsResult;
import at.brandl.lws.notice.model.GwtBeobachtung;

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
	
	GwtBeobachtung getBeobachtung(String beobachtungsKey);
	
	void deleteBeobachtung(String beobachtungsKey);
	
	String getFileUploadUrl();
	
	boolean fileExists(String filename);
	
}
