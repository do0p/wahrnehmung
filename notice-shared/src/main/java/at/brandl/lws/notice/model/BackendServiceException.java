package at.brandl.lws.notice.model;

import java.io.Serializable;

public class BackendServiceException extends Exception implements Serializable {

	private static final long serialVersionUID = -89669404812185807L;

	/**
	 * This ctor is needed to enable sending to gwt
	 */
	public BackendServiceException() {
	}

	public BackendServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
