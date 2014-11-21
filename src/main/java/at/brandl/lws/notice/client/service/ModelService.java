package at.brandl.lws.notice.client.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtModel;

public interface ModelService <T extends GwtModel>{
	List<T> getAll();

	T get(String key);
	
	void store(T model);
	
	void delete(T model);
}
