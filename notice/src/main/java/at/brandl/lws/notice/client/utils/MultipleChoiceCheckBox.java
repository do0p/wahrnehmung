package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.CheckBox;

import at.brandl.lws.notice.model.GwtMultipleChoiceOption;

public class MultipleChoiceCheckBox extends CheckBox {

	private final String value;
	private final String questionKey;

	public MultipleChoiceCheckBox(GwtMultipleChoiceOption option,
			String questionKey) {
		super(option.getLabel());
		this.questionKey = questionKey;
		value = option.getValue();
		
	}

	public String getQuestionKey() {
		return questionKey;
	}

	public String getOptionValue() {
		return value;
	}

}
