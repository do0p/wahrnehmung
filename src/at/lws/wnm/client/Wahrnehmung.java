package at.lws.wnm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;

public class Wahrnehmung implements EntryPoint, ValueChangeHandler<String> {

	private static final String ADMIN = "admin";
	private static final String NEW_ENTRY = "new";

	public void onModuleLoad() {

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
		} else if (token.equals(ADMIN)) {
			rootPanel.add(new AdminContent());
		} else {
			rootPanel.add(new HTML("Hello"));
		}
	}

}
