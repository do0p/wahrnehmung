package at.lws.wnm.client;

import at.lws.wnm.client.admin.AdminContent;
import at.lws.wnm.client.service.UserService;
import at.lws.wnm.client.service.UserServiceAsync;
import at.lws.wnm.shared.model.GwtUserInfo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;

public class Wahrnehmung implements EntryPoint, ValueChangeHandler<String> {

	private static final String ADMIN = "admin";
	private static final String NEW_ENTRY = "new";
	private static final String LIST_ENTRY = "list";

	private final UserServiceAsync userService = GWT.create(UserService.class);

	public void onModuleLoad() {
		userService.getUserInfo(Window.Location.createUrlBuilder().buildString(), new AsyncCallback<GwtUserInfo>() {
			@Override
			public void onSuccess(GwtUserInfo userInfo) {
				if (userInfo.isLoggedIn()) {
					RootPanel.get("logout").add(
							new Anchor("logout", userInfo.getLogoutUrl()));
				} else {
					RootPanel.get("logout").add(
							new Anchor("login", userInfo.getLoginUrl()));
				}
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
		final HorizontalPanel navigation = createNavigation();
		RootPanel.get("navigation").add(navigation);
		History.addValueChangeHandler(this);
		if (History.getToken().isEmpty()) {
			History.newItem(NEW_ENTRY);
		} else {
			changePage(History.getToken());
		}

	}

	private HorizontalPanel createNavigation() {
		final HorizontalPanel navigation = new HorizontalPanel();
		navigation.setSpacing(10);
		navigation.add(new Hyperlink("erfassen", NEW_ENTRY));
		navigation.add(new Hyperlink("anzeigen", LIST_ENTRY));
		navigation.add(new Hyperlink("administrieren", ADMIN));
		return navigation;
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		changePage(History.getToken());
	}

	private void changePage(String token) {
		final RootPanel rootPanel = RootPanel.get("content");
		rootPanel.clear();
		if (token.equals(NEW_ENTRY)) {
			rootPanel.add(new EditContent());
		} else if (token.equals(LIST_ENTRY)) {
			rootPanel.add(new SplitListContent());
		} else if (token.equals(ADMIN)) {
			rootPanel.add(new AdminContent());
		} else {
			rootPanel.add(new HTML("Hello"));
		}
	}
}
