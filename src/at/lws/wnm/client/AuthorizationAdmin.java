package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AuthorizationAdmin extends VerticalPanel {

	private final AuthorizationServiceAsync authorizationService = GWT
			.create(AuthorizationService.class);

	private final TextBox userBox;
	private final Button saveButton;
	private final Button cancelButton;
	private final Button deleteButton;
	private final PopUp dialogBox;
	private final SaveSuccess saveSuccess;

	private final ListBox users;

	public AuthorizationAdmin() {

		userBox = new TextBox();
		saveButton = new Button(Utils.ADD);
		deleteButton = new Button(Utils.DEL);
		deleteButton.setEnabled(false);
		cancelButton = new Button(Utils.CANCEL);
		dialogBox = new PopUp();
		saveSuccess = new SaveSuccess();

		final Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label("User"));
		grid.setWidget(0, 1, userBox);

		final VerticalPanel data = new VerticalPanel();
		data.add(grid);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(cancelButton);
		data.add(buttonPanel);

		users = new ListBox(false);
		users.setVisibleItemCount(20);
		users.addClickHandler(new UserClickHandler());
		rebuildUsersList();

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(users);

		add(root);
		saveButton.addClickHandler(new SaveClickHandler());
		cancelButton.addClickHandler(new CancelClickHandler());
		deleteButton.addClickHandler(new DeleteClickHandler());

	}

	private void rebuildUsersList() {
		users.clear();
		authorizationService
				.queryAuthorizations(new AsyncCallback<List<Authorization>>() {

					@Override
					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage(caught.getLocalizedMessage());
						dialogBox.setDisableWhileShown(saveButton);
						dialogBox.center();
					}

					@Override
					public void onSuccess(List<Authorization> result) {
						for (Authorization authorization : result) {
							users.addItem(authorization.getEmail(),
									authorization.getUserId());
						}
					}
				});
	}

	private void resetForm() {
		userBox.setText("");
		if (deleteButton.isEnabled()) {
			deleteButton.setEnabled(false);
		}
		if (!saveButton.isEnabled()) {
			saveButton.setEnabled(true);
		}
	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final Authorization aut = new Authorization();

			final String email = userBox.getValue();
			if (Utils.isEmpty(email)) {
				return;
			}
			aut.setEmail(email);

			authorizationService.storeAuthorization(aut,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage(caught
									.getLocalizedMessage());
							dialogBox.setDisableWhileShown(saveButton);
							dialogBox.center();
						}

						@Override
						public void onSuccess(Void result) {
							saveSuccess.center();
							saveSuccess.show();
							rebuildUsersList();
							resetForm();
						}

					});
		}

	}

	public class UserClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final int selectedIndex = users.getSelectedIndex();
			if (selectedIndex < 0) {
				return;
			}
			userBox.setText(users.getItemText(selectedIndex));
			saveButton.setEnabled(false);
			deleteButton.setEnabled(true);
		}

	}

	public class DeleteClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final String email = userBox.getValue();
			if (Utils.isEmpty(email)) {
				return;
			}

			authorizationService.deleteAuthorization(email,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage(caught
									.getLocalizedMessage());
							dialogBox.setDisableWhileShown(deleteButton);
							dialogBox.center();
						}

						@Override
						public void onSuccess(Void result) {
							rebuildUsersList();
							resetForm();
						}
					});
		}

	}

	public class CancelClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			resetForm();
		}

	}
}
