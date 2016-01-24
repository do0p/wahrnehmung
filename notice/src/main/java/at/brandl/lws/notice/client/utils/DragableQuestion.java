package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

class DragableQuestion extends DragablePanel<DragableQuestion> {

	static final String QUESTION_LABEL = "question";

	private Label label;

	private DragableQuestion(String data, VerticalPanel parent,
			boolean insideGroup) {
		super(parent, insideGroup);
		label = new Label(data);
		add(label);
	}

	@Override
	String getData() {
		return label.getText();
	}

	@Override
	String getType() {
		return QUESTION_LABEL;
	}

	static DragableQuestion valueOf(String data, VerticalPanel parent,
			boolean insideGroup) {

		return new DragableQuestion(data, parent, insideGroup);
	}
}
