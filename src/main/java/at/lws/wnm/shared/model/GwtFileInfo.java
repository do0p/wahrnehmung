package at.lws.wnm.shared.model;

import java.io.Serializable;

public class GwtFileInfo implements Serializable {

	private static final long serialVersionUID = 7001355377386114077L;
	
	private String filename;
	private String storageFilename;
	private String contentType;
	private String imageUrl;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getStorageFilename() {
		return storageFilename;
	}

	public void setStorageFilename(String storageFilename) {
		this.storageFilename = storageFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public int hashCode() {

		return filename.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GwtFileInfo)) {
			return false;
		}
		GwtFileInfo other = (GwtFileInfo) obj;
		return filename.equals(other.filename);
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
