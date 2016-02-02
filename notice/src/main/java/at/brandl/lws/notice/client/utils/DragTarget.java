package at.brandl.lws.notice.client.utils;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.FocusPanel;

abstract class DragTarget<T extends DragTarget<T>> extends FocusPanel {

	static final Logger LOGGER = Logger.getLogger("DragLogger");

	static final String TYPE = "type";
	static final String DATA = "data";
	private static final String STANDARD_COLOR = "#fff";
	private static final String DRAG_OVER_COLOR = "#ffa";

	private final DragContainer parent;


	DragTarget(DragContainer parent) {
		this.parent = parent;
		addDragOverHandler(getDragOverHanlder());
		addDragLeaveHandler(getDragLeaveHandler());
		addDropHandler(getDropHandler());
	}

	DragOverHandler getDragOverHanlder() {
		return new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
//				LOGGER.log(Level.SEVERE, "in onDragOver of " + this);
				getElement().getStyle().setBackgroundColor(DRAG_OVER_COLOR);
				event.stopPropagation();
			}
		};
	}

	DragLeaveHandler getDragLeaveHandler() {
		return new DragLeaveHandler() {
			@Override
			public void onDragLeave(DragLeaveEvent event) {
//				LOGGER.log(Level.SEVERE, "in onDragLeave of " + this);
				getElement().getStyle().setBackgroundColor(STANDARD_COLOR);
				event.stopPropagation();
			}
		};
	}

	DropHandler getDropHandler() {
		final DragTarget<T> target = this;
		return new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
//				LOGGER.log(Level.SEVERE, "in onDrop of " + this);
				getElement().getStyle().setBackgroundColor(STANDARD_COLOR);
				if (parent.isToplevel() || !event.getData(TYPE).equals(
						DragableQuestionGroup.QUESTION_GROUP_LABEL)) {

					Dragable<?> widget = DragablePanelFactory.create(
							event.getData(TYPE), Data.valueOf(event.getData(DATA)), parent);
					parent.insert(widget, parent.getWidgetIndex(target));
					addToRegister(widget.getKey());
					event.stopPropagation();
				}
			}

		};
	}

	protected void addToRegister(String key) {
//		LOGGER.log(Level.SEVERE, "add '" + key + "' to register");
		parent.addToRegister(key);
	}
	
	protected void removeFromRegister(String key) {
//		LOGGER.log(Level.SEVERE, "remove '" + key + "' from register");
		parent.removeFromRegister(key);
	}
	
	protected boolean isRegistered(String key) {
		return parent.isRegistered(key);
	}
	

	protected void showDecisionBox(ClickHandler clickHandler) {
		parent.showDecisionBox(clickHandler);
	}
}
