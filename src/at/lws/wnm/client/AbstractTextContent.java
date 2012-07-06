package at.lws.wnm.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.shared.model.GwtChild;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public abstract class AbstractTextContent extends VerticalPanel {

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	// popups
	private final PopUp dialogBox;

	// main window
	private final TextArea textArea;
	private final DateBox dateBox;
	private final ListBox sectionSelection;
	private final Map<Long, List<String[]>> subSectionSelections;
	private final SuggestBox nameSelection;

	private final Map<String, Long> childMap = new HashMap<String, Long>();
	private HorizontalPanel buttonContainer;
	private ListBox subSectionSelection;

	public AbstractTextContent(String width) {
		// init fields
		dialogBox = new PopUp();
		textArea = new TextArea();
		dateBox = new DateBox();
		getDateBox().setValue(new Date());
		sectionSelection = new ListBox();
		sectionSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (sectionSelection.getSelectedIndex() != -1) {
					subSectionSelection.clear();
					subSectionSelection.addItem("- Subbereich -", "");
					subSectionSelection.setEnabled(false);
					final String value = sectionSelection
							.getValue(sectionSelection.getSelectedIndex());
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
		});
		subSectionSelection = new ListBox();
		subSectionSelection.setEnabled(false);
		subSectionSelection.addItem("- Subbereich -", "");

		subSectionSelections = new HashMap<Long, List<String[]>>();
		createSectionSelectsions();
		nameSelection = new SuggestBox(createChildNameList());

		setSize(width, "550px");
		final HorizontalPanel selectionContainer = new HorizontalPanel();

		selectionContainer.add(getNameSelection());
		getNameSelection().setSize("200px", "20px");
		selectionContainer.setCellVerticalAlignment(getNameSelection(),
				HasVerticalAlignment.ALIGN_MIDDLE);

		selectionContainer.add(sectionSelection);
		sectionSelection.setSize("150px", "20px");
		selectionContainer.setCellVerticalAlignment(sectionSelection,
				HasVerticalAlignment.ALIGN_MIDDLE);

		selectionContainer.add(subSectionSelection);
		subSectionSelection.setSize("150px", "20px");
		selectionContainer.setCellVerticalAlignment(subSectionSelection,
				HasVerticalAlignment.ALIGN_MIDDLE);

		selectionContainer.add(getDateBox());
		getDateBox().setSize("100px", "20px");
		getDateBox().setFormat(Utils.DATEBOX_FORMAT);
		selectionContainer.setCellHorizontalAlignment(getDateBox(),
				HasHorizontalAlignment.ALIGN_RIGHT);
		selectionContainer.setCellVerticalAlignment(getDateBox(),
				HasVerticalAlignment.ALIGN_MIDDLE);

		add(selectionContainer);
		setCellHorizontalAlignment(selectionContainer,
				HasHorizontalAlignment.ALIGN_CENTER);
		setCellVerticalAlignment(selectionContainer,
				HasVerticalAlignment.ALIGN_MIDDLE);
		selectionContainer.setSize(width, "40px");
		add(getTextArea());
		getTextArea().setSize(width, "440px");
		buttonContainer = createButtonContainer();
		add(buttonContainer);
	}

	protected void resetForm() {
		getNameSelection().setText("");
		sectionSelection.setSelectedIndex(0);
		getTextArea().setValue("");
	}

	protected abstract HorizontalPanel createButtonContainer();

	private void createSectionSelectsions() {
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

}