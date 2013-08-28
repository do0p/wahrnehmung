package at.lws.wnm.shared.model;

import java.io.Serializable;

public class BeobachtungsFilter implements Serializable {

	private static final long serialVersionUID = -2596604865356427443L;
	
	private String childKey;

	private String sectionKey;

	public String getChildKey() {
		return childKey;
	}

	public void setChildKey(String childKey) {
		this.childKey = childKey;
	}

	public void setSectionKey(String sectionKey) {
		this.sectionKey = sectionKey;
	}

	public String getSectionKey() {
		return sectionKey;
	}

}
