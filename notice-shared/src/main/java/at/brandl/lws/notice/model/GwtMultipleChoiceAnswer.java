package at.brandl.lws.notice.model;

import java.util.Collection;

public class GwtMultipleChoiceAnswer extends GwtAnswer {

	private static final long serialVersionUID = -6929785388867327337L;
	private Collection<GwtMultipleChoiceOption> value;

	@Override
	public Collection<GwtMultipleChoiceOption> getValue() {
		return value;
	}
}
