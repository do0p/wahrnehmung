package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Date;

public class GwtDocumentation implements Serializable,
		Comparable<GwtDocumentation> {

	private static final long serialVersionUID = 981564571078971411L;

	private String url;
	private String title;
	private Date createDate;
	private String childKey;
	private int year;
	private String id;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getChildKey() {
		return childKey;
	}

	public void setChildKey(String childKey) {
		this.childKey = childKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public int compareTo(GwtDocumentation o) {
		return createDate.compareTo(o.createDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GwtDocumentation)) {
			return false;
		}
		GwtDocumentation other = (GwtDocumentation) obj;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
