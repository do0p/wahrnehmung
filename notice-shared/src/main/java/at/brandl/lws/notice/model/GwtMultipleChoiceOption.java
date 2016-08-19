package at.brandl.lws.notice.model;

import java.io.Serializable;

public class GwtMultipleChoiceOption implements Serializable, Cloneable {

	private static final long serialVersionUID = 8772529463120196686L;
	private String label;
	private String value;
	private String key;

	public static GwtMultipleChoiceOption valueOf(GwtMultipleChoiceOption option) {

		GwtMultipleChoiceOption newOption = new GwtMultipleChoiceOption();
		newOption.setLabel(option.getLabel());
		newOption.setValue(option.getValue());
		return newOption;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		if (!(obj instanceof GwtMultipleChoiceOption)) {
			return false;
		}
		GwtMultipleChoiceOption other = (GwtMultipleChoiceOption) obj;
		boolean result = ObjectUtils.equals(label, other.label);
		result &= ObjectUtils.equals(value, other.value);
		return result;
	}

	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(label);
		result = result * 17 + ObjectUtils.hashCode(value);
		return result;
	}

	@Override
	public GwtMultipleChoiceOption clone() {
		try {
			return (GwtMultipleChoiceOption) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("clone is supported", e);
		}
	}
}
