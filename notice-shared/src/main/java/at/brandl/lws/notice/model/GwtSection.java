package at.brandl.lws.notice.model;

import java.io.Serializable;

public class GwtSection implements Serializable{

	private static final long serialVersionUID = 4741899568904397965L;
	private String sectionName;
	private String key;
	private String parentKey;
	private Boolean archived;
	private long pos;

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

	public Boolean getArchived() {
		return archived == null ? Boolean.FALSE : archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public long getPos() {
		return pos;
	}

	public void setPos(long pos) {
		this.pos = pos;
	}

}
