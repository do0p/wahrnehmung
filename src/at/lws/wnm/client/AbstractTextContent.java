package at.lws.wnm.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.shared.model.GwtBeobachtung.DurationEnum;
import at.lws.wnm.shared.model.GwtBeobachtung.SocialEnum;
import at.lws.wnm.shared.model.GwtChild;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public abstract class AbstractTextContent extends VerticalPanel {

	protected static final String FIELD_HEIGHT = "20px";
	protected static final String ROW_HEIGHT = "40px";
	protected static final String BUTTON_WIDTH = "80px";
	private static final String LISTBOX_WIDTH = "150px";
	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
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
	private SuggestBox nameSelection;

	private final Map<Long, List<String[]>> subSectionSelections = new HashMap<Long, List<String[]>>();
	private final Map<String, Long> childMap = new HashMap<String, Long>();

	public AbstractTextContent(String width) {
		init();
		layout(width);
	}

	private void init() {
		nameSelection = new SuggestBox(createChildNameList());
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
		childAndDateContainer.add(nameSelection);
		childAndDateContainer.add(dateBox);
		formatLeftCenter(this, childAndDateContainer, width, ROW_HEIGHT);
		formatLeftCenter(childAndDateContainer, nameSelection, "300px", FIELD_HEIGHT);
		formatLeftCenter(childAndDateContainer, dateBox, "50px", FIELD_HEIGHT);
		
		final HorizontalPanel selectionContainer = new HorizontalPanel();
		selectionContainer.add(sectionSelection);
		selectionContainer.add(subSectionSelection);
		selectionContainer.add(durationSelection);
		selectionContainer.add(socialSelection);
		formatLeftCenter(this, selectionContainer, width, ROW_HEIGHT);
		formatLeftCenter(selectionContainer, sectionSelection, LISTBOX_WIDTH, FIELD_HEIGHT);
		formatLeftCenter(selectionContainer, subSectionSelection, LISTBOX_WIDTH, FIELD_HEIGHT);
		formatLeftCenter(selectionContainer, durationSelection, LISTBOX_WIDTH, FIELD_HEIGHT);
		formatLeftCenter(selectionContainer, socialSelection, LISTBOX_WIDTH, FIELD_HEIGHT);

		textArea.setSize(width, "400px");
		
		final CellPanel buttonContainer = createButtonContainer();
		formatLeftCenter(this, buttonContainer, width, ROW_HEIGHT);
		
		setSize(width, "550px");
		add(childAndDateContainer);
		add(selectionContainer);
		add(textArea);
		add(buttonContainer);
	}

	protected void formatLeftCenter(CellPanel panel, Widget widget, String width, String height) {
		panel.setCellVerticalAlignment(widget,
				HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setCellHorizontalAlignment(widget,
				HasHorizontalAlignment.ALIGN_LEFT);
		widget.setSize(width, height);
		panel.setCellWidth(widget, width + "px");
	}

	protected void resetForm() {
		nameSelection.setText("");
		sectionSelection.setSelectedIndex(0);
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

	private MultiWordSuggestOracle createChildNameList() {
		final MultiWordSuggestOracle names = new MultiWordSuggestOracle();
		childService.queryChildren(new AsyncCallback<List<GwtChild>>() {

			@Override
			public void onFailure(Throwable caught) {
				getDialogBox().setErrorMessage();
				getDialogBox().center();
			}

			@Override
			public void onSuccess(List<GwtChild> result) {
				for (GwtChild child : result) {

					final String formattedChildName = Utils
							.formatChildName(child);
					names.add(formattedChildName);
					getChildMap().put(formattedChildName, child.getKey());
				}
			}

		});
		return names;
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public Map<String, Long> getChildMap() {
		return childMap;
	}

	public SuggestBox getNameSelection() {
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

	public Long getSelectedChildKey() {
		return childMap.get(nameSelection.getValue());
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