package at.lws.wnm.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtChild;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class EditContent extends VerticalPanel {

	private final WahrnehmungsServiceAsync wahrnehmungService = GWT
			.create(WahrnehmungsService.class);
	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	// popups
	private final PopUp dialogBox;
	private final PopupPanel saveSuccess;

	// main window
	private final TextArea textArea;
	private final DateBox dateBox;
	private final ListBox sectionSelection;
	private final SuggestBox nameSelection;
	private final Button sendButton;

	private final Map<String, Long> childMap = new HashMap<String, Long>();
	
	public EditContent() {
		setSize("744px", "550px");
		// init fields
		sendButton = new Button("Speichern");
		dialogBox = new PopUp();
		textArea = new TextArea();
		dateBox = new DateBox();
		dateBox.setValue(new Date());
		sendButton.addStyleName("sendButton");
		sectionSelection = new ListBox();
		createSectionSelectsions();
		nameSelection = new SuggestBox(createChildNameList());
		sendButton.addClickHandler(new SendbuttonHandler());

		saveSuccess = new PopupPanel();
		saveSuccess.setGlassEnabled(true);
		saveSuccess.add(new HTML("erfolgreich gespeichert"));
		saveSuccess.setAutoHideEnabled(true);

		final HorizontalPanel selectionContainer = new HorizontalPanel();
		selectionContainer.add(nameSelection);
		nameSelection.setSize("260px", "20px");
		selectionContainer.setCellVerticalAlignment(nameSelection,
				HasVerticalAlignment.ALIGN_MIDDLE);
		selectionContainer.add(sectionSelection);
		sectionSelection.setSize("150px", "20px");
		selectionContainer.setCellVerticalAlignment(sectionSelection,
				HasVerticalAlignment.ALIGN_MIDDLE);
		selectionContainer.add(dateBox);
		dateBox.setSize("150px", "20px");
		selectionContainer.setCellHorizontalAlignment(dateBox,
				HasHorizontalAlignment.ALIGN_RIGHT);
		selectionContainer.setCellVerticalAlignment(dateBox,
				HasVerticalAlignment.ALIGN_MIDDLE);

		add(selectionContainer);
		setCellHorizontalAlignment(selectionContainer,
				HasHorizontalAlignment.ALIGN_CENTER);
		setCellVerticalAlignment(selectionContainer,
				HasVerticalAlignment.ALIGN_MIDDLE);
		selectionContainer.setSize("100%", "40px");
		add(textArea);
		textArea.setSize("100%", "440px");
		add(sendButton);
		sendButton.setSize("80px", "40px");
		setCellHorizontalAlignment(sendButton,
				HasHorizontalAlignment.ALIGN_CENTER);
	}

	private void resetForm() {
		nameSelection.setText("");
		sectionSelection.setSelectedIndex(-1);
		textArea.setValue("");
	}

	private void createSectionSelectsions() {
		sectionService.querySections(new AsyncCallback<List<GwtSection>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtSection> result) {
				for (GwtSection section : result) {
					final String keyString = section.getKey().toString();
					sectionSelection.addItem(section.getSectionName(),
							keyString);
				}
			}
		});

	}

	private MultiWordSuggestOracle createChildNameList() {
		final MultiWordSuggestOracle names = new MultiWordSuggestOracle();
		childService.queryChildren(new AsyncCallback<List<GwtChild>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtChild> result) {
				for (GwtChild child : result) {
					
					final String formattedChildName = formatChildName(child);
					names.add(formattedChildName);
					childMap.put(formattedChildName, child.getKey());
				}
			}

			private String formatChildName(GwtChild child) {
				return child.getFirstName() + " " + child.getLastName();
			}

		});
		return names;
	}

	private class SendbuttonHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			sendNameToServer();
		}

		private void sendNameToServer() {

			final String text = textArea.getValue();
			final Long childKey = childMap.get(nameSelection.getValue());
			final String sectionKey = sectionSelection.getValue(sectionSelection
			.getSelectedIndex());
			final Date date = dateBox.getValue();

			String errorMessage = new String();
			if (childKey == null) {
				errorMessage = errorMessage + "Kein Kind mit Name "
						+ nameSelection.getValue() + "\n";
			}
			if (sectionKey == null) {
				errorMessage = errorMessage
						+ "Kein Bereich mit Name "
						+ sectionSelection.getValue(sectionSelection
								.getSelectedIndex()) + "\n";
			}
			if (date == null) {
				errorMessage = errorMessage + "Kein Datum angegeben\n";
			}
			if (text.isEmpty()) {
				errorMessage = errorMessage
						+ "Bitte eine Beobachtung / Wahrnehmung eintragen\n";
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
			beobachtung.setSectionKey(Long.valueOf(sectionKey));
			beobachtung.setDate(date);
			wahrnehmungService.storeBeobachtung(beobachtung,
					new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage();
							dialogBox.setDisableWhileShown(sendButton);
							dialogBox.center();
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
