package at.brandl.lws.notice.client;

import java.util.List;

import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.Navigation;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.DocumentationAlreadyExistsException;
import at.brandl.lws.notice.model.GwtDocumentation;
import at.brandl.lws.notice.model.UserGrantRequiredException;
import at.brandl.lws.notice.shared.service.DocsService;
import at.brandl.lws.notice.shared.service.DocsServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

public class Documentation extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final DocsServiceAsync docService = (DocsServiceAsync) GWT
			.create(DocsService.class);

	private final NameSelection nameSelection;
	private int year = 2015;
	private Button printButton;
	private String selectedChidKey;
	private PopUp dialogBox;
	private VerticalPanel documentationPanel;

	public Documentation(Authorization authorization, Navigation navigation) {
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);
		documentationPanel = new VerticalPanel();
		layout();
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				retrieveDocumentations();
//				updateButtonPanel();
			}

			
		});
		
	}

	private void layout() {

		HorizontalPanel root = new HorizontalPanel();
		root.setSpacing(30);
		add(root);

		nameSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		root.add(nameSelection);
		root.add(documentationPanel);

		Utils.formatLeftCenter(this, createButtonContainer(), -1, -1);
	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 2);
		buttonContainer.setCellSpacing(Utils.SPACING);

		printButton = new Button(labels.create());
		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				print();
			}

		});

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);
		
		String yearString = year + " / " + (year+1);
		buttonContainer.setWidget(0,0, new Label(labels.createDocumentation(yearString)));
		buttonContainer.setWidget(0, 1, printButton);

		return buttonContainer;
	}

	private void retrieveDocumentations() {
		
	documentationPanel.clear();	
	String childKey = nameSelection.getSelectedChildKey();
	if (childKey == null) {
		return;
	}
	
	docService.getDocumentations(childKey, new AsyncCallback<List<GwtDocumentation>>() {
		
		@Override
		public void onSuccess(List<GwtDocumentation> documentations) {
			for(GwtDocumentation documentation : documentations) {
				documentationPanel.add(createDocumentationEntry(documentation));
			}
		}


		
		@Override
		public void onFailure(Throwable caught) {
			showErrorMessage(caught.getLocalizedMessage());
		}
	});	
	}
	
	private void print() {
		docService.createDocumentation(getChildKey(), year,
				new AsyncCallback<GwtDocumentation>() {

					@Override
					public void onSuccess(GwtDocumentation documentation) {
						documentationPanel.add(createDocumentationEntry(documentation));
						Window.open(documentation.getUrl(), "_blank", "");
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
						if (caught instanceof DocumentationAlreadyExistsException) {
							String docUrl = ((DocumentationAlreadyExistsException) caught)
									.getDocUrl();
							showErrorMessage(labels.documentationExistsWarning(
									nameSelection.getText(), year, docUrl));
						} else {
							showErrorMessage(caught.getLocalizedMessage());
						}
					}
				});
	}

	private Widget createDocumentationEntry(GwtDocumentation documentation) {
		return new Anchor(documentation.getTitle(), documentation.getUrl(), "_blank");
	}
	
	private void showErrorMessage(String message) {
		dialogBox.setErrorMessage(message);
		dialogBox.setDisableWhileShown(printButton);
		dialogBox.center();
	}

	private String getChildKey() {
		return selectedChidKey != null ? selectedChidKey : nameSelection
				.getSelectedChildKey();
	}

	public void submit() {
		print();
	}

	public void setChildKey(String childKey) {
		nameSelection.setSelected(childKey);
		this.selectedChidKey = childKey;
	}

	public void setYear(int year) {
		this.year = year;
	}
}
