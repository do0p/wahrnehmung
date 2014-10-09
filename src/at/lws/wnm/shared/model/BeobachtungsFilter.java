package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.Date;

public class BeobachtungsFilter implements Serializable {

	private static final long serialVersionUID = -2596604865356427443L;
	
	private String childKey;

	private String sectionKey;

	private Date[] timeRange;

	private boolean showEmptyEntries;

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

	public Date[] getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(Date[] timeRange) {
		this.timeRange = timeRange;
	}

	public boolean isShowEmptyEntries() {
		return showEmptyEntries;
	}

	public void setShowEmptyEntries(boolean showEmptyEntries) {
		this.showEmptyEntries = showEmptyEntries;
	}

}
