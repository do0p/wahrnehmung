package at.lws.wnm.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginFailedContent extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	
	public LoginFailedContent(String loginUrl) {

			add(new Label(labels.notLoggedInWarning()));
			add(new Anchor(labels.toLoginPage(), loginUrl));
	}

}
