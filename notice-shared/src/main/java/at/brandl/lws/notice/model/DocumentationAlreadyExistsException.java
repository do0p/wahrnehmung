package at.brandl.lws.notice.model;

import java.io.Serializable;


public class DocumentationAlreadyExistsException extends Exception implements Serializable {

	private static final long serialVersionUID = -89669404812185807L;

	private String docUrl;

	/**
	 * This ctor is needed to enable sending to gwt
	 */
	public DocumentationAlreadyExistsException() {
	}

	public DocumentationAlreadyExistsException(String docUrl) {
		this.docUrl = docUrl;
	}

	public String getDocUrl() {
		return docUrl;
	}

}
