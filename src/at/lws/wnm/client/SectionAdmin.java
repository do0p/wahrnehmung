package at.lws.wnm.client;

import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SectionAdmin extends VerticalPanel {
	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final TextBox sectionBox;
	private final Button saveButton;
	private final PopUp dialogBox;
	private final SaveSuccess saveSuccess;

	public SectionAdmin() {

		sectionBox = new TextBox();
		saveButton = new Button(Utils.SAVE);
		dialogBox = new PopUp();
		saveSuccess = new SaveSuccess();

		Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label("Bereich"));
		grid.setWidget(0, 1, sectionBox);

		add(grid);
		add(saveButton);

		saveButton.addClickHandler(new SaveClickHandler());
	}

	private void resetForm() {
		sectionBox.setText("");
	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final GwtSection section = new GwtSection();
			section.setSectionName(sectionBox.getValue());
			sectionService.storeSection(section, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					dialogBox.setErrorMessage(caught.getLocalizedMessage());
					dialogBox.setDisableWhileShown(saveButton);
					dialogBox.center();
				}

				@Override
				public void onSuccess(Void result) {
					saveSuccess.center();
					saveSuccess.show();
					resetForm();
				}

			});
		}

	}
}
