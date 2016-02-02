package at.brandl.lws.notice.client.admin;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);

	public AdminContent(Authorization authorization) {
		setSize(Utils.HUNDRED_PERCENT, Utils.APP_WIDTH + Utils.PIXEL);

		if (authorization.isAdmin()) {
			add(new ChildAdmin(), labels.children());
		}
		if (authorization.isEditSections()) {
			add(new SectionAdmin(), labels.sections());
		}
		if (authorization.isAdmin()) {
			add(new AuthorizationAdmin(), labels.user());
		}
		if (authorization.isEditDialogueDates()) {
			add(new DevelopementDialogueAdmin(),
					labels.developementDialogueDates());
		}
		if (authorization.isAdmin()) {
			add(new DnDQuestionnaireAdmin(), labels.forms());
		}
		if (authorization.isSuperUser()) {
			add(new QuestionnaireAdmin(), labels.forms() + " old");
		}
		if (authorization.isAdmin() || authorization.isEditSections() || authorization.isEditDialogueDates()) {
			selectTab(0);
		}
	}
}
