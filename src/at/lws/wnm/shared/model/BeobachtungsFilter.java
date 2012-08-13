package at.lws.wnm.shared.model;

import java.io.Serializable;

public class BeobachtungsFilter implements Serializable {

	private static final long serialVersionUID = -2596604865356427443L;
	
	private Long childKey;

	public Long getChildKey() {
		return childKey;
	}

	public void setChildKey(Long childKey) {
		this.childKey = childKey;
	}

}
