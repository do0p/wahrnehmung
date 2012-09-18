package at.lws.wnm.client;

import java.util.Set;

import at.lws.wnm.client.utils.BeobachtungsTable;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Print;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class Search extends VerticalPanel {

	private final BeobachtungsTable table;
	private final BeobachtungsFilter filter;
	private final NameSelection nameSelection;
	private final SectionSelection sectionSelection;
	private final MultiSelectionModel<GwtBeobachtung> selectionModel;

	public Search(final Authorization authorization, int width) {
		
		final PopUp dialogBox = new PopUp();
		final TextArea textArea = new TextArea();
		filter =  new BeobachtungsFilter();
		nameSelection = new NameSelection(dialogBox);
		sectionSelection = new SectionSelection(dialogBox, null);
		final Button sendButton = new Button(Utils.FILTER);
		sendButton.addClickHandler(new FilterButtonHandler());
		sendButton.addStyleName("sendButton");
		selectionModel = createSelectionModel(textArea);
		table = new BeobachtungsTable(authorization, selectionModel, filter,
				dialogBox);
		table.addCellPreviewHandler(new Handler<GwtBeobachtung>() {
			@Override
			public void onCellPreview(CellPreviewEvent<GwtBeobachtung> event) {
				textArea.setText(event.getValue().getText());
			}
		});

		layout(width, textArea, sendButton);
	}

	private void layout(int width, final TextArea textArea,
			final Button sendButton) {
		final CellPanel filterBox = new HorizontalPanel();
		filterBox.setSpacing(Utils.BUTTON_SPACING);
		add(filterBox);
		Utils.formatLeftCenter(filterBox, nameSelection, NameSelection.WIDTH,
				Utils.FIELD_HEIGHT);

		for (ListBox selectionBox : sectionSelection.getSectionSelectionBoxes()) {
			Utils.formatLeftCenter(filterBox, selectionBox,
					Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		}
		Utils.formatLeftCenter(filterBox, sendButton, Utils.BUTTON_WIDTH,
				Utils.ROW_HEIGHT);
		add(table);
		final SimplePager pager = new SimplePager();
		pager.setDisplay(table);
		add(pager);
		textArea.setSize("" + width + "px", "400px");
		add(textArea);
		Utils.formatLeftCenter(this, createButtonContainer(), width,
				Utils.ROW_HEIGHT);
	}

	private MultiSelectionModel<GwtBeobachtung> createSelectionModel(final TextArea textArea) {
		final MultiSelectionModel<GwtBeobachtung> selectionModel = new MultiSelectionModel<GwtBeobachtung>();
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						final Set<GwtBeobachtung> selectedObjects = selectionModel.getSelectedSet();
						if (!selectedObjects.isEmpty()) {
							textArea.setText(selectedObjects.iterator().next()
									.getText());
						}
					}
				});
		return selectionModel;
	}

	private HorizontalPanel createButtonContainer() {
		final HorizontalPanel buttonContainer = new HorizontalPanel();
		buttonContainer.setWidth("170px");

		Button printButton = new Button(Utils.PRINT);
		printButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final Set<GwtBeobachtung> selectedSet = selectionModel
						.getSelectedSet();
				if (selectedSet.isEmpty()) {
					// message
				} else {
					Print.it(Utils.createPrintHtml(selectedSet));
				}
			}

		});
		printButton.addStyleName("sendButton");

		Utils.formatLeftCenter(buttonContainer, printButton,
				Utils.BUTTON_WIDTH, Utils.ROW_HEIGHT);

		return buttonContainer;
	}

	public class FilterButtonHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			filter.setChildKey(nameSelection.getSelectedChildKey());
			filter.setSectionKey(sectionSelection.getSelectedSectionKey());
			table.updateTable();
		}

	}

}
