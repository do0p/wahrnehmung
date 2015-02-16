package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.model.GwtTemplate;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("template")
public interface TemplateService extends RemoteService{

	List<GwtTemplate> getTemplates();

	GwtTemplate getTemplate(String id);
}
