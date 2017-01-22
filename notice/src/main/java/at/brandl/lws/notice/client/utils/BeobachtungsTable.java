package at.brandl.lws.notice.client.utils;

import java.util.Iterator;

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
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;

import at.brandl.lws.notice.client.EditContent;
import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.BeobachtungsResult;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.shared.service.WahrnehmungsService;
import at.brandl.lws.notice.shared.service.WahrnehmungsServiceAsync;

public class BeobachtungsTable extends CellTable<GwtBeobachtung> {
	private static final int PAGE_SIZE = 10;

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
			final BeobachtungsFilter filter, final PopUp dialogBox,
			final EditContent editContent) {
		this.filter = filter;
		this.dialogBox = dialogBox;
		this.decisionBox = new DecisionBox();
		this.decisionBox.setText(labels.observationDelWarning());

		Header<Boolean> selectAllHeader = new Header<Boolean>(new CheckboxCell(
				true, false)) {
			public Boolean getValue() {
				return Boolean.valueOf(allSelected);
			}
		};
		selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
			public void update(Boolean value) {
				allSelected = value.booleanValue();
				if (value.booleanValue()) {
					wahrnehmungsService
							.getBeobachtungen(filter, new Range(0,
									getRowCount()),
									new AsyncCallback<BeobachtungsResult>() {
										public void onFailure(Throwable arg0) {
											dialogBox
													.setErrorMessage();
											dialogBox
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
				return Utils.DATE_FORMAT.format(object
						.getDate());
			}
		};
//		Column<GwtBeobachtung, String> socialColumn = new TextColumn<GwtBeobachtung>() {
//			public String getValue(GwtBeobachtung object) {
//				if (object.getSocial() != null) {
//					return object.getSocial().getText();
//				}
//				return null;
//			}
//		};
//		Column<GwtBeobachtung, String> durationColumn = new TextColumn<GwtBeobachtung>() {
//			public String getValue(GwtBeobachtung object) {
//				if (object.getDuration() != null) {
//					return object.getDuration().getText();
//				}
//				return null;
//			}
//		};
		Column<GwtBeobachtung, String> userColumn = new TextColumn<GwtBeobachtung>() {
			public String getValue(GwtBeobachtung object) {
				return object.getUser();
			}
		};
		Column<GwtBeobachtung, GwtBeobachtung> editColumn = new IdentityColumn<GwtBeobachtung>(
				new ActionCell<GwtBeobachtung>(labels.change(),
						new ActionCell.Delegate<GwtBeobachtung>() {
							public void execute(GwtBeobachtung object) {
								String key = object.getKey();
								if (key == null || object.isArchived()) {
									return;
								}
								editContent.setKey(key);
								History.newItem(Navigation.NEW_ENTRY, true);
							}
						}));
		Column<GwtBeobachtung, GwtBeobachtung> deleteColumn = new IdentityColumn<GwtBeobachtung>(
				new ActionCell<GwtBeobachtung>(labels.delete(),
						new ActionCell.Delegate<GwtBeobachtung>() {
							public void execute(GwtBeobachtung object) {
								final String key = object.getKey();
								if (key == null || object.isArchived()) {
									return;
								}
								decisionBox
										.addOkClickHandler(new ClickHandler() {
											public void onClick(ClickEvent arg0) {
												wahrnehmungsService
														.deleteBeobachtung(
																key,
																new AsyncCallback<Void>() {
																	public void onFailure(
																			Throwable caught) {
																		dialogBox
																				.setErrorMessage(caught
																						.getLocalizedMessage());
																		dialogBox
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
								decisionBox.center();
							}
						}));
		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		setPageSize(PAGE_SIZE);
		addColumn(markColumn, selectAllHeader);
		addColumn(dateColumn, labels.date());
		addColumn(nameColumn, labels.name());
		addColumn(sectionColumn, labels.section());
//		addColumn(durationColumn, labels.duration());
//		addColumn(socialColumn, labels.socialForm());

		addColumn(userColumn, labels.teacher());
		addColumn(editColumn);
		addColumn(deleteColumn);

		setSelectionModel(selectionModel,
				DefaultSelectionEventManager
						.<GwtBeobachtung> createCheckboxManager());

		this.asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				updateTable();
			}
		};
		this.asyncDataProvider.addDataDisplay(this);

	}

	public void clear() {
		setRowCount(0);
	}

	public void updateTable() {
		final Range visibleRange = getVisibleRange();
		asyncDataProvider.updateRowCount(0, false);
		this.wahrnehmungsService.getBeobachtungen(this.filter, visibleRange,
				new AsyncCallback<BeobachtungsResult>() {
					public void onSuccess(BeobachtungsResult result) {
						
						asyncDataProvider.updateRowData(
								visibleRange.getStart(),
								result.getBeobachtungen());
						asyncDataProvider.updateRowCount(result.getRowCount(), true);
						
						
					}

					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage();
						dialogBox.center();
					}
				});
	}
}
