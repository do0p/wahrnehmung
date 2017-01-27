package at.brandl.lws.notice.dao;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;

import at.brandl.lws.notice.model.ObjectUtils;

public class CacheKey implements Serializable {
	private static final long serialVersionUID = 1406539907465233216L;
	private final Key key;
	private final Object[] parts;

	public CacheKey(Key key, Object ... parts) {
		this.key = key;
		this.parts = (parts == null ? new Object[0] : parts) ;
	}

	public Key getKey() {
		return key;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ObjectUtils.hashCode(key);
		for (Object keyPart : parts) {
			result = prime * result + ObjectUtils.hashCode(keyPart);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof CacheKey)) {
			return false;
		}
		CacheKey other = (CacheKey) obj;
		if(!ObjectUtils.equals(key, other.key)) {
			return false;
		}
		for(int i = 0; i < parts.length; i++) {
			if(!ObjectUtils.equals(parts[i], other.parts[i])) {
				return false;
			}
		}
		return true;
	}
	

}
