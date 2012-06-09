package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.Date;

public class GwtBeobachtung implements Serializable{

	private static final long serialVersionUID = -2732611746367965750L;
	private String text;
	private Long childKey;
	private Long sectionKey;
	private Date date;
	private Long key;

	public void setText(String text) {
		this.text = text;
	}

	public void setChildKey(Long childKey) {
		this.childKey = childKey;
	}

	public void setSectionKey(Long sectionKey) {
		this.sectionKey = sectionKey;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Long getChildKey() {
		return childKey;
	}


	public Long getSectionKey() {
		return sectionKey;
	}


	public String getText() {
		return text;
	}

	public Date getDate() {
		return date;
	}

	public Long getKey() {
		return key;
	}

}
