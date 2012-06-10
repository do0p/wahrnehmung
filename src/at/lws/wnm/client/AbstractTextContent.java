package at.lws.wnm.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.shared.model.GwtChild;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
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
	private final SuggestBox nameSelection;

	private final Map<String, Long> childMap = new HashMap<String, Long>();
	private HorizontalPanel buttonContainer;

	public AbstractTextContent(String width) {
		// init fields
		dialogBox = new PopUp();
		textArea = new TextArea();
		dateBox = new DateBox();
		getDateBox().setValue(new Date());
		sectionSelection = new ListBox();
		createSectionSelectsions();
		nameSelection = new SuggestBox(createChildNameList());

		setSize(width, "550px");
		final HorizontalPanel selectionContainer = new HorizontalPanel();
		selectionContainer.add(getNameSelection());
		getNameSelection().setSize("200px", "20px");
		selectionContainer.setCellVerticalAlignment(getNameSelection(),
				HasVerticalAlignment.ALIGN_MIDDLE);
		selectionContainer.add(getSectionSelection());
		getSectionSelection().setSize("150px", "20px");
		selectionContainer.setCellVerticalAlignment(getSectionSelection(),
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
		getSectionSelection().setSelectedIndex(0);
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
				getSectionSelection().addItem("- Bereich -", "");
				for (GwtSection section : result) {
					final String keyString = section.getKey().toString();
					getSectionSelection().addItem(section.getSectionName(),
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

		final String value = sectionSelection.getValue(sectionSelection
				.getSelectedIndex());
		if (value.isEmpty()) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}