package at.brandl.lws.notice.model;

import java.io.Serializable;

public class GwtQuestion implements Serializable{

	private static final long serialVersionUID = 4489992873551905987L;
	private String label;
	private GwtAnswerTemplate template;
	private String key;
	
	public GwtAnswerTemplate getTemplate() {
		return template;
	}

	public void setTemplate(GwtAnswerTemplate template) {
		this.template = template;
		
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GwtQuestion)) {
			return false;
		}
		GwtQuestion other = (GwtQuestion) obj;
		boolean result = ObjectUtils.equals(label, other.label);
		result &= ObjectUtils.equals(template, other.template);
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(label);
		result = result * 17 + ObjectUtils.hashCode(template);
		return result;
	}
}