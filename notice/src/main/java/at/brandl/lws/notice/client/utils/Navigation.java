package at.brandl.lws.notice.client.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import at.brandl.lws.notice.client.Documentation;
import at.brandl.lws.notice.client.EditContent;
import at.brandl.lws.notice.client.Interactions;
import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.Questionnaire;
import at.brandl.lws.notice.client.Search;
import at.brandl.lws.notice.client.admin.AdminContent;
import at.brandl.lws.notice.model.GwtAuthorization;

public class Navigation extends HorizontalPanel {

	private final Labels labels = GWT.create(Labels.class);

	private static final String ADMIN = "admin";
	static final String NEW_ENTRY = "new";
	private static final String FORM_ENTRY = "form";
	private static final String LIST_ENTRY = "list";
	private static final String INTERACTIONS_ENTRY = "interactions";
	public static final String DOCUMENTATION_ENTRY = "documentation";

	private final GwtAuthorization authorization;

	private AdminContent adminContent;
	private Search search;
	private EditContent editContent;
	private Documentation documentation;
	private Questionnaire questionnaire;

	private Interactions interactions;

	public Navigation(GwtAuthorization authorization) {
		this.authorization = authorization;
		setSpacing(10);
		add(new Hyperlink(labels.notice(), NEW_ENTRY));
		if (authorization.isSeeAll()) {
			add(new Hyperlink(labels.questionnaire(), FORM_ENTRY));
		}
		add(new Hyperlink(labels.search(), LIST_ENTRY));
		if (authorization.isSeeAll()) {
			add(new Hyperlink(labels.interactions(), INTERACTIONS_ENTRY));
		}
		if (authorization.isSeeAll()) {
			add(new Hyperlink(labels.documentation(), DOCUMENTATION_ENTRY));
		}
		if (authorization.isAdmin() || authorization.isEditSections() || authorization.isSeeAll()) {
			add(new Hyperlink(labels.configuration(), ADMIN));
		}
	}

	public Widget getContent(String token) {
		if (token.isEmpty()) {
			token = NEW_ENTRY;
		}
		if (token.equals(NEW_ENTRY)) {
			return getEditContent();
		}
		if (token.equals(FORM_ENTRY)) {
			return getFormContent();
		}
		if (token.equals(LIST_ENTRY)) {
			return getSearch();
		}
		if (token.equals(INTERACTIONS_ENTRY)) {
			return getInteractions();
		}
		if (token.equals(DOCUMENTATION_ENTRY)) {
			return getDocumentation();
		}
		if (token.equals(ADMIN)) {
			return getAdminContent();
		}
		return null;
	}

	private Widget getInteractions() {
		if (interactions == null) {
			interactions = new Interactions();
		}
		return interactions;
	}

	private Documentation getDocumentation() {
		if (documentation == null) {
			documentation = new Documentation(this);
		}
		return documentation;
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
			editContent = new EditContent();
		}
		return editContent;
	}

	public Questionnaire getFormContent() {
		if (questionnaire == null) {
			questionnaire = new Questionnaire();
		}
		return questionnaire;
	}
}
