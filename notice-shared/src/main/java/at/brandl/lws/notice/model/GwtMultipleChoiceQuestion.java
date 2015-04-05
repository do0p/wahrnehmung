package at.brandl.lws.notice.model;


public class GwtMultipleChoiceQuestion extends GwtQuestion {


	@Override
	public GwtMultipleChoiceAnswerTemplate getTemplate() {
		
		return (GwtMultipleChoiceAnswerTemplate) super.getTemplate();
	}
	
	@Override
	public void setTemplate(GwtAnswerTemplate template) {
		
		if(!(template instanceof GwtMultipleChoiceAnswerTemplate)) {
			throw new IllegalArgumentException("template must be of type " + GwtMultipleChoiceAnswerTemplate.class.getName());
		}
		super.setTemplate(template);
		
	}
}
