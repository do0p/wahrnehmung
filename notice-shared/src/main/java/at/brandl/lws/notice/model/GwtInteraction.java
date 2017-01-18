package at.brandl.lws.notice.model;

import java.io.Serializable;

public class GwtInteraction implements Serializable, Comparable<GwtInteraction> {

	private String childName;
	private int count;

	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int compareTo(GwtInteraction o) {
		return o.count - count;
	}

}
