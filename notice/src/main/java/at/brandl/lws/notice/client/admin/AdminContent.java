package at.brandl.lws.notice.client.admin;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.UIObject;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtAuthorization;

public class AdminContent extends TabPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);

	public AdminContent(GwtAuthorization authorization) {
		setSize(Utils.HUNDRED_PERCENT, Utils.APP_WIDTH + Utils.PIXEL);
		Map<Integer, String> debugIds = new HashMap<Integer, String>();
		int i = 0;
		if (authorization.isAdmin()) {
			ChildAdmin page = new ChildAdmin();
			add(page, labels.children());
			debugIds.put(i++, page.getPageName());
		}
		if (authorization.isEditSections()) {
			SectionAdmin page = new SectionAdmin();
			add(page, labels.sections());
			debugIds.put(i++, page.getPageName());
		}
		if (authorization.isAdmin()) {
			AuthorizationAdmin page = new AuthorizationAdmin();
			add(page, labels.user());
			debugIds.put(i++, page.getPageName());
		}
		if (authorization.isEditDialogueDates()) {
			DevelopementDialogueAdmin page = new DevelopementDialogueAdmin();
			add(page,
					labels.developementDialogueDates());
			debugIds.put(i++, page.getPageName());
		}
		if (authorization.isAdmin()) {
			DnDQuestionnaireAdmin page = new DnDQuestionnaireAdmin();
			add(page, labels.forms());
			debugIds.put(i++, page.getPageName());
		}
		if (authorization.isAdmin() || authorization.isEditSections() || authorization.isEditDialogueDates()) {
			selectTab(0);
		}
		
		for(int j = 0; j < getTabBar().getTabCount(); j++ ) {
			Tab tab = getTabBar().getTab(j);
			if(tab instanceof UIObject) {
				((UIObject) tab).ensureDebugId(debugIds.get(j));
			}
		}
	}
}
