package at.brandl.lws.notice.client.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.user.client.ui.VerticalPanel;

abstract class DragablePanel<T extends DragablePanel<T>> extends DragTargetPanel<T> {

	DragablePanel(VerticalPanel parent, boolean insideGroup) {
		super(parent, insideGroup);
		getElement().setDraggable(Element.DRAGGABLE_TRUE);
		addDragStartHandler(getDragStartHandler());
		addDragEndHandler(getDragEndHandler());
	}

	abstract String getData();

	abstract String getType();
	
	DragStartHandler getDragStartHandler() {
		return new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				event.setData(DATA, getData());
				event.setData(TYPE, getType());
				event.getDataTransfer().setDragImage(getElement(), 10, 10);
				event.stopPropagation();
			}

		};
	}

	DragEndHandler getDragEndHandler() {
		return new DragEndHandler() {
			@Override
			public void onDragEnd(DragEndEvent event) {
				removeFromParent();
				event.stopPropagation();
			}
		};
	}

}
