package at.lws.wnm.client;

import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel {
	public AdminContent() {
		setSize("100%", "550px");
		add(new ChildAdmin(), "Kinder / Jungendliche", false);
		add(new SectionAdmin(), "Bereiche", false);
		add(new AuthorizationAdmin(), "Benutzer", false);

		
	}
}
