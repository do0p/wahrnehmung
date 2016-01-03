package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Date;

public class GwtDocumentation implements Serializable,
		Comparable<GwtDocumentation> {

	private static final long serialVersionUID = 981564571078971411L;
	
	private String url;
	private String title;
	private Date createDate;
	
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
		if(!(obj instanceof GwtDocumentation)) {
			return false;
		}
		GwtDocumentation other = (GwtDocumentation) obj;
		return url.equals(other.url);
	}
	
	@Override
	public int hashCode() {
		return url.hashCode();
	}
}
