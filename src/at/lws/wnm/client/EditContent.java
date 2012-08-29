package at.lws.wnm.client;

import java.util.Date;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class EditContent  extends VerticalPanel{

	private final WahrnehmungsServiceAsync wahrnehmungService = GWT
			.create(WahrnehmungsService.class);

	private final TextArea textArea = new TextArea();
	private final DateBox dateBox = new DateBox();
	private final ListBox durationSelection = new ListBox();
	private final ListBox socialSelection = new ListBox();

	private NameSelection nameSelection;
	private SectionSelection sectionSelection;

	private Button sendButton;
	
//	private SaveSuccess saveSuccess;
	private final PopUp dialogBox = new PopUp();

	public EditContent(Authorization authorization, String width) {
		init();
		layout(width);
	}

	private void init() {
		nameSelection = new NameSelection(dialogBox);
		dateBox.setValue(new Date());

		sectionSelection = new SectionSelection(dialogBox);

		socialSelection.addItem("- Sozialform -", "");
		for (SocialEnum socialForm : SocialEnum.values()) {
			socialSelection.addItem(socialForm.getText(), socialForm.name());
		}

		durationSelection.addItem("- Dauer -", "");
		for (DurationEnum duration : DurationEnum.values()) {
			durationSelection.addItem(duration.getText(), duration.name());
		}
		//saveSuccess = new SaveSuccess();
	}

	private void layout(String width) {

		dateBox.setFormat(Utils.DATEBOX_FORMAT);

		final HorizontalPanel childAndDateContainer = new HorizontalPanel();
		Utils.formatLeftCenter(this, childAndDateContainer, width,
				Utils.ROW_HEIGHT);
		Utils.formatLeftCenter(childAndDateContainer, nameSelection,
				NameSelection.WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(childAndDateContainer, dateBox, "50px",
				Utils.FIELD_HEIGHT);

		final HorizontalPanel selectionContainer = new HorizontalPanel();
		Utils.formatLeftCenter(this, selectionContainer, width,
				Utils.ROW_HEIGHT);
		for (ListBox sectionSelectionBox : sectionSelection
				.getSectionSelectionBoxes()) {
			Utils.formatLeftCenter(selectionContainer, sectionSelectionBox,
					Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		}
		Utils.formatLeftCenter(selectionContainer, durationSelection,
				Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(selectionContainer, socialSelection,
				Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);

		textArea.setSize(width, "400px");
		add(textArea);

		Utils.formatLeftCenter(this, createButtonContainer(), width,
				Utils.ROW_HEIGHT);

		setSize(width, "550px");

	}

	private void resetForm() {
		nameSelection.reset();
		sectionSelection.reset();
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setValue("");
	}

	private SocialEnum getSocialForm() {
		final int selectedIndex = socialSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			final String socialText = socialSelection.getValue(selectedIndex);
			if (!socialText.isEmpty()) {
				return SocialEnum.valueOf(socialText);
			}
		}
		return null;
	}

	private DurationEnum getDuration() {
		final int selectedIndex = durationSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			final String durationText = durationSelection
					.getValue(selectedIndex);
			if (!durationText.isEmpty()) {
				return DurationEnum.valueOf(durationText);
			}
		}
		return null;
	}

	private HorizontalPanel createButtonContainer() {
		final HorizontalPanel buttonContainer = new HorizontalPanel();
		sendButton = new Button(Utils.SAVE);
		sendButton.addClickHandler(new SendbuttonHandler());
		sendButton.addStyleName("sendButton");
		
		Utils.formatLeftCenter(buttonContainer, sendButton, Utils.BUTTON_WIDTH, Utils.ROW_HEIGHT);
		return buttonContainer;
	}

	private class SendbuttonHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			sendNameToServer();
		}

		private void sendNameToServer() {

			final String text = textArea.getValue();
			final Long childKey = nameSelection.getSelectedChildKey();
			final Long sectionKey = sectionSelection.getSelectedSectionKey();
			final Date date = dateBox.getValue();

			String errorMessage = new String();
			if (Utils.isEmpty(nameSelection.getValue())) {
				errorMessage = errorMessage + "W&auml;hle einen Namen!<br/>";
			} else if (childKey == null) {
				errorMessage = errorMessage + "Kein Kind mit Name "
						+ nameSelection.getValue() + "!<br/>";
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
				dialogBox.setErrorMessage(errorMessage);
				dialogBox.setDisableWhileShown(sendButton);
				dialogBox.center();
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
							dialogBox.setErrorMessage();
							dialogBox.setDisableWhileShown(sendButton);
							dialogBox.center();
						}

						public void onSuccess(Void result) {
//							saveSuccess.center();
//							saveSuccess.show();
							resetForm();
						}

					});
		}
	}
}
