package at.lws.wnm.client;

import java.util.Date;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class EditContent extends AbstractTextContent {

	private final WahrnehmungsServiceAsync wahrnehmungService = GWT
			.create(WahrnehmungsService.class);

	private final SaveSuccess saveSuccess;
	private Button sendButton;

	public EditContent() {
		super("850px");
		saveSuccess = new SaveSuccess();
	}

	protected HorizontalPanel createButtonContainer() {
		sendButton = new Button(Utils.SAVE);
		sendButton.addClickHandler(new SendbuttonHandler());
		sendButton.addStyleName("sendButton");
		
		final HorizontalPanel buttonContainer = new HorizontalPanel();
		buttonContainer.add(sendButton);
		formatLeftCenter(buttonContainer, sendButton, BUTTON_WIDTH, ROW_HEIGHT);
		return buttonContainer;
	}

	private class SendbuttonHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			sendNameToServer();
		}

		private void sendNameToServer() {

			final String text = getTextArea().getValue();
			final Long childKey = getSelectedChildKey();
			final Long sectionKey = getSelectedSectionKey();
			final Date date = getDateBox().getValue();

			String errorMessage = new String();
			if (Utils.isEmpty(getNameSelection().getValue())) {
				errorMessage = errorMessage + "W&auml;hle einen Namen!<br/>";
			} else if (childKey == null) {
				errorMessage = errorMessage + "Kein Kind mit Name "
						+ getNameSelection().getValue() + "!<br/>";
			}
			if (sectionKey== null) {
				errorMessage = errorMessage + "W&auml;hle einen Bereich!<br/>";
			}
			if (date == null) {
				errorMessage = errorMessage + "Gib ein Datum an!<br/>";
			}
			if (text.isEmpty()) {
				errorMessage = errorMessage
						+ "Trage eine Wahrnehmung ein!<br/>";
			}
			if (!errorMessage.isEmpty()) {
				getDialogBox().setErrorMessage(errorMessage);
				getDialogBox().setDisableWhileShown(sendButton);
				getDialogBox().center();
				return;
			}

			final GwtBeobachtung beobachtung = new GwtBeobachtung();
			beobachtung.setText(text);
			beobachtung.setChildKey(childKey);
			beobachtung.setSectionKey(sectionKey);
			beobachtung.setDate(date);
			beobachtung.setDuration(getDuration());
			beobachtung.setSocial(getSocialForm());
			wahrnehmungService.storeBeobachtung(beobachtung,
					new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							getDialogBox().setErrorMessage();
							getDialogBox().setDisableWhileShown(sendButton);
							getDialogBox().center();
						}

						public void onSuccess(Void result) {
							saveSuccess.center();
							saveSuccess.show();
							resetForm();
						}

					});
		}
	}
}
