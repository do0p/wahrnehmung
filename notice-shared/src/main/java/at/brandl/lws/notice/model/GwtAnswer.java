package at.brandl.lws.notice.model;

import java.util.Date;

public abstract class GwtAnswer <T>{
	
	private GwtChild child;
	private GwtQuestion question;
	private Date date;
	
	public abstract T getValue();
}
