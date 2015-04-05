package at.brandl.lws.notice.model;

import java.util.Date;

public abstract class GwtAnswer {

	private GwtQuestion question;
	private Date date;

	public abstract Object getValue();
}
