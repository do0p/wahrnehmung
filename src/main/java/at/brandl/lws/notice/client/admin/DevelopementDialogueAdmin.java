package at.brandl.lws.notice.client.admin;

import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class DevelopementDialogueAdmin extends AbstractAdminTab {

	private static final int NUMBER_VISIBLE_DATES = 5;
	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat(PredefinedFormat.DATE_MEDIUM);
	
	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);

	private final NameSelection nameSelection;
	private final ListBox dates;
	private final DateBox dateBox;


	public DevelopementDialogueAdmin() {
		super(true);
		nameSelection = new NameSelection(getDialogBox());
		dateBox = new DateBox();
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dates = new ListBox();
		dates.setVisibleItemCount(NUMBER_VISIBLE_DATES);

		layout();

		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				updateDialogueDatesList();
				updateButtonPanel();
			}
		});
		addButtonUpdateChangeHandler(dateBox);

		dates.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateButtonPanel();
			}
		});

		updateButtonPanel();
	}

	private void layout() {

		final Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label(labels().child()));
		grid.setWidget(0, 1, nameSelection);
		grid.setWidget(1, 0, new Label(labels().date()));
		grid.setWidget(1, 1, dateBox);
		
		final VerticalPanel data = new VerticalPanel();
		data.add(grid);
		data.add(getButtonPanel());

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(dates);

		add(root);
	}

	private void updateDialogueDatesList() {
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey == null) {
			return;
		}

		childService.get(childKey, new ErrorReportingCallback<GwtChild>() {

			@Override
			public void onSuccess(GwtChild child) {
				dates.clear();
				List<Date> dialogueDates = child.getDevelopementDialogueDates();
				if (dialogueDates == null || dialogueDates.isEmpty()) {
					return;
				}
				for (int i = dialogueDates.size() - 1; i >= 0; i--) {
					dates.addItem(DATE_FORMAT.format(dialogueDates.get(i)));
				}
			}
		});
	}

	@Override
	void save() {
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey == null) {
			showErrorMessage(labels().noChildSelected());
			return;
		}

		Date developementDialogueDate = dateBox.getValue();
		if (developementDialogueDate == null) {
			showErrorMessage(labels().noDateGiven());
			return;
		}

		childService.addDevelopementDialogueDate(childKey,
				developementDialogueDate, new ErrorReportingCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						updateDialogueDatesList();
					}

				});
	}

	@Override
	void delete() {
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey == null) {
			showErrorMessage(labels().noChildSelected());
			return;
		}

		String date = dates.getValue(dates.getSelectedIndex());
		Date developementDialogueDate = DATE_FORMAT.parse(date);
		childService.deleteDevelopementDialogueDate(childKey,
				developementDialogueDate, new ErrorReportingCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						dateBox.setValue(null);
						updateDialogueDatesList();
					}
				});

	}

	@Override
	void reset() {
		nameSelection.reset();
		dateBox.setValue(null, false);
		dates.clear();

	}

	@Override
	boolean enableSave() {
		return nameSelection.getSelectedChildKey() != null
				&& dateBox.getValue() != null;
	}

	@Override
	boolean enableCancel() {
		return nameSelection.getSelectedChildKey() != null
				|| dateBox.getValue() != null || dates.getSelectedIndex() >= 0;
	}

	@Override
	boolean enableDelete() {
		return dates.getSelectedIndex() >= 0;
	}

}
