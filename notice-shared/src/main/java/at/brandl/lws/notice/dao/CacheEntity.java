package at.brandl.lws.notice.dao;

import java.io.Serializable;

public class CacheEntity<T> implements Serializable {
	
	private static final long serialVersionUID = 320234988413201922L;
	private final long version;
	private final T entity;

	public CacheEntity(T entity, long version) {
		this.entity = entity;
		this.version = version;
	}
	
	public T getEntity() {
		return entity;
	}
	
	public long getVersion() {
		return version;
	}
}
