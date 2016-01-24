package at.brandl.lws.notice.client.utils;

import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

abstract class DragTargetPanel<T extends DragTargetPanel<T>> extends
		FocusPanel {

	static final String TYPE = "type";
	static final String DATA = "data";
	private static final String STANDARD_COLOR = "#fff";
	private static final String DRAG_OVER_COLOR = "#ffa";
	
	private final VerticalPanel parent;
	private final boolean insideGroup;

	DragTargetPanel(VerticalPanel parent, boolean insideGroup) {
		this.parent = parent;
		this.insideGroup = insideGroup;
		addDragOverHandler(getDragOverHanlder());
		addDragLeaveHandler(getDragLeaveHandler());
		addDropHandler(getDropHandler());
	}

	DragOverHandler getDragOverHanlder() {
		return new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
				getElement().getStyle().setBackgroundColor(DRAG_OVER_COLOR);
				event.stopPropagation();
			}
		};
	}

	DragLeaveHandler getDragLeaveHandler() {
		return new DragLeaveHandler() {
			@Override
			public void onDragLeave(DragLeaveEvent event) {
				getElement().getStyle().setBackgroundColor(STANDARD_COLOR);
				event.stopPropagation();
			}
		};
	}

	DropHandler getDropHandler() {
		final DragTargetPanel<T> target = this;
		return new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				getElement().getStyle().setBackgroundColor(STANDARD_COLOR);
				if (!(insideGroup && event.getData(TYPE).equals(
						DragableQuestionGroup.QUESTION_GROUP_LABEL))) {

					DragablePanel<?> widget = DragablePanelFactory.create(
							event.getData(TYPE), event.getData(DATA), parent,
							insideGroup);
					parent.insert(widget, parent.getWidgetIndex(target));
					event.stopPropagation();
				}
			}
		};
	}

}
