package at.brandl.lws.notice.shared.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.BackendServiceException;
import at.brandl.lws.notice.model.DocumentationAlreadyExistsException;
import at.brandl.lws.notice.model.GwtDocumentation;
import at.brandl.lws.notice.model.UserGrantRequiredException;

@RemoteServiceRelativePath("doc")
public interface DocsService extends RemoteService {

	GwtDocumentation createDocumentation(String childKey, int year) throws DocumentationAlreadyExistsException, UserGrantRequiredException, BackendServiceException;

	void deleteDocumentation(String id) throws BackendServiceException;

	List<GwtDocumentation> getDocumentations(String childKey) throws BackendServiceException;
}
