package at.lws.wnm.client.utils;

import java.util.Iterator;

import at.lws.wnm.client.EditContent;
import at.lws.wnm.client.Labels;
import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.cell.client.ActionCell;
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
	
	private final Labels labels = GWT.create(Labels.class);
	
	private final WahrnehmungsServiceAsync wahrnehmungsService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);
	private final AsyncDataProvider<GwtBeobachtung> asyncDataProvider;
	private final DecisionBox decisionBox;
	private final BeobachtungsFilter filter;
	private boolean allSelected;
	private final PopUp dialogBox;

	public BeobachtungsTable(final Authorization authorization,
			final MultiSelectionModel<GwtBeobachtung> selectionModel,
			final BeobachtungsFilter filter, final PopUp dialogBox) {
		this.filter = filter;
		this.dialogBox = dialogBox;
		this.decisionBox = new DecisionBox();
		this.decisionBox.setText(labels.observationDelWarning());

		Header<Boolean> selectAllHeader = new Header<Boolean>(new CheckboxCell(
				true, false)) {
			public Boolean getValue() {
				return Boolean.valueOf(BeobachtungsTable.this.allSelected);
			}
		};
		selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
			public void update(Boolean value) {
				BeobachtungsTable.this.allSelected = value.booleanValue();
				if (value.booleanValue()) {
					BeobachtungsTable.this.wahrnehmungsService
							.getBeobachtungen(filter, new Range(0,
									BeobachtungsTable.this.getRowCount()),
									new AsyncCallback<BeobachtungsResult>() {
										public void onFailure(Throwable arg0) {
											BeobachtungsTable.this.dialogBox
													.setErrorMessage();
											BeobachtungsTable.this.dialogBox
													.center();
										}

										public void onSuccess(
												BeobachtungsResult result) {
											Iterator<GwtBeobachtung> localIterator = result
													.getBeobachtungen()
													.iterator();

											while (localIterator.hasNext()) {
												GwtBeobachtung contact = (GwtBeobachtung) localIterator
														.next();
												BeobachtungsTable.this
														.getSelectionModel()
														.setSelected(contact,
																true);
											}
										}
									});
				} else {
					Iterator<GwtBeobachtung> localIterator = selectionModel
							.getSelectedSet().iterator();

					while (localIterator.hasNext()) {
						GwtBeobachtung contact = (GwtBeobachtung) localIterator
								.next();
						selectionModel.setSelected(contact, false);
					}
				}
			}
		});
		Column<GwtBeobachtung, Boolean> markColumn = new Column<GwtBeobachtung, Boolean>(
				new CheckboxCell(true, false)) {
			public Boolean getValue(GwtBeobachtung object) {
				return Boolean.valueOf(selectionModel.isSelected(object));
			}
		};
		Column<GwtBeobachtung, String> nameColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				return object.getChildName();
			}
		};
		Column<GwtBeobachtung, String> sectionColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				return object.getSectionName();
			}
		};
		Column<GwtBeobachtung, String> dateColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				return Utils.DATE_FORMAT.format(object.getDate());
			}
		};
		Column<GwtBeobachtung, String> socialColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				if (object.getSocial() != null) {
					return object.getSocial().getText();
				}
				return null;
			}
		};
		Column<GwtBeobachtung, String> durationColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				if (object.getDuration() != null) {
					return object.getDuration().getText();
				}
				return null;
			}
		};
		Column<GwtBeobachtung, String> userColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				return object.getUser();
			}
		};
		Column<GwtBeobachtung, GwtBeobachtung> editColumn = new IdentityColumn<GwtBeobachtung>(
				new ActionCell<GwtBeobachtung>(labels.change(),
						new ActionCell.Delegate<GwtBeobachtung>() {
							public void execute(GwtBeobachtung object) {
								RootPanel rootPanel = RootPanel.get(Utils.MAIN_ELEMENT);
								rootPanel.clear();
								rootPanel.add(new EditContent(authorization,
										850, object.getKey()));
								History.newItem(Navigation.NEW_ENTRY, false);
							}
						}));
		Column<GwtBeobachtung, GwtBeobachtung> deleteColumn = new IdentityColumn<GwtBeobachtung>(new ActionCell<GwtBeobachtung>(labels.delete(),
				new ActionCell.Delegate<GwtBeobachtung>() {
					public void execute(final GwtBeobachtung object) {
						BeobachtungsTable.this.decisionBox
								.addOkClickHandler(new ClickHandler() {
									public void onClick(ClickEvent arg0) {
										BeobachtungsTable.this.wahrnehmungsService
												.deleteBeobachtung(
														object.getKey(),
														new AsyncCallback<Void>() {
															public void onFailure(
																	Throwable caught) {
																BeobachtungsTable.this.dialogBox
																		.setErrorMessage(caught
																				.getLocalizedMessage());
																BeobachtungsTable.this.dialogBox
																		.center();
															}

															public void onSuccess(
																	Void arg0) {
																BeobachtungsTable.this
																		.updateTable();
															}
														});
									}
								});
						BeobachtungsTable.this.decisionBox.center();
					}
				}));
		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		setPageSize(10);
		addColumn(markColumn, selectAllHeader);
		addColumn(dateColumn, labels.date());
		addColumn(nameColumn, labels.name());
		addColumn(sectionColumn, labels.section());
		addColumn(durationColumn, labels.duration());
		addColumn(socialColumn, labels.socialForm());

		addColumn(userColumn, labels.teacher());
		addColumn(editColumn);
		addColumn(deleteColumn);

		setSelectionModel(selectionModel,
				DefaultSelectionEventManager.<GwtBeobachtung>createCheckboxManager ());

		this.asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				BeobachtungsTable.this.updateTable();
			}
		};
		this.asyncDataProvider.addDataDisplay(this);

		updateTable();
	}

	public void updateTable() {
		final Range visibleRange = getVisibleRange();

		this.wahrnehmungsService.getBeobachtungen(this.filter, visibleRange,
				new AsyncCallback<BeobachtungsResult>() {
					public void onSuccess(BeobachtungsResult result) {
						BeobachtungsTable.this.asyncDataProvider.updateRowData(
								visibleRange.getStart(),
								result.getBeobachtungen());
						BeobachtungsTable.this.setRowCount(result.getRowCount());
						BeobachtungsTable.this.redraw();
					}

					public void onFailure(Throwable caught) {
						BeobachtungsTable.this.dialogBox.setErrorMessage();
						BeobachtungsTable.this.dialogBox.center();
					}
				});
	}
}