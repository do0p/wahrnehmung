package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TemplateServiceAsync extends ModelServiceAsync<GwtTemplate> {

	void getTemplate(String id, AsyncCallback<GwtTemplate> callback);

	void getTemplates(AsyncCallback<List<GwtTemplate>> callback);

}
