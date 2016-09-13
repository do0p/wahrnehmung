package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;

abstract class Dragable<T extends Dragable<T>> extends DragTarget<T> implements
		ChangesAware {

	private static final Logger LOGGER = Logger.getLogger("Dragable");
	private final String key;
	private final Collection<ChangeListener> listeners = new ArrayList<>();

	Dragable(String key, DragContainer parent) {
		super(parent);
		this.key = key;
		getElement().setDraggable(Element.DRAGGABLE_TRUE);
		addDragStartHandler(getDragStartHandler());
		addDragEndHandler(getDragEndHandler());
		for(ChangeListener listener : parent.getChangeListeners()) {
			registerListenerInternal(listener);
		}
	}

	abstract Data getData();

	abstract String getType();

	final DragStartHandler getDragStartHandler() {
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

	final DragEndHandler getDragEndHandler() {
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
				notifyChanges();
			}

		};
	}

	public String getKey() {
		return key;
	}

	@Override
	public void registerChangeListener(ChangeListener listener) {
		registerListenerInternal(listener);
	}

	private void registerListenerInternal(ChangeListener listener) {
		LOGGER.log(Level.INFO, "register listener " + listener);
		listeners.add(listener);
	}

	protected final void notifyChanges() {
		for (ChangeListener listener : listeners) {
			listener.notifyChange();
		}
	}
}
