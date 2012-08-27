package at.lws.wnm.client.admin;


import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel {
	public AdminContent(Authorization authorization, String width) {
		setSize("100%", width);
		add(new ChildAdmin(), "Kinder / Jugendliche", false);
		add(new SectionAdmin(), "Bereiche", false);
		add(new AuthorizationAdmin(), "Benutzer", false);

		
	}
}
