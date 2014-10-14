package at.lws.wnm.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.RichTextToolbar;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.SectionSelectionBox;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class EditContent extends HorizontalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final WahrnehmungsServiceAsync wahrnehmungService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);

	private final RichTextArea textArea = new RichTextArea();
	private final DateBox dateBox = new DateBox();
	private final ListBox durationSelection = new ListBox();
	private final ListBox socialSelection = new ListBox();
	private final PopUp dialogBox = new PopUp();
	private NameSelection nameSelection;
	private SectionSelection sectionSelection;
	private Button sendButton;
	private Button newButton;
	private boolean changes;
	private String key;
	private DecisionBox decisionBox;
	private ListBox additionalNames;
	private Button nameAddButton;
	private Button nameRemoveButton;

	public EditContent(Authorization authorization, String key) {
		this.key = key;
		init();
		layout();
		loadData();
	}

	private void loadData() {
		if (key != null) {
			this.wahrnehmungService.getBeobachtung(key,
					new AsyncCallback<GwtBeobachtung>() {
						public void onFailure(Throwable caught) {
							displayErrorMessage();
						}

						public void onSuccess(GwtBeobachtung result) {
							updateView(result);
						}

					});
		}
	}

	private void init() {
		ChangeHandler changeHandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				markChanged();
			}
		};
		this.nameSelection = new NameSelection(this.dialogBox);
		this.nameSelection.getTextBox().addChangeHandler(changeHandler);

		this.additionalNames = new ListBox(true);

		this.nameAddButton = new Button(Utils.DOWN_ARROW);
		this.nameAddButton.setEnabled(this.key == null);
		this.nameAddButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				addNameToList();
			}

		});
		this.nameRemoveButton = new Button(Utils.UP_ARROW);
		this.nameRemoveButton.setEnabled(this.key == null);
		this.nameRemoveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				removeNameFromList();
			}

		});
		this.dateBox.setValue(new Date());
		this.dateBox.setFormat(Utils.DATEBOX_FORMAT);
		this.dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				markChanged();
			}
		});
		this.sectionSelection = new SectionSelection(this.dialogBox);
		sectionSelection.addChangeHandler(changeHandler);

		this.socialSelection.addItem("- " + labels.socialForm() + " -", "");
		for (GwtBeobachtung.SocialEnum socialForm : GwtBeobachtung.SocialEnum
				.values()) {
			this.socialSelection.addItem(socialForm.getText(),
					socialForm.name());
		}
		this.socialSelection.addChangeHandler(changeHandler);

		this.durationSelection.addItem("- " + labels.duration() + " -", "");
		for (GwtBeobachtung.DurationEnum duration : GwtBeobachtung.DurationEnum
				.values()) {
			this.durationSelection.addItem(duration.getText(), duration.name());
		}
		this.durationSelection.addChangeHandler(changeHandler);

		this.textArea.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent arg0) {
				markChanged();
			}
		});
		this.decisionBox = new DecisionBox();
		this.decisionBox.setText(labels.notSavedWarning());
		this.decisionBox.addOkClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetForm();
			}
		});
	}

	private void layout() {

		final Panel nameContainer = createNameSelectionContainer();
		this.add(nameContainer);

		final Panel contentContainer = createContentContainer();
		this.add(contentContainer);

	}

	private void updateView(GwtBeobachtung result) {
		nameSelection.setSelected(result.getChildKey());
		dateBox.setValue(result.getDate());
		sectionSelection.setSelected(result.getSectionKey());
		textArea.setHTML(result.getText());
		
		GwtBeobachtung.DurationEnum duration = result.getDuration();
		if (duration != null) {
			durationSelection.setSelectedIndex(duration.ordinal() + 1);
		}

		GwtBeobachtung.SocialEnum social = result.getSocial();
		if (social != null) {
			socialSelection.setSelectedIndex(social.ordinal() + 1);
		}
		
		changes = false;
		sendButton.setEnabled(false);
	}

	private void addNameToList() {
		String selectedChildKey = nameSelection.getSelectedChildKey();
		if ((selectedChildKey != null)
				&& (!isInList(additionalNames, selectedChildKey.toString()))) {
			additionalNames.addItem(nameSelection.getValue(),
					selectedChildKey.toString());
			nameSelection.reset();
		}
	}

	private void removeNameFromList() {
		if (additionalNames.getSelectedIndex() > 0) {
			int itemCount = additionalNames.getItemCount();
			for (int i = additionalNames.getSelectedIndex(); i < itemCount; i++) {
				if (additionalNames.isItemSelected(i)) {
					additionalNames.removeItem(i);
					i--;
					itemCount--;
				}
			}
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

	public VerticalPanel createContentContainer() {

		VerticalPanel contentContainer = new VerticalPanel();
		contentContainer.setSpacing(Utils.SPACING);

		final Panel selectionContainer = createSelectionContainer();
		// Utils.formatCenter(contentContainer, selectionContainer);
		contentContainer.add(selectionContainer);

		final Panel socialContainer = createSocialContainer();
		// Utils.formatCenter(contentContainer, socialContainer);
		contentContainer.add(socialContainer);

		final Panel textArea = createTextArea();
		contentContainer.add(textArea);

		final Panel buttonContainer = createButtonContainer();
		Utils.formatCenter(contentContainer, buttonContainer);

		return contentContainer;
	}

	public Grid createTextArea() {
		int textAreaWidth = Utils.APP_WIDTH - Utils.NAMESELECTION_WIDTH - 38;
		int textAreaHeight = Utils.APP_HEIGHT - 305;
		this.textArea.setSize(textAreaWidth + Utils.PIXEL, textAreaHeight
				+ Utils.PIXEL);

		final RichTextToolbar toolbar = new RichTextToolbar(this.textArea);

		final Grid grid = new Grid(2, 1);
		grid.setStyleName("cw-RichText");
		grid.setWidget(0, 0, toolbar);
		grid.setWidget(1, 0, this.textArea);
		return grid;
	}

	public Grid createSocialContainer() {
		final Grid socialContainer = new Grid(1, 3);
		int i = 0;
		for (Widget widget : Arrays.asList(durationSelection, socialSelection)) {
			widget.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
					+ Utils.PIXEL);
			socialContainer.setWidget(0, i++, widget);
		}
		dateBox.setSize(Utils.DATEBOX_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				- 12 + Utils.PIXEL);
		socialContainer.setWidget(0, i, dateBox);
		return socialContainer;
	}

	public Grid createSelectionContainer() {
		final List<SectionSelectionBox> sectionSelectionBoxes = this.sectionSelection
				.getSectionSelectionBoxes();
		final Grid selectionContainer = new Grid(1,
				sectionSelectionBoxes.size());
		int i = 0;
		for (SectionSelectionBox sectionSelectionBox : sectionSelectionBoxes) {
			sectionSelectionBox.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL,
					Utils.ROW_HEIGHT + Utils.PIXEL);
			selectionContainer.setWidget(0, i++, sectionSelectionBox);
		}
		return selectionContainer;
	}

	public VerticalPanel createNameSelectionContainer() {

		final VerticalPanel nameContainer = new VerticalPanel();
		nameContainer.setSpacing(Utils.SPACING);

		nameSelection.setWidth(Utils.NAMESELECTION_WIDTH + Utils.PIXEL);
		nameSelection.setHeight(Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		nameContainer.add(nameSelection);

		final Grid nameButtoContainer = new Grid(1, 2);
		nameButtoContainer.setWidget(0, 0, this.nameAddButton);
		nameButtoContainer.setWidget(0, 1, this.nameRemoveButton);
		Utils.formatCenter(nameContainer, nameButtoContainer);

		final int nameSelectionHeight = Utils.APP_HEIGHT - 238;
		additionalNames.setSize(Utils.HUNDRED_PERCENT, nameSelectionHeight
				+ Utils.PIXEL);
		nameContainer.add(additionalNames);

		return nameContainer;
	}

	private void resetForm() {
		this.nameSelection.reset();

		this.durationSelection.setSelectedIndex(0);
		this.socialSelection.setSelectedIndex(0);
		this.textArea.setText("");
		this.additionalNames.clear();
		this.key = null;
		this.nameAddButton.setEnabled(true);
		this.nameRemoveButton.setEnabled(true);
	}

	private GwtBeobachtung.SocialEnum getSocialForm() {
		int selectedIndex = this.socialSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			String socialText = this.socialSelection.getValue(selectedIndex);
			if (!socialText.isEmpty()) {
				return GwtBeobachtung.SocialEnum.valueOf(socialText);
			}
		}
		return null;
	}

	private GwtBeobachtung.DurationEnum getDuration() {
		int selectedIndex = this.durationSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			String durationText = this.durationSelection
					.getValue(selectedIndex);
			if (!durationText.isEmpty()) {
				return GwtBeobachtung.DurationEnum.valueOf(durationText);
			}
		}
		return null;
	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 2);

		this.sendButton = new Button(labels.save());
		sendButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		this.sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendButton.setEnabled(false);
				sendNameToServer();
			}

		});

		this.newButton = new Button(labels.cancel());
		newButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		this.newButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (changes)
					decisionBox.center();
				else
					resetForm();
			}
		});

		buttonContainer.setWidget(0, 0, this.sendButton);
		buttonContainer.setWidget(0, 1, this.newButton);

		return buttonContainer;
	}

	private void markChanged() {
		if (!this.changes) {
			this.changes = true;
			this.sendButton.setEnabled(true);
		}
	}

	private void sendNameToServer() {
		String text = textArea.getHTML();
		String name = nameSelection.getValue();
		String childKey = nameSelection.getSelectedChildKey();
		if (((Utils.isEmpty(name)) || (childKey == null))
				&& (additionalNames.getItemCount() > 0)) {
			name = additionalNames.getItemText(0);
			childKey = additionalNames.getValue(0);
			additionalNames.removeItem(0);
		}

		String sectionKey = sectionSelection.getSelectedSectionKey();
		Date date = dateBox.getValue();

		String errorMessage = new String();
		if (Utils.isEmpty(name))
			errorMessage = errorMessage + labels.noChild() + Utils.LINE_BREAK;
		else if (childKey == null) {
			errorMessage = errorMessage + labels.noChildWithName(name)
					+ Utils.LINE_BREAK;
		}
		if (sectionKey == null) {
			errorMessage = errorMessage + labels.noSection() + Utils.LINE_BREAK;
		}
		if (date == null) {
			errorMessage = errorMessage + labels.noDate() + Utils.LINE_BREAK;
			;
		}
		// if (text.isEmpty()) {
		// errorMessage = errorMessage + labels.noObservation()
		// + Utils.LINE_BREAK;
		// }
		if (!errorMessage.isEmpty()) {
			dialogBox.setErrorMessage(errorMessage);
			dialogBox.setDisableWhileShown(new FocusWidget[] { sendButton });
			dialogBox.center();
			return;
		}

		GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(key);
		beobachtung.setText(cleanUp(text));
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);
		beobachtung.setDuration(getDuration());
		beobachtung.setSocial(getSocialForm());

		for (int i = 0; i < additionalNames.getItemCount(); i++) {
			beobachtung.getAdditionalChildKeys().add(
					additionalNames.getValue(i));
		}

		wahrnehmungService.storeBeobachtung(beobachtung,
				new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						displayErrorMessage();
					}

					public void onSuccess(Void result) {
						changes = false;
						resetForm();
					}
				});
	}

	private void displayErrorMessage() {
		dialogBox.setErrorMessage();
		dialogBox.setDisableWhileShown(sendButton);
		dialogBox.center();
	}

	private String cleanUp(String text) {
		return (text != null && text.equals("<br>")) ? "" : text;
	}
}
