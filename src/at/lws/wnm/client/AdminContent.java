package at.lws.wnm.client;

import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel {
	public AdminContent() {
		
		add(new ChildAdmin(), "Kinder / Jungendliche", false);
		add(new SectionAdmin(), "Bereiche", false);

		
	}
}
