package at.brandl.lws.notice.model;

import java.io.Serializable;

public interface GwtAnswerTemplate extends Serializable, Cloneable{

	String getKey();

	GwtAnswer createAnswer();
	
	GwtAnswerTemplate clone();

}
