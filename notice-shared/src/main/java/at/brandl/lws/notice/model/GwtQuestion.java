package at.brandl.lws.notice.model;

public abstract class GwtQuestion<T> {

	private String label;
	
	public abstract T getTemplate();
}
