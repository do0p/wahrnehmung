package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Date;

public abstract class GwtAnswer implements Serializable {

	private static final long serialVersionUID = 4409265175378585387L;
	private GwtQuestion question;
	private Date date;

	public abstract Object getValue();
}
