package at.lws.wnm.client;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends VerticalPanel {


	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);

	private final TextBox fnBox;
	private final TextBox lnBox;
	private final DateBox bdBox;
	private final Button saveButton;
	private final PopUp dialogBox;

	public ChildAdmin() {

		fnBox = new TextBox();
		lnBox = new TextBox();
		bdBox = new DateBox();
		saveButton = new Button("Speichern");
		dialogBox = new PopUp();

		Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label("Vorname"));
		grid.setWidget(0, 1, fnBox);
		grid.setWidget(1, 0, new Label("Nachname"));
		grid.setWidget(1, 1, lnBox);
		grid.setWidget(2, 0, new Label("Geburtstag"));
		grid.setWidget(2, 1, bdBox);

		add(grid);
		add(saveButton);

		saveButton.addClickHandler(new SaveClickHandler());
	}


	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final GwtChild child = new GwtChild();
			child.setFirstName(fnBox.getValue());
			child.setLastName(lnBox.getValue());
			child.setBirthDay(bdBox.getValue());
			
			childService.storeChild(child, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					dialogBox.setErrorMessage(caught.getLocalizedMessage());
					dialogBox.setDisableWhileShown(saveButton);
					dialogBox.center();
				}

				@Override
				public void onSuccess(Void result) {
					// do nothing
				}
			});
		}

	}
}
