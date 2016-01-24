package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

class DragableQuestionGroup extends DragablePanel<DragableQuestionGroup> {

	public static final String QUESTION_GROUP_LABEL = "question group";
	private static final String SEPARATOR = "°°";


	private Label title;
	private VerticalPanel vPanel;
	
	private DragableQuestionGroup(String data, VerticalPanel parent, boolean insideGroup) {
		super(parent, insideGroup);
		vPanel = new VerticalPanel();
		vPanel.setWidth("100%");
		
		String[] dataArray = data.split(SEPARATOR);
		title = new Label(dataArray[0]);
		vPanel.add(title);

		for (int i = 1; i < dataArray.length; i++) {
			DragableQuestion question = DragableQuestion.valueOf(dataArray[i], vPanel, true);
			vPanel.add(question);
		}
		
		vPanel.add(DragTargetLabel.valueOf("end", vPanel, true));
		add(vPanel);
	}

	@Override
	String getData() {

		StringBuilder result = new StringBuilder();
		result.append(title.getText());
		int count = vPanel.getWidgetCount();
		for (int i = 1; i < count - 1; i++) {
			result.append(SEPARATOR);
			result.append(((DragableQuestion)vPanel.getWidget(i)).getData());
		}
		return result.toString();
	}

	@Override
	String getType() {
		return QUESTION_GROUP_LABEL;
	}

	static DragableQuestionGroup valueOf(String data,
			VerticalPanel panel, boolean insideGroup) {
		
		DragableQuestionGroup questionGroup = new DragableQuestionGroup(data, panel, insideGroup);
				return questionGroup;
	}
}
