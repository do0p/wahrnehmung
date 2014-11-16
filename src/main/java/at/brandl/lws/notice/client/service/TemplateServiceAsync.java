package at.brandl.lws.notice.client.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TemplateServiceAsync {

	void getTemplate(String id, AsyncCallback<GwtTemplate> callback);

	void getTemplates(AsyncCallback<List<GwtTemplate>> callback);

	void storeTemplate(GwtTemplate template, AsyncCallback<Void> callback);

	void deleteTemplate(GwtTemplate template, AsyncCallback<Void> callback);
}
