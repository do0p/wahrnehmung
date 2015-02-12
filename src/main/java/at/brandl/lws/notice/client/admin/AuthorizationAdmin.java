package at.brandl.lws.notice.client.admin;

import at.brandl.lws.notice.shared.model.Authorization;
import at.brandl.lws.notice.shared.service.AuthorizationService;
import at.brandl.lws.notice.shared.service.AuthorizationServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AuthorizationAdmin extends AbstractModelAdminTab<Authorization> {

	private static final int VISIBLE_USERS = 20;

	private TextBox userBox;
	private CheckBox adminCheckBox;
	private CheckBox seeAllCheckBox;
	private CheckBox editSectionCheckBox;

	public AuthorizationAdmin() {
		super(true, (AuthorizationServiceAsync) GWT
				.create(AuthorizationService.class));

	}

	@Override
	protected void init() {
		this.userBox = new TextBox();

		userBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getModel().setEmail(userBox.getText());
				updateButtonPanel();
			}
		});
		this.adminCheckBox = new CheckBox(labels().admin());

		adminCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				getModel().setAdmin(adminCheckBox.getValue());
				updateButtonPanel();
			}
		});
		this.seeAllCheckBox = new CheckBox(labels().teacher());

		seeAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				getModel().setEditDialogueDates(seeAllCheckBox.getValue());
				getModel().setSeeAll(seeAllCheckBox.getValue());
				updateButtonPanel();
			}
		});
		this.editSectionCheckBox = new CheckBox(labels().sectionAdmin());
		editSectionCheckBox
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						getModel().setEditSections(
								editSectionCheckBox.getValue());
						updateButtonPanel();
					}
				});
	}

	@Override
	protected Widget createContentLayout() {
		Panel rights = new VerticalPanel();
		rights.add(this.adminCheckBox);
		rights.add(this.seeAllCheckBox);
		rights.add(this.editSectionCheckBox);

		Grid grid = new Grid(2, 2);
		grid.setWidget(0, 0, new Label(labels().user()));
		grid.setWidget(0, 1, this.userBox);
		grid.setWidget(1, 0, new Label(labels().rights()));
		grid.setWidget(1, 1, rights);
		return grid;
	}

	@Override
	protected int getListCount() {
		return VISIBLE_USERS;
	}

	@Override
	protected void updateFields() {
		userBox.setText(getModel().getEmail());
		adminCheckBox.setValue(Boolean.valueOf(getModel().isAdmin()));
		seeAllCheckBox.setValue(Boolean.valueOf(getModel().isSeeAll()));
		editSectionCheckBox.setValue(Boolean.valueOf(getModel()
				.isEditSections()));
	}

	@Override
	protected Authorization createModel() {
		return new Authorization();
	}

	@Override
	protected String getKey(Authorization authorization) {
		return authorization.getKey();
	}

	@Override
	protected String getDisplayName(Authorization authorization) {
		return authorization.getEmail();
	}

}