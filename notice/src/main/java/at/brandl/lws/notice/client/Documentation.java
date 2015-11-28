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
	private boolean overwrite = false;
	private int year = 2015;
	private String code;
	private Button printButton;
	private String selectedChidKey;

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

		printButton = new Button(labels.print());
		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				print();
			}

		});

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		buttonContainer.setWidget(0, 0, printButton);

		return buttonContainer;
	}

	private void print() {
		docService.printDocumentation(getChildKey(), overwrite, year, code,
				new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						Window.open(result, "_blank", "");
						nameSelection.reset();
						selectedChidKey = null;
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

	private String getChildKey() {
		return selectedChidKey != null ? selectedChidKey : nameSelection.getSelectedChildKey();
	}

	public void submit() {
		print();
	}

	public void setChildKey(String childKey) {
		nameSelection.setSelected(childKey);
		this.selectedChidKey = childKey;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
