package at.lws.wnm.shared.model;

import java.io.Serializable;

public class GwtSection implements Serializable{

	private static final long serialVersionUID = 4741899568904397965L;
	private String sectionName;
	private Long key;
	private Long parentKey;

	public Long getKey() {
		return key;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getParentKey() {
		return parentKey;
	}

	public void setParentKey(Long parentKey) {
		this.parentKey = parentKey;
	}

}
