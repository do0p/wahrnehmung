package at.brandl.lws.notice.client.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ModelServiceAsync <T extends GwtModel> {

	void get(String key, AsyncCallback<T> callback);

	void getAll(AsyncCallback<List<T>> callback);

	void store(T model, AsyncCallback<Void> callback);

	void delete(T model, AsyncCallback<Void> callback);
}
