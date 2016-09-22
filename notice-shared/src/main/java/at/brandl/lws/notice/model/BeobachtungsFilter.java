package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class BeobachtungsFilter implements Serializable {

	private static final long serialVersionUID = -2596604865356427443L;
	
	private String childKey;

	private String sectionKey;

	private Date[] timeRange;

	private boolean showEmptyEntries;
	
	private boolean showSummaries;

	private boolean sinceLastDevelopmementDialogue;
	
	private boolean under12;
	
	private boolean over12;
	
	private String user;
	
	private boolean archived;
	
	private boolean aggregateSectionEntries;

	public boolean isAggregateSectionEntries() {
		return aggregateSectionEntries;
	}

	public void setAggregateSectionEntries(boolean aggregateSectionEntries) {
		this.aggregateSectionEntries = aggregateSectionEntries;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

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

	public void setTimeRange(Date... timeRange) {
		this.timeRange = timeRange;
	}

	public boolean isShowEmptyEntries() {
		return showEmptyEntries;
	}

	public void setShowEmptyEntries(boolean showEmptyEntries) {
		this.showEmptyEntries = showEmptyEntries;
	}

	public boolean isShowSummaries() {
		return showSummaries;
	}

	public void setShowSummaries(boolean showSummaries) {
		this.showSummaries = showSummaries;
	}

	public void setSinceLastDevelopmementDialogue(boolean sinceLastDevelopmementDialogue) {
		this.sinceLastDevelopmementDialogue = sinceLastDevelopmementDialogue;
	}

	public boolean isSinceLastDevelopmementDialogue() {
		return sinceLastDevelopmementDialogue;
	}

	public boolean isUnder12() {
		return under12;
	}

	public void setUnder12(boolean under12) {
		this.under12 = under12;
	}

	public boolean isOver12() {
		return over12;
	}

	public void setOver12(boolean over12) {
		this.over12 = over12;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((childKey == null) ? 0 : childKey.hashCode());
		result = prime * result
				+ ((sectionKey == null) ? 0 : sectionKey.hashCode());
		result = prime * result
				+ (sinceLastDevelopmementDialogue ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(timeRange);
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + (archived ? 1 : 0);
		result = prime * result + (aggregateSectionEntries ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeobachtungsFilter other = (BeobachtungsFilter) obj;
		if(archived != other.archived) 
			return false;
		if(aggregateSectionEntries != other.aggregateSectionEntries) {
			return false;
		}
		if (childKey == null) {
			if (other.childKey != null)
				return false;
		} else if (!childKey.equals(other.childKey))
			return false;
		if (sectionKey == null) {
			if (other.sectionKey != null)
				return false;
		} else if (!sectionKey.equals(other.sectionKey))
			return false;
		if (sinceLastDevelopmementDialogue != other.sinceLastDevelopmementDialogue)
			return false;
		if (!Arrays.equals(timeRange, other.timeRange))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Override
	public String toString() {
		return "BeobachtungsFilter [childKey=" + childKey + ", sectionKey="
				+ sectionKey + ", timeRange=" + Arrays.toString(timeRange)
				+ ", showEmptyEntries=" + showEmptyEntries + ", showSummaries="
				+ showSummaries + ", sinceLastDevelopmementDialogue="
				+ sinceLastDevelopmementDialogue + ", under12=" + under12
				+ ", over12=" + over12 + ", user=" + user + ", archived="
				+ archived + ", aggregateSectionEntries="
				+ aggregateSectionEntries + "]";
	}

	
	

}
