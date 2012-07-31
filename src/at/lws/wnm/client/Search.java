package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class Search extends VerticalPanel {

	private final CellTable<GwtBeobachtung> table;

	private final PopUp dialogBox = new PopUp();
	private final WahrnehmungsServiceAsync wahrnehmungsService = GWT.create(WahrnehmungsService.class);
	private final TextArea textArea = new TextArea();
	
	
	public Search(String width) {
		


		
		wahrnehmungsService.getRowCount(new BeobachtungsFilter(), new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(Integer result) {
				table.setRowCount(result.intValue());
			}
		});	
		
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
				return object.getSocial().getText();
			}
		};

		final Column<GwtBeobachtung, String> durationColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return object.getDuration().getText();
			}
		};

		final Column<GwtBeobachtung, String> textColumn = new TextColumn<GwtBeobachtung>() {
			@Override
			public String getValue(GwtBeobachtung object) {
				return Utils.shorten(object.getText(), 20);
			}
		};

		
		table = new CellTable<GwtBeobachtung>();
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		table.setPageSize(7);
		table.addColumn(dateColumn, "Datum");
		table.addColumn(nameColumn, "Name");
		table.addColumn(sectionColumn, "Bereich");
		table.addColumn(durationColumn, "Dauer");
		table.addColumn(socialColumn, "Sozialform");
		table.addColumn(textColumn, "Beobachtung");
		add(table);
		
		final SingleSelectionModel<GwtBeobachtung> selectionModel = new SingleSelectionModel<GwtBeobachtung>();
		table.setSelectionModel(selectionModel );
		 selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				final GwtBeobachtung selectedObject = selectionModel.getSelectedObject();
				textArea.setText(selectedObject.getText());
			}
		});


		final AsyncDataProvider<GwtBeobachtung> asyncDataProvider = new AsyncDataProvider<GwtBeobachtung>() {

			@Override
			protected void onRangeChanged(HasData<GwtBeobachtung> display) {
				final Range visibleRange = display.getVisibleRange();
				final BeobachtungsFilter filter = new BeobachtungsFilter();
				wahrnehmungsService.getBeobachtungen(filter, visibleRange, new AsyncCallback<List<GwtBeobachtung>>() {
					
					@Override
					public void onSuccess(List<GwtBeobachtung> result) {
						updateRowData(visibleRange.getStart(), result);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						dialogBox.setErrorMessage();
						dialogBox.center();
					}
				});
			}
		};
		asyncDataProvider.addDataDisplay(table);

		final SimplePager pager = new SimplePager();
		pager.setDisplay(table);
		
		add(pager);
		
		textArea.setSize(width, "400px");
		add(textArea);
	}

}
