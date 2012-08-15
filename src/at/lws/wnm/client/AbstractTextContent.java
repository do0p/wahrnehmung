package at.lws.wnm.client;

import java.util.Date;

import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public abstract class AbstractTextContent extends VerticalPanel {

	// popups
	private final PopUp dialogBox = new PopUp();

	// main window
	private final TextArea textArea = new TextArea();
	private final DateBox dateBox = new DateBox();

	private final ListBox durationSelection = new ListBox();
	private final ListBox socialSelection = new ListBox();
	private NameSelection nameSelection;

	private SectionSelection sectionSelection;

	public AbstractTextContent(String width) {
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

	protected void resetForm() {
		nameSelection.reset();
		sectionSelection.reset();
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setValue("");
	}

	protected abstract CellPanel createButtonContainer();

	public TextArea getTextArea() {
		return textArea;
	}

	public NameSelection getNameSelection() {
		return nameSelection;
	}

	public DateBox getDateBox() {
		return dateBox;
	}

	public PopUp getDialogBox() {
		return dialogBox;
	}

	public SocialEnum getSocialForm() {
		final int selectedIndex = socialSelection.getSelectedIndex();
		if (selectedIndex != -1) {
			final String socialText = socialSelection.getValue(selectedIndex);
			if (!socialText.isEmpty()) {
				return SocialEnum.valueOf(socialText);
			}
		}
		return null;
	}

	public DurationEnum getDuration() {
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

	public Long getSelectedSectionKey() {

		return sectionSelection.getSelectedSectionKey();
	}

}