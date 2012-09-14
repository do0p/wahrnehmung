package at.lws.wnm.client;

import java.util.Set;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;

public class Search extends VerticalPanel {

	private static final String BEOBACHTUNG_DEL_WARNING = "Achtung, diese Beobachtung wird gel&ouml;scht. Der Vorgang nicht mehr r&uuml;ckg&auml;nig gemacht werden!";

	private final CellTable<GwtBeobachtung> table;

	private final PopUp dialogBox = new PopUp();
	private final WahrnehmungsServiceAsync wahrnehmungsService = GWT
			.create(WahrnehmungsService.class);
	private final TextArea textArea = new TextArea();
	private final BeobachtungsFilter filter = new BeobachtungsFilter();

	private NameSelection nameSelection;
	private SectionSelection sectionSelection;

	private Button sendButton;

	private AsyncDataProvider<GwtBeobachtung> asyncDataProvider;

	private DecisionBox decisionBox;

	private MultiSelectionModel<GwtBeobachtung> selectionModel;

	public Search(final Authorization authorization, String width) {

		decisionBox = new DecisionBox();
		decisionBox.setText(BEOBACHTUNG_DEL_WARNING);

		final CellPanel filterBox = new HorizontalPanel();
		filterBox.setSpacing(Utils.BUTTON_SPACING);
		add(filterBox);

		nameSelection = new NameSelection(dialogBox);
		Utils.formatLeftCenter(filterBox, nameSelection, NameSelection.WIDTH,
				Utils.FIELD_HEIGHT);

		sectionSelection = new SectionSelection(dialogBox, null);
		for (ListBox selectionBox : sectionSelection.getSectionSelectionBoxes()) {
			Utils.formatLeftCenter(filterBox, selectionBox,
					Utils.LISTBOX_WIDTH, Utils.FIELD_HEIGHT);
		}

		sendButton = new Button(Utils.FILTER);
		sendButton.addClickHandler(new FilterButtonHandler());
		sendButton.addStyleName("sendButton");

		Utils.formatLeftCenter(filterBox, sendButton, Utils.BUTTON_WIDTH,
				Utils.ROW_HEIGHT);
		
		selectionModel = new MultiSelectionModel<GwtBeobachtung>();
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						final Set<GwtBeobachtung> selectedObjects = selectionModel
								.getSelectedSet();
						if (!selectedObjects.isEmpty()) {
							textArea.setText(selectedObjects.iterator().next()
									.getText());
						}
					}
				});


		final Column<GwtBeobachtung, Boolean> markColumn = new Column<GwtBeobachtung, Boolean>(
				new CheckboxCell(true, false)) {

			@Override
			public Boolean getValue(GwtBeobachtung object) {
				return Boolean.valueOf(selectionModel.isSelected(object));
			}
		};

		final Column<GwtBeobachtung, String> nameColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return object.getChildName();
			}
		};

		final Column<GwtBeobachtung, String> sectionColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return object.getSectionName();
			}
		};

		final Column<GwtBeobachtung, String> dateColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return Utils.DATE_FORMAT.format(object.getDate());
			}
		};

		final Column<GwtBeobachtung, String> socialColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				if (object.getSocial() != null) {
					return object.getSocial().getText();
				}
				return null;
			}
		};

		final Column<GwtBeobachtung, String> durationColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				if (object.getDuration() != null) {
					return object.getDuration().getText();
				}
				return null;
			}
		};

		final Column<GwtBeobachtung, String> textColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return Utils.shorten(object.getText(), 20);
			}
		};

		final Column<GwtBeobachtung, String> userColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return object.getUser();
			}
		};

		final Column<GwtBeobachtung, GwtBeobachtung> editColumn = new IdentityColumn<GwtBeobachtung>(
				new ActionCell<GwtBeobachtung>(Utils.EDIT,
						new Delegate<GwtBeobachtung>() {
							@Override
							public void execute(GwtBeobachtung object) {
								final RootPanel rootPanel = RootPanel
										.get("content");
								rootPanel.clear();
								rootPanel.add(new EditContent(authorization,
										"850px", object.getKey()));
								History.newItem(Wahrnehmung.NEW_ENTRY, false);
							}
						}));

		final Column<GwtBeobachtung, GwtBeobachtung> deleteColumn = new IdentityColumn<GwtBeobachtung>(
				new ActionCell<GwtBeobachtung>(Utils.DEL,
						new Delegate<GwtBeobachtung>() {
							@Override
							public void execute(
									final GwtBeobachtung object) {
								decisionBox
										.addOkClickHandler(new ClickHandler() {

											@Override
											public void onClick(ClickEvent arg0) {
												wahrnehmungsService.deleteBeobachtung(
														object.getKey(),
														new AsyncCallback<Void>() {

															@Override
															public void onFailure(
																	Throwable caught) {
																dialogBox
																		.setErrorMessage(caught
																				.getLocalizedMessage());
																dialogBox
																		.center();
															}

															@Override
															public void onSuccess(
																	Void arg0) {
																updateTable();
															}
														});
											}
										});
								decisionBox.center();
							}
						}));

		table = new CellTable<GwtBeobachtung>();
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		table.setPageSize(10);
		table.addColumn(markColumn);
		table.addColumn(dateColumn, "Datum");
		table.addColumn(nameColumn, "Name");
		table.addColumn(sectionColumn, "Bereich");
		table.addColumn(durationColumn, "Dauer");
		table.addColumn(socialColumn, "Sozialform");
		table.addColumn(textColumn, "Beobachtung");
		table.addColumn(userColumn, "von");
		table.addColumn(editColumn);
		table.addColumn(deleteColumn);
		add(table);

		table.addCellPreviewHandler(new Handler<GwtBeobachtung>() {
			
			@Override
			public void onCellPreview(CellPreviewEvent<GwtBeobachtung> event) {
				textArea.setText(event.getValue().getText());
			}
		});
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<GwtBeobachtung> createCheckboxManager());

		asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {
			@Override
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				updateTable();
			}
		};
		asyncDataProvider.addDataDisplay(table);

		updateTable();

		final SimplePager pager = new SimplePager();
		pager.setDisplay(table);

		add(pager);

		textArea.setSize(width, "400px");
		add(textArea);
	}

	private void updateTable() {
		final Range visibleRange = table.getVisibleRange();

		wahrnehmungsService.getBeobachtungen(filter, visibleRange,
				new AsyncCallback<BeobachtungsResult>() {

					@Override
					public void onSuccess(BeobachtungsResult result) {
						asyncDataProvider.updateRowData(
								visibleRange.getStart(),
								result.getBeobachtungen());
						table.setRowCount(result.getRowCount());
						table.redraw();
					}

					

					@Override
					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage();
						dialogBox.center();
					}
				});
	}

	public class FilterButtonHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			filter.setChildKey(nameSelection.getSelectedChildKey());
			filter.setSectionKey(sectionSelection.getSelectedSectionKey());
			updateTable();
		}

	}

}
