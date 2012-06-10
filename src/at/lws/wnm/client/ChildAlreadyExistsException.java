package at.lws.wnm.client;

import com.google.gwt.rpc.client.impl.RemoteException;

public class ChildAlreadyExistsException extends RemoteException {

	private static final long serialVersionUID = 7442227780628875298L;

	public ChildAlreadyExistsException(Throwable cause) {
		super(cause);
	}
}
