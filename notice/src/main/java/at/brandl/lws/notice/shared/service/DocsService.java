package at.brandl.lws.notice.shared.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("doc")
public interface DocsService extends RemoteService {

	String printDocumentation(String childKey, boolean overwrite, int year);

}
