package at.lws.wnm.client;

import java.util.Set;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Print;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.UIObject;
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

	private Button printButton;
	private boolean allSelected;

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

		table = createTable(authorization);
		add(table);

		final SimplePager pager = new SimplePager();
		pager.setDisplay(table);

		add(pager);

		textArea.setSize(width, "400px");
		add(textArea);
		
		Utils.formatLeftCenter(this, createButtonContainer(), width,
				Utils.ROW_HEIGHT);
	}

	private HorizontalPanel createButtonContainer() {
		final HorizontalPanel buttonContainer = new HorizontalPanel();
		buttonContainer.setWidth("170px");

		printButton = new Button(Utils.PRINT);
		printButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final Set<GwtBeobachtung> selectedSet = selectionModel
						.getSelectedSet();
				if (selectedSet.isEmpty()) {
					// message
				} else {
					Print.it(createPrintHtml(selectedSet));
				}
			}

			private UIObject createPrintHtml(Set<GwtBeobachtung> selectedSet) {

				final VerticalPanel all = new VerticalPanel();

				for (GwtBeobachtung beobachtung : selectedSet) {
					final DateLabel dateLabel = new DateLabel(Utils.DATE_FORMAT);
					dateLabel.setValue(beobachtung.getDate());
					final HorizontalPanel header = new HorizontalPanel();
					header.setSpacing(10);
					header.add(new Label(beobachtung.getChildName()));
					header.add(new Label("am"));
					header.add(dateLabel);

					final Label sectionName = new Label(beobachtung
							.getSectionName());
					final Label duration = new Label(
							beobachtung.getDuration() == null ? ""
									: beobachtung.getDuration().getText());
					final Label socialForm = new Label(
							beobachtung.getSocial() == null ? "" : beobachtung
									.getSocial().getText());
					final HorizontalPanel section = new HorizontalPanel();
					section.setSpacing(10);
					section.add(sectionName);
					section.add(duration);
					section.add(socialForm);

					final Label text = new Label(beobachtung.getText(), true);
					final Label author = new Label(beobachtung.getUser());

					final VerticalPanel one = new VerticalPanel();
					one.add(header);
					one.add(section);
					one.add(new HTML("<BR/>"));
					one.add(text);
					one.add(new HTML("<BR/>"));
					one.add(author);
					all.add(one);
					all.add(new HTML("<HR/>"));
				}

				return all;
			}
		});
		printButton.addStyleName("sendButton");

		Utils.formatLeftCenter(buttonContainer, printButton,
				Utils.BUTTON_WIDTH, Utils.ROW_HEIGHT);

		return buttonContainer;
	}

	private CellTable<GwtBeobachtung> createTable(
			final Authorization authorization) {
		final CellTable<GwtBeobachtung> table = new CellTable<GwtBeobachtung>();

		final Header<Boolean> selectAllHeader = new Header<Boolean>(
				new CheckboxCell(true, false)) {
			
			
			@Override
			public Boolean getValue() {
				return Boolean.valueOf(allSelected);
			}
			
			
			
		};

		selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
			@Override
			public void update(Boolean value) {
				allSelected = value.booleanValue();
				if (value.booleanValue()) {

					wahrnehmungsService.getBeobachtungen(filter, new Range(0,
							table.getRowCount()),
							new AsyncCallback<BeobachtungsResult>() {

								@Override
								public void onFailure(Throwable arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onSuccess(BeobachtungsResult result) {

									for (GwtBeobachtung contact : result
											.getBeobachtungen()) {
										selectionModel.setSelected(contact,
												true);
									}
								}
							});

				} else {
					for (GwtBeobachtung contact : selectionModel.getSelectedSet()) {
						selectionModel.setSelected(contact, false);
					}
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
							public void execute(final GwtBeobachtung object) {
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
																updateTable(table);
															}
														});
											}
										});
								decisionBox.center();
							}
						}));

		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		table.setPageSize(10);
		table.addColumn(markColumn, selectAllHeader);
		table.addColumn(dateColumn, "Datum");
		table.addColumn(nameColumn, "Name");
		table.addColumn(sectionColumn, "Bereich");
		table.addColumn(durationColumn, "Dauer");
		table.addColumn(socialColumn, "Sozialform");
		table.addColumn(textColumn, "Beobachtung");
		table.addColumn(userColumn, "von");
		table.addColumn(editColumn);
		table.addColumn(deleteColumn);

		table.addCellPreviewHandler(new Handler<GwtBeobachtung>() {

			@Override
			public void onCellPreview(CellPreviewEvent<GwtBeobachtung> event) {
				textArea.setText(event.getValue().getText());
			}
		});
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager
				.<GwtBeobachtung> createCheckboxManager());

		asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {
			@Override
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				updateTable(table);
			}
		};
		asyncDataProvider.addDataDisplay(table);

		updateTable(table);

		return table;
	}

	private void updateTable(final CellTable<GwtBeobachtung> table) {
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
			updateTable(table);
		}

	}

}
