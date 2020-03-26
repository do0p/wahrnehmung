package at.brandl.lws.notice.model;

import java.io.Serializable;

import com.google.common.base.MoreObjects;



public class GwtSection implements Serializable, Comparable<GwtSection> {

	private static final long serialVersionUID = 4741899568904397965L;
	private String sectionName;
	private String key;
	private String parentKey;
	private Boolean archived;
	private long pos = -1;

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

	@Override
	public int compareTo(GwtSection o) {
		long diff = sortArchived(this) - sortArchived(o);
		// System.out.println("archvied: " + diff);
		if (diff != 0) {
			return (int) diff;
		}
		if (pos >= 0 && o.getPos() >= 0) {
			diff = pos - o.getPos();
			// System.out.println("pos: " + diff);
			if (diff != 0) {
				return (int) diff;
			}
		}
		return sectionName.compareTo(o.sectionName);
	}

	private int sortArchived(GwtSection gwtSection) {
		return (gwtSection.archived == null || !gwtSection.archived) ? -1 : 1;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("sectionName", sectionName).add("key", key).add("parentKey", parentKey)
				.add("pos", pos).add("archived", archived).toString();
	}

}
