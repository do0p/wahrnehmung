package at.lws.wnm.client;

import java.util.Date;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class EditContent extends VerticalPanel {

	private final WahrnehmungsServiceAsync wahrnehmungService = GWT
			.create(WahrnehmungsService.class);

	private final TextArea textArea = new TextArea();
	private final DateBox dateBox = new DateBox();
	private final ListBox durationSelection = new ListBox();
	private final ListBox socialSelection = new ListBox();
	private final PopUp dialogBox = new PopUp();

	private NameSelection nameSelection;
	private SectionSelection sectionSelection;

	private Button sendButton;
	private Button newButton;

	private boolean changes;

	private Long key;

	private DecisionBox decisionBox;

	private ListBox additionalNames;

	private Button nameAddButton;

	private Button nameRemoveButton;

	public EditContent(Authorization authorization, int width, Long key) {
		this.key = key;
		init();
		layout(width);
		if (key != null) {
			loadData(key);
		}
	}

	private void loadData(Long key) {
		wahrnehmungService.getBeobachtung(key,
				new AsyncCallback<GwtBeobachtung>() {

					@Override
					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage();
						dialogBox.setDisableWhileShown(sendButton);
						dialogBox.center();
					}

					@Override
					public void onSuccess(GwtBeobachtung result) {
						nameSelection.setSelected(result.getChildKey());
						dateBox.setValue(result.getDate());
						sectionSelection.setSelected(result.getSectionKey());
						final DurationEnum duration = result.getDuration();
						if (duration != null) {
							durationSelection.setSelectedIndex(duration
									.ordinal() + 1);
						}

						final SocialEnum social = result.getSocial();
						if (social != null) {
							socialSelection.setSelectedIndex(social.ordinal() + 1);
						}
						textArea.setText(result.getText());
						changes = false;
						sendButton.setEnabled(false);
					}
				});

	}

	private void init() {

		final ChangeHandler changeHandler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				markChanged();
			}
		};

		nameSelection = new NameSelection(dialogBox);
		nameSelection.getTextBox().addChangeHandler(changeHandler);

		additionalNames = new ListBox(true);

		nameAddButton = new Button("\u2193");
		nameAddButton.setEnabled(key == null);
		nameAddButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				final Long selectedChildKey = nameSelection
						.getSelectedChildKey();
				if (selectedChildKey != null
						&& !isInList(additionalNames,
								selectedChildKey.toString())) {
					additionalNames.addItem(nameSelection.getValue(),
							selectedChildKey.toString());
					nameSelection.reset();
				}
			}

			private boolean isInList(ListBox additionalNames, String value) {
				for (int i = 0; i < additionalNames.getItemCount(); i++) {
					if (additionalNames.getValue(i).equals(value)) {
						return true;
					}
				}
				return false;
			}
		});
		nameRemoveButton = new Button("\u2191");
		nameRemoveButton.setEnabled(key == null);
		nameRemoveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				if (additionalNames.getSelectedIndex() > 0)
					;
				int itemCount = additionalNames.getItemCount();
				for (int i = additionalNames.getSelectedIndex(); i < itemCount; i++) {
					if (additionalNames.isItemSelected(i)) {
						additionalNames.removeItem(i);
						i--;
						itemCount--;
					}
				}
			}
		});

		dateBox.setValue(new Date());
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {

			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				markChanged();
			}
		});

		sectionSelection = new SectionSelection(dialogBox, changeHandler);

		socialSelection.addItem("- Sozialform -", "");
		for (SocialEnum socialForm : SocialEnum.values()) {
			socialSelection.addItem(socialForm.getText(), socialForm.name());
		}
		socialSelection.addChangeHandler(changeHandler);

		durationSelection.addItem("- Dauer -", "");
		for (DurationEnum duration : DurationEnum.values()) {
			durationSelection.addItem(duration.getText(), duration.name());
		}
		durationSelection.addChangeHandler(changeHandler);

		textArea.addChangeHandler(changeHandler);

		decisionBox = new DecisionBox();
		decisionBox
				.setText("Es gibt nicht gespeicherte Eingaben. Fortfahren (Eingaben verlieren)?");
		decisionBox.addOkClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				resetForm();
			}
		});

	}

	private void layout(int width) {

		final HorizontalPanel rootContainer = new HorizontalPanel();
		Utils.formatLeftCenter(this, rootContainer, width, 550);
		add(rootContainer);

		final VerticalPanel nameContainer = new VerticalPanel();
		Utils.formatLeftCenter(rootContainer, nameContainer,
				NameSelection.WIDTH, 550);
		Utils.formatLeftTop(nameContainer, nameSelection, NameSelection.WIDTH,
				Utils.FIELD_HEIGHT);
		final HorizontalPanel nameButtoContainer = new HorizontalPanel();
		nameButtoContainer.add(nameAddButton);
		nameButtoContainer.add(nameRemoveButton);

		Utils.formatCenter(nameContainer, nameButtoContainer,
				NameSelection.WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftTop(nameContainer, additionalNames,
				NameSelection.WIDTH, 500);

		final VerticalPanel contentContainer = new VerticalPanel();
		final int contentWidth = width - NameSelection.WIDTH - 10;
		Utils.formatRightCenter(rootContainer, contentContainer, contentWidth,
				550);

		final HorizontalPanel selectionContainer = new HorizontalPanel();
		Utils.formatCenter(contentContainer, selectionContainer, contentWidth,
				Utils.ROW_HEIGHT);
		for (ListBox sectionSelectionBox : sectionSelection
				.getSectionSelectionBoxes()) {
			Utils.formatCenter(selectionContainer, sectionSelectionBox,
					Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		}

		final HorizontalPanel socialContainer = new HorizontalPanel();
		Utils.formatCenter(contentContainer, socialContainer, contentWidth,
				Utils.ROW_HEIGHT);
		Utils.formatCenter(socialContainer, durationSelection,
				Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatCenter(socialContainer, socialSelection,
				Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);

		textArea.setSize("" + contentWidth + "px", "400px");
		contentContainer.add(textArea);

		Utils.formatCenter(contentContainer, createButtonContainer(),
				contentWidth, Utils.ROW_HEIGHT);

		setSize("" + width + "px", "550px");

	}

	private void resetForm() {
		nameSelection.reset();
		// sectionSelection.reset();
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setValue("");
		additionalNames.clear();
		key = null;
		nameAddButton.setEnabled(true);
		nameRemoveButton.setEnabled(true);
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
		buttonContainer.setWidth("170px");

		sendButton = new Button(Utils.SAVE);
		sendButton.addClickHandler(new SendbuttonHandler());
		sendButton.addStyleName("sendButton");

		newButton = new Button(Utils.NEW);
		newButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (changes) {
					decisionBox.center();
				} else {
					resetForm();
				}
			}
		});
		newButton.addStyleName("sendButton");

		Utils.formatLeftCenter(buttonContainer, sendButton, Utils.BUTTON_WIDTH,
				Utils.ROW_HEIGHT);
		Utils.formatLeftCenter(buttonContainer, newButton, Utils.BUTTON_WIDTH,
				Utils.ROW_HEIGHT);

		return buttonContainer;
	}

	private void markChanged() {
		if (!changes) {
			changes = true;
			sendButton.setEnabled(true);
		}
	}

	private class SendbuttonHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			sendButton.setEnabled(false);
			sendNameToServer();
		}

		private void sendNameToServer() {

			final String text = textArea.getValue();
			String name = nameSelection.getValue();
			Long childKey = nameSelection.getSelectedChildKey();
			if ((Utils.isEmpty(name) || childKey == null)
					&& additionalNames.getItemCount() > 0) {
				name = additionalNames.getItemText(0);
				childKey = Long.valueOf(additionalNames.getValue(0));
				additionalNames.removeItem(0);
			}

			final Long sectionKey = sectionSelection.getSelectedSectionKey();
			final Date date = dateBox.getValue();

			String errorMessage = new String();
			if (Utils.isEmpty(name)) {
				errorMessage = errorMessage + "W&auml;hle einen Namen!<br/>";
			} else if (childKey == null) {
				errorMessage = errorMessage + "Kein Kind mit Name " + name
						+ "!<br/>";
			}
			if (sectionKey == null) {
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
			beobachtung.setKey(key);
			beobachtung.setText(text);
			beobachtung.setChildKey(childKey);
			beobachtung.setSectionKey(sectionKey);
			beobachtung.setDate(date);
			beobachtung.setDuration(getDuration());
			beobachtung.setSocial(getSocialForm());

			for (int i = 0; i < additionalNames.getItemCount(); i++) {
				beobachtung.getAdditionalChildKeys().add(
						Long.valueOf(additionalNames.getValue(i)));
			}

			wahrnehmungService.storeBeobachtung(beobachtung,
					new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage();
							dialogBox.setDisableWhileShown(sendButton);
							dialogBox.center();
							sendButton.setEnabled(true);
						}

						public void onSuccess(Void result) {
							changes = false;
							resetForm();
						}
					});
		}
	}
}
