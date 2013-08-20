package at.lws.wnm.client.utils;

import at.lws.wnm.client.EditContent;
import at.lws.wnm.client.Labels;
import at.lws.wnm.client.Search;
import at.lws.wnm.client.admin.AdminContent;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

public class Navigation extends HorizontalPanel {
	
	private final Labels labels = GWT.create(Labels.class);

	private static final String ADMIN = "admin";
	static final String NEW_ENTRY = "new";
	private static final String LIST_ENTRY = "list";
	private final Authorization authorization;

	public Navigation(Authorization authorization) {
		this.authorization = authorization;
		setSpacing(10);
		add(new Hyperlink(labels.create(), NEW_ENTRY));
		add(new Hyperlink(labels.show(), LIST_ENTRY));
		if ((authorization.isAdmin()) || (authorization.isEditSections()))
			add(new Hyperlink(labels.configure(), ADMIN));
	}

	public Widget getContent(String token) {
		if (token.isEmpty()) {
			token = NEW_ENTRY;
		}
		if (token.equals(NEW_ENTRY)) {

			return new EditContent(this.authorization, null);
		}
		if (token.equals(LIST_ENTRY)) {
			return new Search(this.authorization);
		}
		if (token.equals(ADMIN)) {
			return new AdminContent(this.authorization);
		}
		return null;
	}
}
