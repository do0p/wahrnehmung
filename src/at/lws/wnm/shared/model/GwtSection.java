package at.lws.wnm.shared.model;

import java.io.Serializable;

public class GwtSection implements Serializable{

	private static final long serialVersionUID = 4741899568904397965L;
	private String sectionName;
	private String key;
	private String parentKey;

	public String getKey() {
		return key;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

}
