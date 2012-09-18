package at.lws.wnm.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginFailedContent extends VerticalPanel {

	public LoginFailedContent(String loginUrl) {

			add(new Label("Du bist nicht eingeloggt."));
			add(new Anchor("Zur Login Seite", loginUrl));
	}

}
