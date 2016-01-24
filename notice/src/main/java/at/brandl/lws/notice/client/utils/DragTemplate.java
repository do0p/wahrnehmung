package at.brandl.lws.notice.client.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

public class DragTemplate extends FocusPanel {

	private final String type;
	private final String text;
	private static int count;

	private DragTemplate(String text, String type) {
		this.text = text;
		this.type = type;
		getElement().setDraggable(Element.DRAGGABLE_TRUE);
		add(new Label(text));
		addDragStartHandler(getDragStartHandler());
	}

	private DragStartHandler getDragStartHandler() {
		return new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				event.setData(DragTargetLabel.DATA, text + count++);
				event.setData(DragTargetLabel.TYPE, type);
				event.getDataTransfer().setDragImage(getElement(), 10, 10);
				event.stopPropagation();
			}

		};
	}
	
	public static  DragTemplate createQuestionTemplate() {
		return new DragTemplate("question", DragableQuestion.QUESTION_LABEL);
	}
	
	public static DragTemplate createQuestionGroupTemplate() {
		return new DragTemplate("question group", DragableQuestionGroup.QUESTION_GROUP_LABEL);
	}
}
