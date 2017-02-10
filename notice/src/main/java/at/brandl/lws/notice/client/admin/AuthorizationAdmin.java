package at.brandl.lws.notice.client.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.shared.service.AuthorizationService;
import at.brandl.lws.notice.shared.service.AuthorizationServiceAsync;

public class AuthorizationAdmin extends AbstractAdminTab {

	public static final String PAGE_NAME = "UserAdmin";
	private static final int VISIBLE_USERS = 20;
	private final AuthorizationServiceAsync authorizationService = (AuthorizationServiceAsync) GWT
			.create(AuthorizationService.class);

	private final TextBox userBox;
	private final CheckBox adminCheckBox;
	private final CheckBox seeAllCheckBox;
	private final CheckBox editSectionCheckBox;

	private final ListBox users;
	private final Map<String, GwtAuthorization> authorizations = new HashMap<String, GwtAuthorization>();
	private GwtAuthorization aut;

	public AuthorizationAdmin() {
		super(true);

		userBox = new TextBox();
		userBox.ensureDebugId("user");
		adminCheckBox = new CheckBox(labels().admin());
		adminCheckBox.ensureDebugId("admin");
		seeAllCheckBox = new CheckBox(labels().teacher());
		seeAllCheckBox.ensureDebugId("seeAll");
		editSectionCheckBox = new CheckBox(labels().sectionAdmin());
		editSectionCheckBox.ensureDebugId("editSections");
		aut = new GwtAuthorization();

		users = new ListBox();
		users.ensureDebugId("userList");
		users.setVisibleItemCount(VISIBLE_USERS);
		rebuildUsersList();

		layout();

		userBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				aut.setEmail(userBox.getText());
				updateButtonPanel();
			}
		});

		adminCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				aut.setAdmin(convertToBoolean(adminCheckBox.getValue()));
				updateButtonPanel();
			}
		});

		seeAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				boolean seeAll = convertToBoolean(seeAllCheckBox.getValue());
				aut.setSeeAll(seeAll);
				aut.setEditDialogueDates(seeAll);
				updateButtonPanel();
			}
		});

		editSectionCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				aut.setEditSections(convertToBoolean(editSectionCheckBox.getValue()));
				updateButtonPanel();
			}
		});

		users.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select();
				updateButtonPanel();
			}
		});

		updateButtonPanel();

	}

	private boolean convertToBoolean(Boolean value) {
		return value == null ? false : value;
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
		this.authorizationService.queryAuthorizations(new ErrorReportingCallback<Collection<GwtAuthorization>>() {
			@Override
			public void onSuccess(Collection<GwtAuthorization> result) {
				for (GwtAuthorization authorization : result) {
					users.addItem(authorization.getEmail(), authorization.getUserId());
					authorizations.put(authorization.getUserId(), authorization);
				}
			}
		});
	}

	@Override
	void reset() {
		aut = new GwtAuthorization();
		updateForm(aut);
		updateButtonPanel();
		if (users.getSelectedIndex() >= 0) {
			users.setItemSelected(users.getSelectedIndex(), false);
		}
		getButtonPanel().setSaveButtonLabel(labels().create());
	}

	@Override
	void save() {

		authorizationService.storeAuthorization(aut, new ErrorReportingCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				reset();
				rebuildUsersList();
			}
		});
	}

	@Override
	void delete() {

		authorizationService.deleteAuthorization(aut.getEmail(), new ErrorReportingCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				reset();
				rebuildUsersList();
			}
		});
	}

	private void select() {
		int selectedIndex = users.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		aut = (GwtAuthorization) authorizations.get(users.getValue(selectedIndex));
		updateForm(aut);

		getButtonPanel().setSaveButtonLabel(labels().change());
	}

	private void updateForm(GwtAuthorization authorization) {
		userBox.setText(authorization.getEmail());
		adminCheckBox.setValue(Boolean.valueOf(authorization.isAdmin()));
		seeAllCheckBox.setValue(Boolean.valueOf(authorization.isSeeAll()));
		editSectionCheckBox.setValue(Boolean.valueOf(authorization.isEditSections()));
	}

	@Override
	boolean enableDelete() {
		return at.brandl.lws.notice.shared.Utils.isNotEmpty(aut.getEmail()) && users.getSelectedIndex() != -1;
	}

	@Override
	boolean enableCancel() {
		return at.brandl.lws.notice.shared.Utils.isNotEmpty(aut.getEmail()) || aut.isAdmin() || aut.isSeeAll()
				|| aut.isEditSections();
	}

	@Override
	boolean enableSave() {
		return at.brandl.lws.notice.shared.Utils.isNotEmpty(aut.getEmail());
	}

	@Override
	protected String getPageName() {
		return PAGE_NAME;
	}

}