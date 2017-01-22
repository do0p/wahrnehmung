package at.brandl.lws.notice.shared.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.GwtInteraction;

@RemoteServiceRelativePath("interactions")
public interface InteractionService extends RemoteService {
	
	List<GwtInteraction> getInteractions(String childKey, Date fromDate, Date toDate) throws IOException;
}
