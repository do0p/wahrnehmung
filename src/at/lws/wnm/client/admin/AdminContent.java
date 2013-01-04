package at.lws.wnm.client.admin;

import at.lws.wnm.client.Labels;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);

	public AdminContent(Authorization authorization, String width) {
		setSize("100%", width);
		if (authorization.isAdmin()) {
			add(new ChildAdmin(), labels.children());
		}
		if ((authorization.isAdmin()) || (authorization.isEditSections())) {
			add(new SectionAdmin(), labels.sections());
		}
		if (authorization.isAdmin()) {
			add(new AuthorizationAdmin(), labels.user());
		}
		if ((authorization.isAdmin()) || (authorization.isEditSections()))
			selectTab(0);
	}
}