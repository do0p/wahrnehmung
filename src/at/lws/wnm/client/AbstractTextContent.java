package at.lws.wnm.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.client.service.SectionService;
import at.lws.wnm.client.service.SectionServiceAsync;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public abstract class AbstractTextContent extends VerticalPanel {

	private static final String LISTBOX_WIDTH = "150px";

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	// popups
	private final PopUp dialogBox = new PopUp();

	// main window
	private final TextArea textArea = new TextArea();
	private final DateBox dateBox = new DateBox();
	private final ListBox sectionSelection = new ListBox();
	private final ListBox subSectionSelection = new ListBox();
	private final ListBox durationSelection = new ListBox();
	private final ListBox socialSelection = new ListBox();
	private NameSelection nameSelection;

	private final Map<Long, List<String[]>> subSectionSelections = new HashMap<Long, List<String[]>>();


	public AbstractTextContent(String width) {
		init();
		layout(width);
	}

	private void init() {
		nameSelection = new NameSelection(dialogBox);
		dateBox.setValue(new Date());

		sectionSelection.addChangeHandler(new SectionChangeHandler());
		subSectionSelection.setEnabled(false);
		subSectionSelection.addItem("- Subbereich -", "");
		createSectionSelections();

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
		Utils.formatLeftCenter(this, childAndDateContainer, width, Utils.ROW_HEIGHT);
		Utils.formatLeftCenter(childAndDateContainer, nameSelection, NameSelection.WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(childAndDateContainer, dateBox, "50px", Utils.FIELD_HEIGHT);
		
		final HorizontalPanel selectionContainer = new HorizontalPanel();
		Utils.formatLeftCenter(this, selectionContainer, width, Utils.ROW_HEIGHT);
		Utils.formatLeftCenter(selectionContainer, sectionSelection, LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(selectionContainer, subSectionSelection, LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(selectionContainer, durationSelection, LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		Utils.formatLeftCenter(selectionContainer, socialSelection, LISTBOX_WIDTH, Utils.FIELD_HEIGHT);

		textArea.setSize(width, "400px");
		add(textArea);
		
		Utils.formatLeftCenter(this, createButtonContainer(), width, Utils.ROW_HEIGHT);
		
		setSize(width, "550px");

	}

	protected void resetForm() {
		nameSelection.setText("");
		sectionSelection.setSelectedIndex(0);
		durationSelection.setSelectedIndex(0);
		socialSelection.setSelectedIndex(0);
		textArea.setValue("");
	}

	protected abstract CellPanel createButtonContainer();

	private void createSectionSelections() {
		sectionService.querySections(new AsyncCallback<List<GwtSection>>() {

			@Override
			public void onFailure(Throwable caught) {
				getDialogBox().setErrorMessage();
				getDialogBox().center();
			}

			@Override
			public void onSuccess(List<GwtSection> result) {
				sectionSelection.addItem("- Bereich -", "");
				final Map<Long, List<GwtSection>> children = new HashMap<Long, List<GwtSection>>();
				for (GwtSection section : result) {
					if (section.getParentKey() == null) {
						sectionSelection.addItem(section.getSectionName(),
								section.getKey().toString());
						subSectionSelections.put(section.getKey(),
								new ArrayList<String[]>());
					} else {
						List<GwtSection> sections = children.get(section
								.getParentKey());
						if (sections == null) {
							sections = new ArrayList<GwtSection>();
							children.put(section.getParentKey(), sections);
						}
						sections.add(section);
					}
				}

				for (Long parentKey : subSectionSelections.keySet()) {
					final List<String[]> subSelectionItems = subSectionSelections
							.get(parentKey);
					addChildren(children, parentKey, "", subSelectionItems);
				}
			}

			private void addChildren(Map<Long, List<GwtSection>> children,
					Long parentKey, String prefix,
					List<String[]> subSelectionItems) {
				final List<GwtSection> sections = children.get(parentKey);
				if (sections != null) {
					for (GwtSection section : sections) {
						subSelectionItems.add(new String[] {
								prefix + section.getSectionName(),
								section.getKey().toString() });
						addChildren(children, section.getKey(),
								createPrefix(prefix), subSelectionItems);
					}
				}
			}

			private String createPrefix(String prefix) {
				if (prefix.isEmpty()) {
					return "-> ";
				}
				return "  " + prefix;
			}
		});

	}

	
	public TextArea getTextArea() {
		return textArea;
	}

	public NameSelection getNameSelection() {
		return nameSelection;
	}

	public ListBox getSectionSelection() {
		return sectionSelection;
	}

	public DateBox getDateBox() {
		return dateBox;
	}

	public PopUp getDialogBox() {
		return dialogBox;
	}



	public Long getSelectedSectionKey() {

		final int sectionIndex = sectionSelection.getSelectedIndex();
		if (sectionIndex == -1) {
			return null;
		}

		final String value = sectionSelection.getValue(sectionIndex);
		if (value.isEmpty()) {
			return null;
		}

		if (subSectionSelection.isEnabled()) {
			final int subSectionIndex = subSectionSelection.getSelectedIndex();
			if (subSectionIndex != -1) {
				final String subSectionValue = subSectionSelection
						.getValue(subSectionIndex);
				if (!subSectionValue.isEmpty()) {
					return Long.valueOf(subSectionValue);
				}
			}
		}
		return Long.valueOf(value);
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

	private class SectionChangeHandler implements ChangeHandler {

		@Override
		public void onChange(ChangeEvent event) {
			if (sectionSelection.getSelectedIndex() != -1) {
				subSectionSelection.clear();
				subSectionSelection.addItem("- Subbereich -", "");
				subSectionSelection.setEnabled(false);
				final String value = sectionSelection.getValue(sectionSelection
						.getSelectedIndex());
				if (!value.isEmpty()) {
					final List<String[]> subSelectionItems = subSectionSelections
							.get(Long.valueOf(value));
					if (subSelectionItems != null
							&& !subSelectionItems.isEmpty()) {
						for (String[] entry : subSelectionItems) {
							subSectionSelection.addItem(entry[0], entry[1]);
						}
						subSectionSelection.setEnabled(true);
					}
				}
			}
		}

	}
}