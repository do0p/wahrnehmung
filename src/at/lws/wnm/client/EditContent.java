package at.lws.wnm.client;

import java.util.Date;
import java.util.List;

import at.lws.wnm.shared.FieldVerifier;
import at.lws.wnm.shared.model.Child;
import at.lws.wnm.shared.model.Section;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
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

	// popup
	private final PopUp dialogBox;

	// main window
	private final TextArea textArea;
	private final DateBox dateBox;
	private final ListBox sectionSelection;
	private final SuggestBox nameSelection;
	private final Button sendButton;

	public EditContent() {
		setSize("744px", "550px");
		// init fields
		sendButton = new Button("Speichern");
		dialogBox = new PopUp(sendButton);
		textArea = new TextArea();
		dateBox = new DateBox();
		dateBox.setValue(new Date());
		sendButton.addStyleName("sendButton");
		sectionSelection = new ListBox();
		createSectionSelectsions();
		nameSelection = new SuggestBox(createChildNameList());
		sendButton.addClickHandler(new SendbuttonHandler());

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

	private void createSectionSelectsions() {
		sectionService.querySections(new AsyncCallback<List<Section>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<Section> result) {
				for (Section section : result) {
					sectionSelection.addItem(section.getSectionName());
				}
			}
		});
		
	}

	private MultiWordSuggestOracle createChildNameList() {
		final MultiWordSuggestOracle names = new MultiWordSuggestOracle();
		childService.queryChildren(new AsyncCallback<List<Child>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<Child> result) {
				for (Child child : result) {
					names.add(formatChildName(child));
				}
			}

			private String formatChildName(Child child) {
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

			final String textToServer = textArea.getText();
			if (!FieldVerifier.isValidName(textToServer)) {
				dialogBox
						.setErrorMessage("Please enter at least four characters");
				dialogBox.center();
				return;
			}

			sendButton.setEnabled(false);

			wahrnehmungService.storeText(textToServer,
					new AsyncCallback<String>() {

						public void onFailure(Throwable caught) {
							dialogBox.setErrorMessage();
							dialogBox.center();
						}

						public void onSuccess(String result) {
							dialogBox.setMessage(result);
							dialogBox.center();
						}
					});
		}
	}
}
