package at.lws.wnm.client;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import at.lws.wnm.client.utils.BeobachtungsTable;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Print;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.SectionSelectionBox;
import at.lws.wnm.client.utils.Show;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.client.utils.YearSelection;
import at.lws.wnm.client.utils.YearSelection.YearSelectionResult;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class Search extends VerticalPanel {
	private static final int START_YEAR = 2012;

	private final Labels labels = (Labels) GWT.create(Labels.class);

	private final BeobachtungsTable table;
	private final BeobachtungsFilter filter;
	private final NameSelection nameSelection;
	private final SectionSelection sectionSelection;
	private final YearSelection yearSelection;
	private final MultiSelectionModel<GwtBeobachtung> selectionModel;
	private final Show beobachtungen;

	public Search(Authorization authorization) {
		PopUp dialogBox = new PopUp();
		final RichTextArea textArea = new RichTextArea();
		this.filter = new BeobachtungsFilter();
		this.nameSelection = new NameSelection(dialogBox);
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
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
//		Button sendButton = new Button(labels.filter());
//		sendButton.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				search();
//			}
//		});
//		sendButton.addStyleName(Utils.SEND_BUTTON_STYLE);
		this.selectionModel = createSelectionModel(textArea);
		this.table = new BeobachtungsTable(authorization, this.selectionModel,
				this.filter, dialogBox);
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
		final List<SectionSelectionBox> sectionSelectionBoxes = this.sectionSelection
				.getSectionSelectionBoxes();
		final Grid filterBox = new Grid(1, sectionSelectionBoxes.size() + 3);
		add(filterBox);

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

//		sendButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
//				+ Utils.PIXEL);
//		filterBox.setWidget(0, i, sendButton);

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
						Search.this.selectionModel.getSelectedSet());
				if (!selectedSet.isEmpty()) {
					Print.it(at.lws.wnm.shared.model.Utils
							.createPrintHtml(selectedSet));
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
						Search.this.selectionModel.getSelectedSet());
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
		Search.this.filter.setChildKey(Search.this.nameSelection
				.getSelectedChildKey());
		Search.this.filter.setSectionKey(Search.this.sectionSelection
				.getSelectedSectionKey());
		YearSelectionResult selectionResult = yearSelection
				.getSelectedTimeRange();
		if(selectionResult.isSinceLastDevelopementDialogue()){
			filter.setSinceLastDevelopmementDialogue(true);
			filter.setTimeRange(null);
		} else {
			filter.setSinceLastDevelopmementDialogue(false);
			filter.setTimeRange(selectionResult.getTimeRange());
		}
		Search.this.table.updateTable();
	}
}