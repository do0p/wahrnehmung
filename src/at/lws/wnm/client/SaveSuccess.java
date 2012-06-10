package at.lws.wnm.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class SaveSuccess extends PopupPanel {
	public SaveSuccess() {
		setGlassEnabled(true);
		add(new HTML("erfolgreich gespeichert"));
		setAutoHideEnabled(true);
	}
}
