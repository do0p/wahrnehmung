package at.lws.wnm.client.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.lws.wnm.client.Labels;
import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.client.service.AuthorizationServiceAsync;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AuthorizationAdmin extends VerticalPanel {
	
	private final AuthorizationServiceAsync authorizationService = (AuthorizationServiceAsync) GWT
			.create(AuthorizationService.class);
	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final TextBox userBox;
	private final CheckBox adminCheckBox;
	private final CheckBox seeAllCheckBox;
	private final CheckBox editSectionCheckBox;
	private final Button saveButton;
	private final Button cancelButton;
	private final Button deleteButton;
	private final PopUp dialogBox;
	private final ListBox users;
	private final Map<String, Authorization> authorizations = new HashMap<String, Authorization>();

	public AuthorizationAdmin() {
		
		this.userBox = new TextBox();
		this.adminCheckBox = new CheckBox(labels.admin());
		this.seeAllCheckBox = new CheckBox(labels.teacher());
		this.editSectionCheckBox = new CheckBox(labels.sectionAdmin());
		this.saveButton = new Button(labels.create());
		this.deleteButton = new Button(labels.delete());
		this.deleteButton.setEnabled(false);
		this.cancelButton = new Button(labels.cancel());
		this.dialogBox = new PopUp();

		Panel rights = new VerticalPanel();
		rights.add(this.adminCheckBox);
		rights.add(this.seeAllCheckBox);
		rights.add(this.editSectionCheckBox);
		Grid grid = new Grid(2, 2);
		grid.setWidget(0, 0, new Label(labels.user()));
		grid.setWidget(0, 1, this.userBox);
		grid.setWidget(1, 0, new Label(labels.rights()));
		grid.setWidget(1, 1, rights);

		VerticalPanel data = new VerticalPanel();
		data.add(grid);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(this.saveButton);
		buttonPanel.add(this.deleteButton);
		buttonPanel.add(this.cancelButton);
		data.add(buttonPanel);

		this.users = new ListBox(false);
		this.users.setVisibleItemCount(20);
		this.users.addClickHandler(new UserClickHandler());
		rebuildUsersList();

		HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(this.users);

		add(root);
		this.saveButton.addClickHandler(new SaveClickHandler());
		this.cancelButton.addClickHandler(new CancelClickHandler());
		this.deleteButton.addClickHandler(new DeleteClickHandler());
	}

	private void rebuildUsersList() {
		this.users.clear();
		this.authorizations.clear();
		this.authorizationService
				.queryAuthorizations(new AsyncCallback<Collection<Authorization>>() {
					public void onFailure(Throwable caught) {
						AuthorizationAdmin.this.dialogBox
								.setErrorMessage(caught.getLocalizedMessage());
						AuthorizationAdmin.this.dialogBox
								.setDisableWhileShown(new FocusWidget[] { AuthorizationAdmin.this.saveButton });
						AuthorizationAdmin.this.dialogBox.center();
					}

					public void onSuccess(Collection<Authorization> result) {
						for (Authorization authorization : result) {
							AuthorizationAdmin.this.users.addItem(
									authorization.getEmail(),
									authorization.getUserId());
							AuthorizationAdmin.this.authorizations.put(
									authorization.getUserId(), authorization);
						}
					}
				});
	}

	private void resetForm() {
		this.userBox.setText("");
		this.adminCheckBox.setValue(Boolean.valueOf(false));
		this.seeAllCheckBox.setValue(Boolean.valueOf(false));
		this.editSectionCheckBox.setValue(Boolean.valueOf(false));
		if (this.deleteButton.isEnabled()) {
			this.deleteButton.setEnabled(false);
		}
		this.saveButton.setHTML("anlegen");
	}

	public class CancelClickHandler implements ClickHandler {
		public CancelClickHandler() {
		}

		public void onClick(ClickEvent event) {
			AuthorizationAdmin.this.resetForm();
		}
	}

	public class DeleteClickHandler implements ClickHandler {
		public DeleteClickHandler() {
		}

		public void onClick(ClickEvent event) {
			String email = AuthorizationAdmin.this.userBox.getValue();
			if (Utils.isEmpty(email)) {
				return;
			}

			AuthorizationAdmin.this.authorizationService.deleteAuthorization(
					email, new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
							AuthorizationAdmin.this.dialogBox
									.setErrorMessage(caught
											.getLocalizedMessage());
							AuthorizationAdmin.this.dialogBox
									.setDisableWhileShown(new FocusWidget[] { AuthorizationAdmin.this.deleteButton });
							AuthorizationAdmin.this.dialogBox.center();
						}

						public void onSuccess(Void result) {
							AuthorizationAdmin.this.rebuildUsersList();
							AuthorizationAdmin.this.resetForm();
						}
					});
		}
	}

	public class SaveClickHandler implements ClickHandler {
		public SaveClickHandler() {
		}

		public void onClick(ClickEvent event) {
			Authorization aut = new Authorization();

			String email = AuthorizationAdmin.this.userBox.getValue();
			if (Utils.isEmpty(email)) {
				return;
			}
			aut.setEmail(email);
			aut.setAdmin(AuthorizationAdmin.this.adminCheckBox.getValue()
					.booleanValue());
			aut.setSeeAll(AuthorizationAdmin.this.seeAllCheckBox.getValue()
					.booleanValue());
			aut.setEditSections(AuthorizationAdmin.this.editSectionCheckBox
					.getValue());

			AuthorizationAdmin.this.authorizationService.storeAuthorization(
					aut, new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
							AuthorizationAdmin.this.dialogBox
									.setErrorMessage(caught
											.getLocalizedMessage());
							AuthorizationAdmin.this.dialogBox
									.setDisableWhileShown(new FocusWidget[] { AuthorizationAdmin.this.saveButton });
							AuthorizationAdmin.this.dialogBox.center();
						}

						public void onSuccess(Void result) {
							AuthorizationAdmin.this.rebuildUsersList();
							AuthorizationAdmin.this.resetForm();
						}
					});
		}
	}

	public class UserClickHandler implements ClickHandler {
		public UserClickHandler() {
		}

		public void onClick(ClickEvent event) {
			int selectedIndex = AuthorizationAdmin.this.users
					.getSelectedIndex();
			if (selectedIndex < 0) {
				return;
			}
			Authorization authorization = (Authorization) AuthorizationAdmin.this.authorizations
					.get(AuthorizationAdmin.this.users.getValue(selectedIndex));
			AuthorizationAdmin.this.userBox
					.setText(AuthorizationAdmin.this.users
							.getItemText(selectedIndex));
			AuthorizationAdmin.this.adminCheckBox.setValue(Boolean
					.valueOf(authorization.isAdmin()));
			AuthorizationAdmin.this.seeAllCheckBox.setValue(Boolean
					.valueOf(authorization.isSeeAll()));
			AuthorizationAdmin.this.editSectionCheckBox.setValue(Boolean
					.valueOf(authorization.isEditSections()));

			AuthorizationAdmin.this.saveButton.setHTML(labels.change());
			AuthorizationAdmin.this.deleteButton.setEnabled(true);
		}
	}
}