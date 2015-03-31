package at.brandl.lws.notice.model;

import java.util.Collection;

public class GwtMultipleChoiceAnswer extends GwtAnswer<Collection<GwtMultipleChoiceOption>> {

	private Collection<GwtMultipleChoiceOption> value;

	@Override
	public Collection<GwtMultipleChoiceOption> getValue() {
		return value;
	}
}
