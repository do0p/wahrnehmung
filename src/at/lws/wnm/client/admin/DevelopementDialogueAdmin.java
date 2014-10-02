package at.lws.wnm.client.admin;

import java.util.Date;
import java.util.List;

import at.lws.wnm.client.Labels;
import at.lws.wnm.client.service.ChildService;
import at.lws.wnm.client.service.ChildServiceAsync;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class DevelopementDialogueAdmin extends VerticalPanel {

	private static final int NUMBER_VISIBLE_DATES = 5;
	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final Labels labels = GWT.create(Labels.class);
	private final PopUp dialogBox;
	private final NameSelection nameSelection;
	private final ListBox dates;
	private DateBox dateBox;
	private Button saveButton;
	private Button cancelButton;

	public DevelopementDialogueAdmin() {
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);

		dateBox = new DateBox();
		dateBox.setFormat(Utils.DATEBOX_FORMAT);

		saveButton = new Button(labels.create());
		// deleteButton = new Button(labels.delete());
		// deleteButton.setEnabled(false);
		cancelButton = new Button(labels.cancel());

		dates = new ListBox(false);
		dates.setVisibleItemCount(NUMBER_VISIBLE_DATES);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveButton);
		// buttonPanel.add(deleteButton);
		buttonPanel.add(cancelButton);

		final VerticalPanel data = new VerticalPanel();
		data.add(nameSelection);
		data.add(dateBox);
		data.add(buttonPanel);

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);

		root.add(dates);

		add(root);

		nameSelection.getValueBox().addChangeHandler(new ChildChangeHandler());
		saveButton.addClickHandler(new SaveClickHandler());

	}

	private void updateDialogueDatesList() {
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey == null) {
			return;
		}

		childService.getChild(childKey, new AsyncCallback<GwtChild>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage(caught
						.getLocalizedMessage());
				dialogBox.setDisableWhileShown(saveButton);
				dialogBox.center();
			}

			@Override
			public void onSuccess(GwtChild child) {
				dates.clear();
				List<Date> dialogueDates = child.getDevelopementDialogueDates();
				if(dialogueDates == null || dialogueDates.isEmpty()) {
					return;
				}
				for(int i = dialogueDates.size() -1 ; i >= 0; i--){
					dates.addItem(dialogueDates.get(i).toString());
				}
			}
		});
	}

	
	public class ChildChangeHandler implements ChangeHandler {

		@Override
		public void onChange(ChangeEvent event) {
			updateDialogueDatesList();
		}

		
	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			String childKey = nameSelection.getSelectedChildKey();
			if (childKey == null) {
				dialogBox.setErrorMessage(labels.noChildSelected());
				dialogBox.setDisableWhileShown(saveButton);
				dialogBox.center();
				return;
			}

			Date developementDialogueDate = dateBox.getValue();
			if (developementDialogueDate == null) {
				dialogBox.setErrorMessage(labels.noDateGiven());
				dialogBox.setDisableWhileShown(saveButton);
				dialogBox.center();
				return;
			}

			childService.addDevelopementDialogueDate(childKey,
					developementDialogueDate, new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage(caught
									.getLocalizedMessage());
							dialogBox.setDisableWhileShown(saveButton);
							dialogBox.center();
						}

						@Override
						public void onSuccess(Void result) {
							updateDialogueDatesList();
						}

					});
		}

	}

}
