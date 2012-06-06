package at.lws.wnm.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface WahrnehmungsService extends RemoteService {
	String storeText(String text) throws IllegalArgumentException;
	List<String> getSections();
}
