package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.Date;

public abstract class GwtAnswer implements Serializable {

	private static final long serialVersionUID = 4409265175378585387L;
	private String key;
	private String questionKey;
	private Date date;
	private Date createDate;
	private boolean updated;

	public abstract Object getValue();
	
	public abstract void setValue(Object value);

	public String getQuestionKey() {
		return questionKey;
	}

	public void setQuestionKey(String questionKey) {
		this.questionKey = questionKey;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	@Override
	public String toString() {
	
		return "Answer for question: " + questionKey;
	}
	
	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(date);
		result = result * 17 + ObjectUtils.hashCode(questionKey);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {

		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof GwtAnswer)) {
			return false;
		}
		
		GwtAnswer other = (GwtAnswer) obj;
		
		boolean result = ObjectUtils.equals(questionKey, other.questionKey);
		result &= ObjectUtils.equals(date, other.date);
		
		return result;
	}


}
