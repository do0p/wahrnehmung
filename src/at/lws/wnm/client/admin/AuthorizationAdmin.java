package at.lws.wnm.client.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.lws.wnm.client.service.AuthorizationService;
import at.lws.wnm.client.service.AuthorizationServiceAsync;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AuthorizationAdmin extends AbstractAdminTab {

	private static final int VISIBLE_USERS = 20;
	private final AuthorizationServiceAsync authorizationService = (AuthorizationServiceAsync) GWT
			.create(AuthorizationService.class);

	private final TextBox userBox;
	private final CheckBox adminCheckBox;
	private final CheckBox seeAllCheckBox;
	private final CheckBox editSectionCheckBox;

	private final ListBox users;
	private final Map<String, Authorization> authorizations = new HashMap<String, Authorization>();

	public AuthorizationAdmin() {
		super(true);
		
		this.userBox = new TextBox();
		this.adminCheckBox = new CheckBox(labels().admin());
		this.seeAllCheckBox = new CheckBox(labels().teacher());
		this.editSectionCheckBox = new CheckBox(labels().sectionAdmin());

		this.users = new ListBox(false);
		this.users.setVisibleItemCount(VISIBLE_USERS);
		rebuildUsersList();

		layout();

		addButtonUpdateChangeHandler(userBox);
		addButtonUpdateChangeHandler(adminCheckBox);
		addButtonUpdateChangeHandler(seeAllCheckBox);
		addButtonUpdateChangeHandler(editSectionCheckBox);
		
	
		users.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select();
				updateButtonPanel();
			}
		});
		
		updateButtonPanel();
	}


	private void layout() {
		Panel rights = new VerticalPanel();
		rights.add(this.adminCheckBox);
		rights.add(this.seeAllCheckBox);
		rights.add(this.editSectionCheckBox);

		Grid grid = new Grid(2, 2);
		grid.setWidget(0, 0, new Label(labels().user()));
		grid.setWidget(0, 1, this.userBox);
		grid.setWidget(1, 0, new Label(labels().rights()));
		grid.setWidget(1, 1, rights);

		VerticalPanel data = new VerticalPanel();
		data.add(grid);

		data.add(getButtonPanel());

		HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(this.users);
		add(root);
	}

	private void rebuildUsersList() {
		this.users.clear();
		this.authorizations.clear();
		this.authorizationService
				.queryAuthorizations(new ErrorReportingCallback<Collection<Authorization>>() {
					@Override
					public void onSuccess(Collection<Authorization> result) {
						for (Authorization authorization : result) {
							users.addItem(authorization.getEmail(),
									authorization.getUserId());
							authorizations.put(authorization.getUserId(),
									authorization);
						}
					}
				});
	}

	@Override
	void reset() {
		this.userBox.setText("");
		this.adminCheckBox.setValue(Boolean.valueOf(false));
		this.seeAllCheckBox.setValue(Boolean.valueOf(false));
		this.editSectionCheckBox.setValue(Boolean.valueOf(false));

		getButtonPanel().setSaveButtonLabel(labels().create());
	}

	@Override
	void save() {
		Authorization aut = new Authorization();

		String email = userBox.getValue();
		if (Utils.isEmpty(email)) {
			return;
		}
		aut.setEmail(email);
		aut.setAdmin(adminCheckBox.getValue().booleanValue());
		boolean isTeacher = seeAllCheckBox.getValue().booleanValue();
		aut.setSeeAll(isTeacher);
		aut.setEditSections(editSectionCheckBox.getValue());
		aut.setEditDialogueDates(isTeacher);

		authorizationService.storeAuthorization(aut,
				new ErrorReportingCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						rebuildUsersList();
						reset();
					}
				});
	}

	@Override
	void delete() {
		String email = userBox.getValue();
		if (Utils.isEmpty(email)) {
			return;
		}

		authorizationService.deleteAuthorization(email,
				new ErrorReportingCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						rebuildUsersList();
						reset();
					}
				});
	}

	private void select() {
		int selectedIndex = users.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		Authorization authorization = (Authorization) authorizations.get(users
				.getValue(selectedIndex));
		userBox.setText(users.getItemText(selectedIndex));
		adminCheckBox.setValue(Boolean.valueOf(authorization.isAdmin()));
		seeAllCheckBox.setValue(Boolean.valueOf(authorization.isSeeAll()));
		editSectionCheckBox.setValue(Boolean.valueOf(authorization
				.isEditSections()));

		getButtonPanel().setSaveButtonLabel(labels().change());
	}

	@Override
	boolean enableDelete() {
		return users.getSelectedIndex() != -1;
	}

	@Override
	boolean enableCancel() {
		return Utils.isNotEmpty(userBox.getValue()) || adminCheckBox.getValue()
				|| seeAllCheckBox.getValue() || editSectionCheckBox.getValue();
	}

	@Override
	boolean enableSave() {
		return Utils.isNotEmpty(userBox.getValue());
	}

}