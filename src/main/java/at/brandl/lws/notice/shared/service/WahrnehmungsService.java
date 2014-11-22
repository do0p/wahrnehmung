package at.brandl.lws.notice.shared.service;

import at.brandl.lws.notice.shared.model.BeobachtungsFilter;
import at.brandl.lws.notice.shared.model.BeobachtungsResult;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.view.client.Range;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface WahrnehmungsService extends ModelService<GwtBeobachtung>,
		RemoteService {

	BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter, Range range);

	String getFileUploadUrl();

	boolean fileExists(String filename);

}
