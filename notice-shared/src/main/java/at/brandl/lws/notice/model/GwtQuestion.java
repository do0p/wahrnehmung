package at.brandl.lws.notice.model;

public abstract class GwtQuestion {

	private String label;
	private GwtAnswerTemplate template;
	
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
}
