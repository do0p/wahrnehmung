package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DragContainer extends VerticalPanel implements ChangesAware {

	private static final Logger LOGGER = Logger.getLogger("DragContainer");
	private final Collection<ChangeListener> listeners = new ArrayList<>();
	private final Set<String> register;
	private final DecisionBox decisionBox;
	private final boolean toplevel;

	public DragContainer(DecisionBox decisionBox) {
		this.decisionBox = decisionBox;
		this.register = new HashSet<String>();
		this.toplevel = true;
	}

	DragContainer(DragContainer parent) {
		this.decisionBox = parent.decisionBox;
		this.register = parent.register;
		this.listeners.addAll(parent.listeners);
		this.toplevel = false;
	}

	boolean isToplevel() {
		return toplevel;
	}

	void addToRegister(String key) {
		register.add(key);
		notifyChanges();
	}

	void removeFromRegister(String key) {
		register.remove(key);
		notifyChanges();
	}

	boolean isRegistered(String key) {
		return register.contains(key);
	}

	void showDecisionBox(ClickHandler clickHandler) {
		decisionBox.addOkClickHandler(clickHandler);
		decisionBox.center();
	}

	@Override
	public void registerChangeListener(ChangeListener listener) {
		
		LOGGER.log(Level.INFO, "register listener " + listener);
		listeners.add(listener);
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget widget = getWidget(i);
			if (widget instanceof ChangesAware) {
				LOGGER.log(Level.INFO, "register listener on child " + widget);
				((ChangesAware) widget).registerChangeListener(listener);
			} else {
				LOGGER.log(Level.INFO, "not register listener on child " + widget);
			}
		}
	}

	public Collection<ChangeListener> getChangeListeners() {
		return new ArrayList<>(listeners);
	}

	protected final void notifyChanges() {
		for (ChangeListener listener : listeners) {
			listener.notifyChange();
		}
	}
}
