package at.brandl.lws.notice.client.utils;

import at.brandl.lws.notice.client.EditContent;
import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.Search;
import at.brandl.lws.notice.client.admin.AdminContent;
import at.brandl.lws.notice.shared.model.Authorization;

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

	private AdminContent adminContent;

	private Search search;

	private EditContent editContent;

	public Navigation(Authorization authorization) {
		this.authorization = authorization;
		setSpacing(10);
		add(new Hyperlink(labels.create(), NEW_ENTRY));
		add(new Hyperlink(labels.show(), LIST_ENTRY));
		if ((authorization.isAdmin()) || (authorization.isEditSections())
				|| (authorization.isSeeAll()))
			add(new Hyperlink(labels.configure(), ADMIN));
	}

	public Widget getContent(String token) {
		if (token.isEmpty()) {
			token = NEW_ENTRY;
		}
		if (token.equals(NEW_ENTRY)) {
			return getEditContent();
		}
		if (token.equals(LIST_ENTRY)) {
			return getSearch();
		}
		if (token.equals(ADMIN)) {
			return getAdminContent();
		}
		return null;
	}

	private AdminContent getAdminContent() {
		if (adminContent == null) {
			adminContent = new AdminContent(this.authorization);
		}
		return adminContent;
	}

	private Search getSearch() {
		if (search == null) {
			search = new Search(this.authorization, this);
		}
		return search;
	}

	public EditContent getEditContent() {
		if (editContent == null) {
			editContent = new EditContent(this.authorization);
		}
		return editContent;
	}
}
