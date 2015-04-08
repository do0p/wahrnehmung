package at.brandl.lws.notice.model;

import java.util.Collection;

public class GwtMultipleChoiceAnswer extends GwtAnswer {

	private static final long serialVersionUID = -6929785388867327337L;
	private Collection<String> values;

	@Override
	public Collection<String> getValue() {
		return values;
	}

	@Override
	public void setValue(Object value) {
		if(!(value instanceof Collection)) {
			throw new IllegalArgumentException("value must be of type Collection<String> but was " + value);
		}
		values = (Collection<String>) value;
	}
}
