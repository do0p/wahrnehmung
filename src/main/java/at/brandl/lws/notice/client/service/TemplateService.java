package at.brandl.lws.notice.client.service;

import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("template")
public interface TemplateService extends ModelService<GwtTemplate>, RemoteService{


}
