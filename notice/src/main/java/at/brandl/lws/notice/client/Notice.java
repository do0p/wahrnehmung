package at.brandl.lws.notice.client;

import at.brandl.lws.notice.client.utils.Navigation;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.shared.service.StateParser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;
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

		String stateParam = Location.getParameter("state");
		if (stateParam != null && ((authorization.isAdmin()) || (authorization.isSeeAll()))) {
			Documentation content = (Documentation) navigation
					.getContent(Navigation.DOCUMENTATION_ENTRY);
			setContent(content);

			StateParser state = new StateParser(stateParam);
			content.setChildKey(state.getChildKey());
			content.setYear(state.getYear());
			content.submit();

		} else {
			changePage(History.getToken());
		}
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
			setContent(content);
		}
	}

	private void setContent(Widget content) {
		RootPanel rootPanel = RootPanel.get(Utils.MAIN_ELEMENT);
		rootPanel.clear();
		rootPanel.add(content);
	}
}