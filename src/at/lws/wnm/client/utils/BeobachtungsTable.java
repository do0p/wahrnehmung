package at.lws.wnm.client.utils;

import at.lws.wnm.client.EditContent;
import at.lws.wnm.client.Wahrnehmung;
import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
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
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;

public class BeobachtungsTable extends CellTable<GwtBeobachtung> {
	
	private static final String BEOBACHTUNG_DEL_WARNING = "Achtung, diese Beobachtung wird gel&ouml;scht. Der Vorgang nicht mehr r&uuml;ckg&auml;nig gemacht werden!";
	private final WahrnehmungsServiceAsync wahrnehmungsService = GWT
			.create(WahrnehmungsService.class);
	

	private final AsyncDataProvider<GwtBeobachtung> asyncDataProvider;
	

	private final DecisionBox decisionBox;

	private final BeobachtungsFilter filter;
	
	private boolean allSelected;
	private final PopUp dialogBox;

	
	
	public BeobachtungsTable(final Authorization authorization, final MultiSelectionModel<GwtBeobachtung> selectionModel, final BeobachtungsFilter filter, final PopUp dialogBox) {
		
		
		this.filter = filter;
		this.dialogBox = dialogBox;
		decisionBox = new DecisionBox();
		decisionBox.setText(BEOBACHTUNG_DEL_WARNING);
		
		
		
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

					wahrnehmungsService.getBeobachtungen(filter, new Range(0, getRowCount()),
							new AsyncCallback<BeobachtungsResult>() {

								@Override
								public void onFailure(Throwable arg0) {
									dialogBox.setErrorMessage();
									dialogBox.center();
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
					for (GwtBeobachtung contact : selectionModel
							.getSelectedSet()) {
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
										850, object.getKey()));
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
																updateTable();
															}
														});
											}
										});
								decisionBox.center();
							}
						}));

		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		setPageSize(10);
		addColumn(markColumn, selectAllHeader);
		addColumn(dateColumn, "Datum");
		addColumn(nameColumn, "Name");
		addColumn(sectionColumn, "Bereich");
		addColumn(durationColumn, "Dauer");
		addColumn(socialColumn, "Sozialform");
		addColumn(textColumn, "Beobachtung");
		addColumn(userColumn, "von");
		addColumn(editColumn);
		addColumn(deleteColumn);


		setSelectionModel(selectionModel, DefaultSelectionEventManager
				.<GwtBeobachtung> createCheckboxManager());

		asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {
			@Override
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				updateTable();
			}
		};
		asyncDataProvider.addDataDisplay(this);

		updateTable();
	}

	public void updateTable() {
		final Range visibleRange = getVisibleRange();

		wahrnehmungsService.getBeobachtungen(filter, visibleRange,
				new AsyncCallback<BeobachtungsResult>() {

					@Override
					public void onSuccess(BeobachtungsResult result) {
						asyncDataProvider.updateRowData(
								visibleRange.getStart(),
								result.getBeobachtungen());
						setRowCount(result.getRowCount());
						redraw();
					}

					@Override
					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage();
						dialogBox.center();
					}
				});
	}
	
}
