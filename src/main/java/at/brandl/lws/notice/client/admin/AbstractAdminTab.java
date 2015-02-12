package at.brandl.lws.notice.client.admin;

import java.util.Date;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

abstract class AbstractAdminTab extends VerticalPanel {

	private final Labels labels;
	private final PopUp dialogBox;
	private final ButtonPanel buttonPanel;

	AbstractAdminTab(boolean showDeleteButton) {
		labels = GWT.create(Labels.class);
		dialogBox = new PopUp();
		buttonPanel = new ButtonPanel(showDeleteButton);
	}

	void save() {
		// override in subclass
	}

	void delete() {
		// override in subclass
	}

	void reset() {
		// override in subclass
	}

	boolean enableDelete() {
		return false;
	}

	boolean enableCancel() {
		return false;
	}

	boolean enableSave() {
		return false;
	}

	void updateButtonPanel() {
		buttonPanel.updateButtonStates();
	}

	void showErrorMessage(String message) {
		dialogBox.setErrorMessage(message);
		dialogBox.setDisableWhileShown(buttonPanel.getButtons());
		dialogBox.center();
	}

	void addButtonUpdateChangeHandler(DateBox checkBox) {
		checkBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				updateButtonPanel();
			}
		});
	}

	Labels labels() {
		return labels;
	}

	PopUp getDialogBox() {
		return dialogBox;
	}

	ButtonPanel getButtonPanel() {
		return buttonPanel;
	}

	class ErrorReportingCallback<T> implements AsyncCallback<T> {

		@Override
		public void onFailure(Throwable caught) {
			showErrorMessage(caught.getLocalizedMessage());
		}

		@Override
		public void onSuccess(T result) {

		}

	}

	class ButtonPanel extends HorizontalPanel {

		private final Button saveButton;
		private final Button cancelButton;
		private final Button deleteButton;
		private final boolean showDeleteButton;

		public ButtonPanel(boolean showDeleteButton) {
			setSpacing(Utils.SPACING);
			this.showDeleteButton = showDeleteButton;
			saveButton = new Button(labels.create());
			deleteButton = new Button(labels().delete());
			cancelButton = new Button(labels().cancel());
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					save();
					updateButtonStates();
				}
			});
			cancelButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					reset();
					updateButtonStates();
				}
			});
			if (showDeleteButton) {
				deleteButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						delete();
						updateButtonStates();
					}
				});

			}

			add(saveButton);
			if (showDeleteButton) {
				add(deleteButton);
			}
			add(cancelButton);
		}

		void updateButtonStates() {
			saveButton.setEnabled(enableSave());
			cancelButton.setEnabled(enableCancel());
			if (showDeleteButton) {
				deleteButton.setEnabled(enableDelete());
			}
		}

		void setSaveButtonLabel(String label) {
			saveButton.setHTML(label);
		}

		private Button[] getButtons() {
			return new Button[] { saveButton, cancelButton, deleteButton };
		}
	}

}