package at.lws.wnm.client;

import at.lws.wnm.client.admin.AdminContent;
import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.client.service.AuthorizationServiceAsync;
import at.lws.wnm.shared.model.Authorization;

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
import com.google.gwt.user.client.ui.Widget;

public class Wahrnehmung implements EntryPoint, ValueChangeHandler<String> {

	private static final String ADMIN = "admin";
	private static final String NEW_ENTRY = "new";
	private static final String LIST_ENTRY = "list";

	private final AuthorizationServiceAsync authService = GWT
			.create(AuthorizationService.class);

	private Authorization authorization;

	public void onModuleLoad() {
		authService.getAuthorizationForCurrentUser(Window.Location
				.createUrlBuilder().buildString(),
				new AsyncCallback<Authorization>() {

					@Override
					public void onFailure(Throwable caught) {

					}

					@Override
					public void onSuccess(Authorization authorization) {
						Wahrnehmung.this.authorization = authorization;
						if (authorization.isLoggedIn()) {
							final HorizontalPanel navigation = createNavigation();
							RootPanel.get("navigation").add(navigation);
							History.addValueChangeHandler(Wahrnehmung.this);
							if (History.getToken().isEmpty()) {
								History.newItem(NEW_ENTRY);
							} else {
								changePage(History.getToken());
							}
							RootPanel.get("logout").add(
									new Anchor("logout", authorization
											.getLogoutUrl()));
							RootPanel.get("title").add(new HTML("Wahrnehmung"));
						} else {
							Window.Location.replace(authorization.getLoginUrl());
						}
					}
				});

	}

	private HorizontalPanel createNavigation() {
		final HorizontalPanel navigation = new HorizontalPanel();
		navigation.setSpacing(10);
		navigation.add(new Hyperlink("erfassen", NEW_ENTRY));
		navigation.add(new Hyperlink("anzeigen", LIST_ENTRY));
		if (authorization.isAdmin()) {
			navigation.add(new Hyperlink("administrieren", ADMIN));
		}

		return navigation;
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		changePage(History.getToken());
	}

	private void changePage(String token) {
		final Widget content;
		if (token.equals(NEW_ENTRY)) {
			content = new EditContent(authorization, "850px");
		} else if (token.equals(LIST_ENTRY)) {
			content = new Search(authorization, "850px");
		} else if (token.equals(ADMIN) && authorization.isAdmin()) {
			content = new AdminContent(authorization, "550px");
		} else {
			content = null;
		}

		if (content != null) {
			final RootPanel rootPanel = RootPanel.get("content");
			rootPanel.clear();
			rootPanel.add(content);
		}
	}
}
