package at.brandl.lws.notice.client;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

import at.brandl.lws.notice.client.utils.BeobachtungsTable;
import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.Navigation;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.Print;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Show;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.client.utils.YearSelection;
import at.brandl.lws.notice.client.utils.YearSelection.YearSelectionResult;
import at.brandl.lws.notice.model.GwtAuthorization;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;

public class Search extends VerticalPanel {
	private static final int START_YEAR = 2012;

	private final Labels labels = (Labels) GWT.create(Labels.class);

	private final BeobachtungsTable table;
	private final BeobachtungsFilter filter;
	private final NameSelection nameSelection;
	private final SectionSelection sectionSelection;
	private final YearSelection yearSelection;
	private final CheckBox under12;
	private final CheckBox over12;
	private final CheckBox archived;
	private final CheckBox summaries;
	private final MultiSelectionModel<GwtBeobachtung> selectionModel;
	private final Show beobachtungen;

	public Search(GwtAuthorization authorization, Navigation navigation) {
		PopUp dialogBox = new PopUp();
		final RichTextArea textArea = new RichTextArea();
		this.filter = new BeobachtungsFilter();
		this.filter.setAggregateSectionEntries(true);
		this.nameSelection = new NameSelection(dialogBox);
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				search();
			}
		});
		under12 = new CheckBox(labels.under12());
		under12.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateGui();
				search();
			}
		});
		over12 = new CheckBox(labels.over12());
		over12.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateGui();
				search();
			}
		});
		archived = new CheckBox(labels.archived());
		archived.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nameSelection.setIncludeArchived(archived.getValue());
				sectionSelection.setIncludeArchived(archived.getValue());
				updateGui();
				search();
			}
		});	
		summaries = new CheckBox(labels.summaries());
		summaries.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateGui();
				search();
			}
		});		
		this.sectionSelection = new SectionSelection(dialogBox);
		sectionSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				search();
			}
		});
		this.yearSelection = new YearSelection(START_YEAR);
		yearSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				search();
			}
		});

		this.selectionModel = createSelectionModel(textArea);
		this.table = new BeobachtungsTable(this.selectionModel,
				this.filter, dialogBox, navigation.getEditContent());
		this.table
				.addCellPreviewHandler(new CellPreviewEvent.Handler<GwtBeobachtung>() {
					public void onCellPreview(
							CellPreviewEvent<GwtBeobachtung> event) {
						textArea.setHTML(((GwtBeobachtung) event.getValue())
								.getText());
					}
				});
		beobachtungen = new Show();
		layout(textArea);
	}

	private void layout(RichTextArea textArea) {
		VerticalPanel controls = new VerticalPanel();
		add(controls);
		final List<SectionSelectionBox> sectionSelectionBoxes = this.sectionSelection
				.getSectionSelectionBoxes();
		final Grid filterBox = new Grid(1, sectionSelectionBoxes.size() + 3);
		controls.add(filterBox);
		nameSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		int i = 0;
		filterBox.setWidget(0, i++, nameSelection);

		for (ListBox selectionBox : sectionSelectionBoxes) {
			selectionBox.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL,
					Utils.ROW_HEIGHT + Utils.PIXEL);
			filterBox.setWidget(0, i++, selectionBox);
		}

		yearSelection.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT + Utils.PIXEL);
		filterBox.setWidget(0, i++, yearSelection);

		Panel boxes = new HorizontalPanel();
		boxes.add(under12);
		boxes.add(over12);
		boxes.add(archived);
		boxes.add(summaries);
		controls.add(boxes);

		add(this.table);

		SimplePager pager = new SimplePager();
		pager.setDisplay(this.table);
		add(pager);
		textArea.setSize(Utils.APP_WIDTH - 10 + Utils.PIXEL, Utils.APP_HEIGHT
				- 300 + Utils.PIXEL);
		add(textArea);
		Utils.formatCenter(this, createButtonContainer());
	}

	private MultiSelectionModel<GwtBeobachtung> createSelectionModel(
			final RichTextArea textArea) {
		final MultiSelectionModel<GwtBeobachtung> selectionModel = new MultiSelectionModel<GwtBeobachtung>();
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					public void onSelectionChange(SelectionChangeEvent event) {
						Set<GwtBeobachtung> selectedObjects = selectionModel
								.getSelectedSet();
						if (!selectedObjects.isEmpty())
							textArea.setHTML(((GwtBeobachtung) selectedObjects
									.iterator().next()).getText());
					}
				});
		return selectionModel;
	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 2);

		final Button printButton = new Button(labels.print());
		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Set<GwtBeobachtung> selectedSet = new TreeSet<GwtBeobachtung>(
						selectionModel.getSelectedSet());
				if (!selectedSet.isEmpty()) {
					Print.it(Utils.createPrintHtml(selectedSet));
				}
			}
		});

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		buttonContainer.setWidget(0, 0, printButton);

		final Button showButton = new Button(labels.show());
		showButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Set<GwtBeobachtung> selectedSet = new TreeSet<GwtBeobachtung>(
						selectionModel.getSelectedSet());
				if (!selectedSet.isEmpty()) {
					beobachtungen.setBeobachtungen(selectedSet);
					beobachtungen.center();
				}
			}
		});

		showButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		buttonContainer.setWidget(0, 1, showButton);

		return buttonContainer;
	}

	private void search() {
		filter.setOver12(over12.getValue());
		filter.setUnder12(under12.getValue());
		filter.setArchived(archived.getValue());
		filter.setShowSummaries(summaries.getValue());
		if (enableNameSelection()) {
			filter.setChildKey(nameSelection.getSelectedChildKey());
		} else {
			filter.setChildKey(null);
		}

		filter.setSectionKey(sectionSelection.getSelectedSectionKey());
		YearSelectionResult selectionResult = yearSelection
				.getSelectedTimeRange();
		if (selectionResult.isSinceLastDevelopementDialogue()) {
			filter.setSinceLastDevelopmementDialogue(true);
			filter.setTimeRange((Date)null);
		} else {
			filter.setSinceLastDevelopmementDialogue(false);
			filter.setTimeRange(selectionResult.getTimeRange());
		}
		table.clear();
		if (readyToSearch()) {
			table.updateTable();
		}
	}

	private boolean readyToSearch() {
		return under12.getValue() || over12.getValue()
				|| filter.getChildKey() != null || sectionSelection.getSelectedSectionKeyLevel() > 0;
	}

	private void updateGui() {
		nameSelection.setEnabled(enableNameSelection());
	}

	private boolean enableNameSelection() {
		return !(under12.getValue() || over12.getValue());
	}
}
