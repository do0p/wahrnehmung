package at.brandl.lws.notice.client.admin;

import java.util.Date;

import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends AbstractModelAdminTab<GwtChild> {

	private static final int VISIBLE_CHILDREN = 20;

	private TextBox fnBox;
	private TextBox lnBox;
	private DateBox bdBox;

	public ChildAdmin() {
		super(true, (ChildServiceAsync) GWT.create(ChildService.class));

	}

	@Override
	protected void init() {
		fnBox = new TextBox();

		fnBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getModel().setFirstName(fnBox.getText());
				updateButtonPanel();
			}
		});

		lnBox = new TextBox();

		lnBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getModel().setLastName(lnBox.getText());
				updateButtonPanel();
			}
		});

		bdBox = new DateBox();
		bdBox.setFormat(Utils.DATEBOX_FORMAT);
		bdBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				getModel().setBirthDay(bdBox.getValue());
				updateButtonPanel();
			}
		});

	}

	@Override
	protected Widget createContentLayout() {
		final Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label(labels().firstName()));
		grid.setWidget(0, 1, fnBox);
		grid.setWidget(1, 0, new Label(labels().lastName()));
		grid.setWidget(1, 1, lnBox);
		grid.setWidget(2, 0, new Label(labels().birthday()));
		grid.setWidget(2, 1, bdBox);

		return grid;
	}

	@Override
	protected int getListCount() {
		return VISIBLE_CHILDREN;
	}

	@Override
	protected String getDelWarning() {
		return labels().childDelWarning();
	}

	@Override
	protected void updateFields() {
		fnBox.setText(getModel().getFirstName());
		lnBox.setText(getModel().getLastName());
		bdBox.setValue(getModel().getBirthDay());
	}

	@Override
	protected GwtChild createModel() {
		return new GwtChild();
	}

	@Override
	protected String getKey(GwtChild child) {
		return child.getKey();
	}

	@Override
	protected String getDisplayName(GwtChild child) {
		return Utils.formatChildName(child);
	}

}
