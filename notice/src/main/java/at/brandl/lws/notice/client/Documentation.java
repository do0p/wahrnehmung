package at.brandl.lws.notice.client;

import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.Navigation;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.UserGrantRequiredException;
import at.brandl.lws.notice.shared.service.DocsService;
import at.brandl.lws.notice.shared.service.DocsServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Documentation extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final DocsServiceAsync docService = (DocsServiceAsync) GWT
			.create(DocsService.class);

	private final NameSelection nameSelection;

	public Documentation(Authorization authorization, Navigation navigation) {
		PopUp dialogBox = new PopUp();
		this.nameSelection = new NameSelection(dialogBox);
		layout();
	}

	private void layout() {

		nameSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		add(nameSelection);

		Utils.formatCenter(this, createButtonContainer());
	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 1);

		final Button printButton = new Button(labels.print());
		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				docService.printDocumentation(
						nameSelection.getSelectedChildKey(), false, 2015,
						new AsyncCallback<String>() {

							@Override
							public void onSuccess(String result) {
								Window.open(result, "_blank", "");
							}

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof UserGrantRequiredException) {
									Window.Location
											.assign(((UserGrantRequiredException) caught)
													.getAuthorizationUrl());
								}
							}
						});
			}
		});

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		buttonContainer.setWidget(0, 0, printButton);

		return buttonContainer;
	}

}
