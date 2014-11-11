package at.brandl.lws.notice.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.client.service.WahrnehmungsService;
import at.brandl.lws.notice.client.service.WahrnehmungsServiceAsync;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.FileUploadForm;
import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.RichTextToolbar;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.Authorization;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;
import at.brandl.lws.notice.shared.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.shared.model.GwtBeobachtung.SocialEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class EditContent extends HorizontalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final WahrnehmungsServiceAsync wahrnehmungService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);

	private final RichTextArea textArea;
	private final DateBox dateBox;
	private final ListBox durationSelection;
	private final ListBox socialSelection;
	private final PopUp dialogBox;
	private final NameSelection nameSelection;
	private final SectionSelection sectionSelection;
	private final Button sendButton;
	private final Button newButton;
	private final Button nameAddButton;
	private final Button nameRemoveButton;
	private final ListBox additionalNames;
	private final DecisionBox decisionBox;
	private final FileUploadForm uploadForm;
	private final CheckBox countOnly;

	private boolean changes;
	private String key;
	private RichTextToolbar toolbar;

	public EditContent(Authorization authorization) {

		countOnly = new CheckBox(labels.countOnly());
		textArea = new RichTextArea();
		toolbar = new RichTextToolbar(textArea);
		uploadForm = new FileUploadForm();
		dateBox = new DateBox();
		durationSelection = new ListBox();
		socialSelection = new ListBox();
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);
		additionalNames = new ListBox(true);
		nameAddButton = new Button(Utils.DOWN_ARROW);
		nameRemoveButton = new Button(Utils.UP_ARROW);
		sectionSelection = new SectionSelection(dialogBox);
		decisionBox = new DecisionBox();
		sendButton = new Button(labels.save());
		newButton = new Button(labels.cancel());

		init();
		layout();
		updateState();
	}

	private void init() {

		countOnly.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				updateState();
			}
		});
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				markChanged();
				updateState();
				addNameToTextField();
			}

		});

		nameAddButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				addNameToList();
				updateState();
			}

		});
		nameRemoveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				removeNameFromList();
				updateState();
			}

		});
		dateBox.setValue(new Date());
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dateBox.setFireNullValues(true);
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				markChanged();
				updateState();
			}
		});
		sectionSelection.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				markChanged();
				updateState();
			}
		});

		socialSelection.addItem(Utils.addDashes(labels.socialForm()), "");
		for (GwtBeobachtung.SocialEnum socialForm : GwtBeobachtung.SocialEnum
				.values()) {
			socialSelection.addItem(socialForm.getText(), socialForm.name());
		}
		socialSelection.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				markChanged();
				updateState();
			}
		});

		durationSelection.addItem(Utils.addDashes(labels.duration()), "");
		for (GwtBeobachtung.DurationEnum duration : GwtBeobachtung.DurationEnum
				.values()) {
			durationSelection.addItem(duration.getText(), duration.name());
		}
		durationSelection.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				markChanged();
				updateState();
			}
		});

		textArea.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent arg0) {
				markChanged();
			}
		});
		textArea.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				updateState();
			}
		});
		textArea.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				updateState();
			}
		});
		decisionBox.setText(labels.notSavedWarning());
		decisionBox.addOkClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetForm();
				updateState();
			}
		});
		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendButton.setEnabled(false);
				storeBeobachtung();
				updateState();
			}

		});
		newButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (changes)
					decisionBox.center();
				else
					resetForm();
				updateState();
			}
		});
		uploadForm.setChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				markChanged();
				updateState();
			}
		});
	}

	private void loadData() {
		if (key != null) {
			wahrnehmungService.getBeobachtung(key,
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

	private void layout() {

		final Panel nameContainer = createNameSelectionContainer();
		add(nameContainer);

		final Panel contentContainer = createContentContainer();
		add(contentContainer);

	}

	private void updateView(GwtBeobachtung result) {
		nameSelection.setSelected(result.getChildKey());
		dateBox.setValue(result.getDate());
		sectionSelection.setSelected(result.getSectionKey());
		textArea.setHTML(result.getText());
		uploadForm.setFileInfos(result.getFileInfos());

		GwtBeobachtung.DurationEnum duration = result.getDuration();
		if (duration != null) {
			durationSelection.setSelectedIndex(duration.ordinal() + 1);
		}

		GwtBeobachtung.SocialEnum social = result.getSocial();
		if (social != null) {
			socialSelection.setSelectedIndex(social.ordinal() + 1);
		}

		changes = false;
	}

	private void addNameToList() {
		String selectedChildKey = nameSelection.getSelectedChildKey();
		if ((selectedChildKey != null)
				&& (!isInList(selectedChildKey.toString()))) {
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

	private boolean isInList(String value) {
		for (int i = 0; i < additionalNames.getItemCount(); i++) {
			if (additionalNames.getValue(i).equals(value)) {
				return true;
			}
		}
		return false;
	}

	private VerticalPanel createContentContainer() {

		VerticalPanel contentContainer = new VerticalPanel();
		contentContainer.setSpacing(Utils.SPACING);

		contentContainer.add(countOnly);

		final Panel selectionContainer = createSelectionContainer();
		contentContainer.add(selectionContainer);

		final Panel socialContainer = createSocialContainer();
		contentContainer.add(socialContainer);

		final Panel textArea = createTextArea();
		contentContainer.add(textArea);

		// contentContainer.add(uploadForm);

		final Panel buttonContainer = createButtonContainer();
		Utils.formatCenter(contentContainer, buttonContainer);

		return contentContainer;
	}

	private Grid createTextArea() {
		int textAreaWidth = Utils.APP_WIDTH - Utils.NAMESELECTION_WIDTH - 38;
		int textAreaHeight = Utils.APP_HEIGHT - 305;
		textArea.setSize(textAreaWidth + Utils.PIXEL, textAreaHeight
				+ Utils.PIXEL);

		final Grid grid = new Grid(2, 1);
		grid.setStyleName("cw-RichText");
		grid.setWidget(0, 0, toolbar);
		grid.setWidget(1, 0, textArea);
		return grid;
	}

	private Grid createSocialContainer() {
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

	private Grid createSelectionContainer() {
		final List<SectionSelectionBox> sectionSelectionBoxes = sectionSelection
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

	private VerticalPanel createNameSelectionContainer() {

		final VerticalPanel nameContainer = new VerticalPanel();
		nameContainer.setSpacing(Utils.SPACING);

		nameSelection.setWidth(Utils.NAMESELECTION_WIDTH + Utils.PIXEL);
		nameSelection.setHeight(Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		nameContainer.add(nameSelection);

		final Grid nameButtoContainer = new Grid(1, 2);
		nameButtoContainer.setWidget(0, 0, nameAddButton);
		nameButtoContainer.setWidget(0, 1, nameRemoveButton);
		Utils.formatCenter(nameContainer, nameButtoContainer);

		final int nameSelectionHeight = Utils.APP_HEIGHT - 238;
		additionalNames.setSize(Utils.HUNDRED_PERCENT, nameSelectionHeight
				+ Utils.PIXEL);
		nameContainer.add(additionalNames);

		return nameContainer;
	}

	private void resetForm() {
		nameSelection.reset();
		sectionSelection.reset();
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setText("");
		additionalNames.clear();
		uploadForm.reset();
		key = null;
		changes = false;
		updateState();
	}

	private SocialEnum getSocialForm() {
		int selectedIndex = socialSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			String socialText = socialSelection.getValue(selectedIndex);
			if (!socialText.isEmpty()) {
				return GwtBeobachtung.SocialEnum.valueOf(socialText);
			}
		}
		return null;
	}

	private DurationEnum getDuration() {
		int selectedIndex = durationSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			String durationText = durationSelection.getValue(selectedIndex);
			if (!durationText.isEmpty()) {
				return GwtBeobachtung.DurationEnum.valueOf(durationText);
			}
		}
		return null;
	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 2);

		sendButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		newButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		buttonContainer.setWidget(0, 0, sendButton);
		buttonContainer.setWidget(0, 1, newButton);

		return buttonContainer;
	}

	private void markChanged() {
		if (!changes) {
			changes = true;
		}
	}

	private void storeBeobachtung() {
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
		}
		if (!errorMessage.isEmpty()) {
			dialogBox.setErrorMessage(errorMessage);
			dialogBox.setDisableWhileShown(new FocusWidget[] { sendButton });
			dialogBox.center();
			return;
		}

		GwtBeobachtung beobachtung = new GwtBeobachtung();
		beobachtung.setKey(key);
		beobachtung.setChildKey(childKey);
		beobachtung.setSectionKey(sectionKey);
		beobachtung.setDate(date);

		if (!countOnly.getValue()) {
			beobachtung.setText(cleanUp(text));
			beobachtung.setDuration(getDuration());
			beobachtung.setSocial(getSocialForm());
			beobachtung.setFileInfos(uploadForm.getFileInfos());
		} else {
			beobachtung.setText("");
		}

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

	private void updateState() {

		nameAddButton.setEnabled(enableNameAdd());
		nameRemoveButton.setEnabled(enableNameRemove());
		sendButton.setEnabled(enableSend());
		newButton.setEnabled(enableNew());
		textArea.setEnabled(enableTextArea());
		toolbar.setEnabled(enableToolBar());
		socialSelection.setEnabled(enableSocialSelection());
		durationSelection.setEnabled(enableDurationSelection());
		uploadForm.setEnabled(enableFileUpload());
	}

	private boolean enableFileUpload() {
		return !countOnly.getValue();
	}

	private boolean enableDurationSelection() {
		return !countOnly.getValue();
	}

	private boolean enableSocialSelection() {
		return !countOnly.getValue();
	}

	private boolean enableToolBar() {
		return !countOnly.getValue();
	}

	private boolean enableTextArea() {
		return !countOnly.getValue();
	}

	private boolean enableNew() {
		return changes || key != null;
	}

	private boolean enableSend() {
		return changes
				&& (nameSelection.hasSelection() || additionalNames
						.getItemCount() > 0) && sectionSelection.hasSelection()
				&& dateBox.getValue() != null && countOnly.getValue() ? true
				: (getDuration() != null && getSocialForm() != null && Utils
						.isNotEmpty(cleanUp(textArea.getText())));
	}

	private boolean enableNameRemove() {
		return key == null && additionalNames.getItemCount() > 0;
	}

	private boolean enableNameAdd() {
		return key == null && nameSelection.hasSelection();
	}

	public String getKey() {
		return key;
	}

	private void addNameToTextField() {
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey != null && !isInList(childKey)) {

			String name = nameSelection.getValue();
			name = removeBirthDate(name);

			String text = textArea.getText();
			if (Utils.isNotEmpty(cleanUp(text))) {
				text += ", ";
			}
			text += name;
			textArea.setText(text);
		}

	}

	private String removeBirthDate(String name) {
		int pos = name.indexOf('(');
		if (pos != -1) {
			name = name.substring(0, pos - 1);
		}
		return name;
	}

	public void setKey(String key) {
		this.key = key;
		loadData();
		updateState();
	}
}