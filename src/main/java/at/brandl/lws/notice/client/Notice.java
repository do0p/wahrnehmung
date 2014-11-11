package at.brandl.lws.notice.client;

import at.brandl.lws.notice.client.utils.Navigation;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Notice extends SecuredContent implements
		ValueChangeHandler<String> {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private Navigation navigation;

	protected void onLogin(Authorization authorization) {
		this.navigation = new Navigation(authorization);
		RootPanel.get(Utils.NAVIGATION_ELEMENT).add(this.navigation);
		History.addValueChangeHandler(this);
		changePage(History.getToken());
		RootPanel.get(Utils.LOGOUT_ELEMENT).add(
				new Anchor(labels.logout(), authorization.getLogoutUrl()));
	}

	protected void onLogOut(Authorization authorization) {
		RootPanel rootPanel = RootPanel.get(Utils.MAIN_ELEMENT);
		rootPanel.clear();
		rootPanel.add(new LoginFailedContent(authorization.getLoginUrl()));
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		changePage(History.getToken());
	}

	private void changePage(String token) {
		Widget content = this.navigation.getContent(token);
		if (content != null) {
			RootPanel rootPanel = RootPanel.get(Utils.MAIN_ELEMENT);
			rootPanel.clear();
			rootPanel.add(content);
		}
	}
}