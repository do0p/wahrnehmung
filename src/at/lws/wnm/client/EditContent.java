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
	private Long key;
	private DecisionBox decisionBox;
	private ListBox additionalNames;
	private Button nameAddButton;
	private Button nameRemoveButton;

	public EditContent(Authorization authorization, Long key) {
		this.key = key;
		init();
		layout();
		if (key != null)
			loadData(key);
	}

	private void loadData(Long key) {
		this.wahrnehmungService.getBeobachtung(key,
				new AsyncCallback<GwtBeobachtung>() {
					public void onFailure(Throwable caught) {
						EditContent.this.dialogBox.setErrorMessage();
						EditContent.this.dialogBox
								.setDisableWhileShown(new FocusWidget[] { EditContent.this.sendButton });
						EditContent.this.dialogBox.center();
					}

					public void onSuccess(GwtBeobachtung result) {
						EditContent.this.nameSelection.setSelected(result
								.getChildKey());
						EditContent.this.dateBox.setValue(result.getDate());
						EditContent.this.sectionSelection.setSelected(result
								.getSectionKey());
						GwtBeobachtung.DurationEnum duration = result
								.getDuration();
						if (duration != null) {
							EditContent.this.durationSelection
									.setSelectedIndex(duration.ordinal() + 1);
						}

						GwtBeobachtung.SocialEnum social = result.getSocial();
						if (social != null) {
							EditContent.this.socialSelection
									.setSelectedIndex(social.ordinal() + 1);
						}
						EditContent.this.textArea.setHTML(result.getText());
						EditContent.this.changes = false;
						EditContent.this.sendButton.setEnabled(false);
					}
				});
	}

	private void init() {
		ChangeHandler changeHandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				EditContent.this.markChanged();
			}
		};
		this.nameSelection = new NameSelection(this.dialogBox);
		this.nameSelection.getValueBox().addChangeHandler(changeHandler);

		this.additionalNames = new ListBox(true);

		this.nameAddButton = new Button(Utils.DOWN_ARROW);
		this.nameAddButton.setEnabled(this.key == null);
		this.nameAddButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				Long selectedChildKey = EditContent.this.nameSelection
						.getSelectedChildKey();
				if ((selectedChildKey != null)
						&& (!isInList(EditContent.this.additionalNames,
								selectedChildKey.toString()))) {
					EditContent.this.additionalNames.addItem(
							EditContent.this.nameSelection.getValue(),
							selectedChildKey.toString());
					EditContent.this.nameSelection.reset();
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
		this.nameRemoveButton = new Button(Utils.UP_ARROW);
		this.nameRemoveButton.setEnabled(this.key == null);
		this.nameRemoveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				if (EditContent.this.additionalNames.getSelectedIndex() > 0)
					;
				int itemCount = EditContent.this.additionalNames.getItemCount();
				for (int i = EditContent.this.additionalNames
						.getSelectedIndex(); i < itemCount; i++)
					if (EditContent.this.additionalNames.isItemSelected(i)) {
						EditContent.this.additionalNames.removeItem(i);
						i--;
						itemCount--;
					}
			}
		});
		this.dateBox.setValue(new Date());
		this.dateBox.setFormat(Utils.DATEBOX_FORMAT);
		this.dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				EditContent.this.markChanged();
			}
		});
		this.sectionSelection = new SectionSelection(this.dialogBox,
				changeHandler);

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
				EditContent.this.markChanged();
			}
		});
		this.decisionBox = new DecisionBox();
		this.decisionBox.setText(labels.notSavedWarning());
		this.decisionBox.addOkClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				EditContent.this.resetForm();
			}
		});
	}

	private void layout() {

		final Panel nameContainer = createNameSelectionContainer();
		this.add(nameContainer);

		final Panel contentContainer = createContentContainer();
		this.add(contentContainer);

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
		final Grid selectionContainer = new Grid(1, sectionSelectionBoxes.size());
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
		additionalNames.setSize(Utils.HUNDERT_PERCENT, nameSelectionHeight
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
		this.sendButton.addClickHandler(new SendbuttonHandler());

		this.newButton = new Button(labels.cancel());
		newButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		this.newButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (EditContent.this.changes)
					EditContent.this.decisionBox.center();
				else
					EditContent.this.resetForm();
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

	private class SendbuttonHandler implements ClickHandler {
		private SendbuttonHandler() {
		}

		public void onClick(ClickEvent event) {
			EditContent.this.sendButton.setEnabled(false);
			sendNameToServer();
		}

		private void sendNameToServer() {
			String text = EditContent.this.textArea.getHTML();
			String name = EditContent.this.nameSelection.getValue();
			Long childKey = EditContent.this.nameSelection
					.getSelectedChildKey();
			if (((Utils.isEmpty(name)) || (childKey == null))
					&& (EditContent.this.additionalNames.getItemCount() > 0)) {
				name = EditContent.this.additionalNames.getItemText(0);
				childKey = Long.valueOf(EditContent.this.additionalNames
						.getValue(0));
				EditContent.this.additionalNames.removeItem(0);
			}

			Long sectionKey = EditContent.this.sectionSelection
					.getSelectedSectionKey();
			Date date = EditContent.this.dateBox.getValue();

			String errorMessage = new String();
			if (Utils.isEmpty(name))
				errorMessage = errorMessage + labels.noChild()
						+ Utils.LINE_BREAK;
			else if (childKey == null) {
				errorMessage = errorMessage + labels.noChildWithName(name)
						+ Utils.LINE_BREAK;
			}
			if (sectionKey == null) {
				errorMessage = errorMessage + labels.noSection()
						+ Utils.LINE_BREAK;
			}
			if (date == null) {
				errorMessage = errorMessage + labels.noDate()
						+ Utils.LINE_BREAK;
				;
			}
			if (text.isEmpty()) {
				errorMessage = errorMessage + labels.noObservation()
						+ Utils.LINE_BREAK;
			}
			if (!errorMessage.isEmpty()) {
				EditContent.this.dialogBox.setErrorMessage(errorMessage);
				EditContent.this.dialogBox
						.setDisableWhileShown(new FocusWidget[] { EditContent.this.sendButton });
				EditContent.this.dialogBox.center();
				return;
			}

			GwtBeobachtung beobachtung = new GwtBeobachtung();
			beobachtung.setKey(EditContent.this.key);
			beobachtung.setText(text);
			beobachtung.setChildKey(childKey);
			beobachtung.setSectionKey(sectionKey);
			beobachtung.setDate(date);
			beobachtung.setDuration(EditContent.this.getDuration());
			beobachtung.setSocial(EditContent.this.getSocialForm());

			for (int i = 0; i < EditContent.this.additionalNames.getItemCount(); i++) {
				beobachtung.getAdditionalChildKeys().add(
						Long.valueOf(EditContent.this.additionalNames
								.getValue(i)));
			}

			EditContent.this.wahrnehmungService.storeBeobachtung(beobachtung,
					new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
							EditContent.this.dialogBox.setErrorMessage();
							EditContent.this.dialogBox
									.setDisableWhileShown(new FocusWidget[] { EditContent.this.sendButton });
							EditContent.this.dialogBox.center();
							EditContent.this.sendButton.setEnabled(true);
						}

						public void onSuccess(Void result) {
							EditContent.this.changes = false;
							EditContent.this.resetForm();
						}
					});
		}
	}
}
