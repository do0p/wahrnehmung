package at.brandl.lws.notice.client.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;

abstract class Dragable<T extends Dragable<T>> extends DragTarget<T> {

	private final String key;

	Dragable(String key, DragContainer parent) {
		super(parent);
		this.key = key;
		getElement().setDraggable(Element.DRAGGABLE_TRUE);
		addDragStartHandler(getDragStartHandler());
		addDragEndHandler(getDragEndHandler());
	}

	abstract Data getData();

	abstract String getType();

	DragStartHandler getDragStartHandler() {
		return new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				// LOGGER.log(Level.SEVERE, "in onDragStart of " + this);
				event.setData(DATA, getData().toString());
				event.setData(TYPE, getType());
				event.getDataTransfer().setDragImage(getElement(), 10, 10);
				removeFromRegister(getKey());
				event.stopPropagation();
			}

		};
	}

	DragEndHandler getDragEndHandler() {
		return new DragEndHandler() {
			@Override
			public void onDragEnd(DragEndEvent event) {
				// LOGGER.log(Level.SEVERE, "in onDragEnd of " + this);
				if (isRegistered(getKey())) {
					removeFromParent();
				} else {
					// LOGGER.log(Level.SEVERE, "deleting " + this);
					showDecisionBox(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							removeFromParent();
						}
					});
				}
				event.stopPropagation();
			}
		};
	}

	public String getKey() {
		return key;
	}
}
