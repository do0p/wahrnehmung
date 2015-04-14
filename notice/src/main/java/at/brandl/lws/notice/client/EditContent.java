package at.brandl.lws.notice.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.FileUploadForm;
import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.RichTextToolbar;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum;
import at.brandl.lws.notice.shared.service.WahrnehmungsService;
import at.brandl.lws.notice.shared.service.WahrnehmungsServiceAsync;
import at.brandl.lws.notice.shared.validator.GwtBeobachtungValidator;

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
	// private final Button nameAddButton;
	private final Button nameRemoveButton;
	private final ListBox additionalNames;
	private final DecisionBox decisionBox;
	private final FileUploadForm uploadForm;
	private final CheckBox countOnly;

	private boolean changes;
	private RichTextToolbar toolbar;
	private GwtBeobachtung beobachtung;

	public EditContent(Authorization authorization) {

		beobachtung = new GwtBeobachtung();

		countOnly = new CheckBox(labels.countOnly());
		textArea = new RichTextArea();
		toolbar = new RichTextToolbar(textArea);
		uploadForm = new FileUploadForm();
		dateBox = new DateBox();
		durationSelection = new ListBox();
		socialSelection = new ListBox();
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);
		additionalNames = new ListBox();
		// nameAddButton = new Button(Utils.DOWN_ARROW);
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

		beobachtung.setDate(new Date());

		countOnly.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				beobachtung.setCountOnly(countOnly.getValue());
				updateState();
			}
		});

		additionalNames.setMultipleSelect(true);

		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				markChanged();
				addNameToList();
				updateState();
			}

		});

		// nameAddButton.addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent arg0) {
		// addNameToList();
		// updateState();
		// }
		//
		// });
		nameRemoveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				removeNameFromList();
				updateState();
			}

		});

		dateBox.setValue(beobachtung.getDate());
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dateBox.setFireNullValues(true);
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				beobachtung.setDate(dateBox.getValue());
				markChanged();
				updateState();
			}
		});

		sectionSelection.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				beobachtung.setSectionKey(sectionSelection
						.getSelectedSectionKey());
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
				beobachtung.setSocial(getSocialForm());
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
				beobachtung.setDuration(getDuration());
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
				beobachtung.setText(cleanUp(textArea.getHTML()));
				updateState();
			}
		});

		textArea.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				beobachtung.setText(cleanUp(textArea.getHTML()));
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
				if (changes) {
					decisionBox.center();
				} else {
					resetForm();
				}
				updateState();
			}
		});

		uploadForm.setChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				beobachtung.setFileInfos(uploadForm.getFileInfos());
				markChanged();
				updateState();
			}
		});
	}

	private void loadData(String key) {
		wahrnehmungService.getBeobachtung(key,
				new AsyncCallback<GwtBeobachtung>() {
					public void onFailure(Throwable caught) {
						displayErrorMessage();
					}

					public void onSuccess(GwtBeobachtung result) {
						beobachtung = result;
						updateView();
					}

				});
	}

	private void layout() {

		final Panel nameContainer = createNameSelectionContainer();
		add(nameContainer);

		final Panel contentContainer = createContentContainer();
		add(contentContainer);

	}

	private void updateView() {
		nameSelection.setSelected(beobachtung.getChildKey());
		dateBox.setValue(beobachtung.getDate());
		sectionSelection.setSelected(beobachtung.getSectionKey());
		textArea.setHTML(beobachtung.getText());
		uploadForm.setFileInfos(beobachtung.getFileInfos());

		GwtBeobachtung.DurationEnum duration = beobachtung.getDuration();
		if (duration != null) {
			durationSelection.setSelectedIndex(duration.ordinal() + 1);
		}

		GwtBeobachtung.SocialEnum social = beobachtung.getSocial();
		if (social != null) {
			socialSelection.setSelectedIndex(social.ordinal() + 1);
		}

		changes = false;
	}

	private void addNameToList() {
		String selectedChildKey = nameSelection.getSelectedChildKey();
		if (beobachtung.addChild(selectedChildKey)) {
			String name = nameSelection.getValue();
			if (additionalNames.getItemCount() == 1) {
				addNameToTextField(additionalNames.getItemText(0));
			}
			if (additionalNames.getItemCount() > 0) {
				addNameToTextField(name);
			}
			additionalNames.addItem(name, selectedChildKey.toString());
			nameSelection.reset();
		}
	}

	private void removeNameFromList() {
		if (additionalNames.getSelectedIndex() >= 0) {
			int itemCount = additionalNames.getItemCount();
			for (int i = additionalNames.getSelectedIndex(); i < itemCount; i++) {
				if (additionalNames.isItemSelected(i)) {
					beobachtung.removeChild(additionalNames.getValue(i));
					additionalNames.removeItem(i);
					i--;
					itemCount--;
				}
			}
		}
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
		final Grid socialContainer = new Grid(1, 2);
		int i = 0;
		for (Widget widget : Arrays.asList(durationSelection, socialSelection)) {
			widget.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
					+ Utils.PIXEL);
			socialContainer.setWidget(0, i++, widget);
		}
		dateBox.setSize(Utils.DATEBOX_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				- 12 + Utils.PIXEL);

		return socialContainer;
	}

	private Grid createSelectionContainer() {
		final List<SectionSelectionBox> sectionSelectionBoxes = sectionSelection
				.getSectionSelectionBoxes();
		final Grid selectionContainer = new Grid(1,
				sectionSelectionBoxes.size() + 1);
		int i = 0;
		for (SectionSelectionBox sectionSelectionBox : sectionSelectionBoxes) {
			sectionSelectionBox.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL,
					Utils.ROW_HEIGHT + Utils.PIXEL);
			selectionContainer.setWidget(0, i++, sectionSelectionBox);
		}
		selectionContainer.setWidget(0, i, dateBox);
		return selectionContainer;
	}

	private VerticalPanel createNameSelectionContainer() {

		final VerticalPanel nameContainer = new VerticalPanel();
		nameContainer.setSpacing(Utils.SPACING);

		nameSelection.setWidth(Utils.NAMESELECTION_WIDTH + Utils.PIXEL);
		nameSelection.setHeight(Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		nameContainer.add(nameSelection);

		// final Grid nameButtoContainer = new Grid(1, 2);
		// nameButtoContainer.setWidget(0, 0, nameAddButton);
		// nameButtoContainer.setWidget(0, 1, nameRemoveButton);
		Utils.formatCenter(nameContainer, nameRemoveButton);

		final int nameSelectionHeight = Utils.APP_HEIGHT - 238;
		additionalNames.setSize(Utils.HUNDRED_PERCENT, nameSelectionHeight
				+ Utils.PIXEL);
		nameContainer.add(additionalNames);

		return nameContainer;
	}

	private void resetForm() {
		sectionSelection.reset();
		dateBox.setValue(new Date());
		clearForNext();
	}

	private void clearForNext() {
		nameSelection.reset();
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setText("");
		additionalNames.clear();
		uploadForm.reset();
		changes = false;
		beobachtung = new GwtBeobachtung();
		beobachtung.setDate(dateBox.getValue());
		beobachtung.setSectionKey(sectionSelection.getSelectedSectionKey());
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

		if (countOnly.getValue()) {
			beobachtung.setText("");
			beobachtung.setDuration(null);
			beobachtung.setSocial(null);
			beobachtung.setFileInfos(null);
		}

		wahrnehmungService.storeBeobachtung(beobachtung,
				new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						displayErrorMessage();
					}

					public void onSuccess(Void result) {
						changes = false;
						clearForNext();
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

		// nameAddButton.setEnabled(enableNameAdd());
		nameRemoveButton.setEnabled(enableNameRemove());
		sendButton.setEnabled(enableSend());
		newButton.setEnabled(true);
		textArea.setEnabled(enableTextArea());
		textArea.setVisible(enableTextArea());
		toolbar.setEnabled(enableToolBar());
		toolbar.setVisible(enableToolBar());
		socialSelection.setEnabled(enableSocialSelection());
		socialSelection.setVisible(enableSocialSelection());
		durationSelection.setEnabled(enableDurationSelection());
		durationSelection.setVisible(enableDurationSelection());
		uploadForm.setEnabled(enableFileUpload());
		// uploadForm.setVisible(enableFileUpload());
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

	private boolean enableSend() {
		return changes && GwtBeobachtungValidator.valid(beobachtung);
	}

	private boolean enableNameRemove() {
		return beobachtung.getKey() == null
				&& additionalNames.getItemCount() > 0;
	}

	// private boolean enableNameAdd() {
	// return beobachtung.getKey() == null && nameSelection.hasSelection();
	// }

	private void addNameToTextField(String name) {

		name = removeBirthDate(name);

		String text = textArea.getText();
		if (at.brandl.lws.notice.shared.Utils.isNotEmpty(cleanUp(text))) {
			text += ", ";
		}
		text += name;
		textArea.setText(text);
		beobachtung.setText(cleanUp(textArea.getHTML()));
	}

	private String removeBirthDate(String name) {
		int pos = name.indexOf('(');
		if (pos != -1) {
			name = name.substring(0, pos - 1);
		}
		return name;
	}

	public void setKey(String key) {
		loadData(key);
		updateState();
	}
}
