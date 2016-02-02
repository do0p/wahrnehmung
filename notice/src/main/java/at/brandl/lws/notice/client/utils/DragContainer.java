package at.brandl.lws.notice.client.utils;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DragContainer extends VerticalPanel {

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
		this.toplevel = false;
	}

	boolean isToplevel() {
		return toplevel;
	}
	
	void addToRegister(String key) {
		register.add(key);
	}
	
	void removeFromRegister(String key) {
		register.remove(key);
	}
	
	boolean isRegistered(String key) {
		return register.contains(key);
	}
	

	void showDecisionBox(ClickHandler clickHandler) {
		decisionBox.addOkClickHandler(clickHandler);
		decisionBox.center();
	}
}
