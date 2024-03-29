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
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if (!(value instanceof Collection)) {
			throw new IllegalArgumentException(
					"value must be of type Collection<String> but was " + value);
		}
		values = (Collection<String>) value;
	}

	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + super.hashCode();
		result = result * 17 + ObjectUtils.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if(!(obj instanceof GwtMultipleChoiceAnswer))
		{
			return false;
		}
		
		GwtMultipleChoiceAnswer other = (GwtMultipleChoiceAnswer) obj;
		boolean result = super.equals(other);
		result &= values.equals(other.values);
		return result;
	}
}
