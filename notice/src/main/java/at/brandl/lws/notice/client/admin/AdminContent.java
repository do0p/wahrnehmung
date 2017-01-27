package at.brandl.lws.notice.client.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.UIObject;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;

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
		if (authorization.isAdmin() || authorization.isEditSections() || authorization.isEditDialogueDates()) {
			selectTab(0);
		}
		
		for(int i = 0; i < getTabBar().getTabCount(); i++ ) {
			Tab tab = getTabBar().getTab(i);
			if(tab instanceof UIObject) {
				((UIObject) tab).ensureDebugId(Integer.toString(i));
			}
		}
	}
}
