package at.brandl.lws.notice.model;

import java.util.Collection;
import java.util.List;

public class GwtMultipleChoiceQuestion extends GwtQuestion<Collection<GwtMultipleChoiceOption>> {

	private List<GwtMultipleChoiceOption> options;
	private boolean multipleAnswers;

	@Override
	public Collection<GwtMultipleChoiceOption> getTemplate() {
		
		return options;
	}
	
}
