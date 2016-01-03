package at.brandl.lws.notice.client;

import java.util.List;

import at.brandl.lws.notice.client.utils.DecisionBox;
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
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
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
	private Grid documentationGrid;
	private DecisionBox decisionBox;

	public Documentation(Authorization authorization, Navigation navigation) {
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);
		documentationGrid = new Grid(0, 3);
		decisionBox = new DecisionBox();
		decisionBox.setText(labels.documentationDelWarning());
		layout();
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				retrieveDocumentations();
				// updateButtonPanel();
			}

		});

	}

	private void layout() {

		setSpacing(15);

		nameSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		add(nameSelection);
		add(documentationGrid);

		Utils.formatLeftCenter(this, createButton(), -1, -1);
	}

	private Widget createButton() {

		printButton = new Button(labels.create());
		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				print();
			}

		});

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		String yearString = year + " / " + (year + 1);
		printButton.setTitle(labels.createDocumentation(yearString));

		return printButton;
	}

	private void retrieveDocumentations() {

		documentationGrid.resizeRows(0);
		String childKey = nameSelection.getSelectedChildKey();
		if (childKey == null) {
			return;
		}

		docService.getDocumentations(childKey,
				new AsyncCallback<List<GwtDocumentation>>() {

					@Override
					public void onSuccess(List<GwtDocumentation> documentations) {
						int numRows = documentations.size();
						documentationGrid.resizeRows(numRows);
						for (int i = 0; i < numRows; i++) {
							GwtDocumentation documentation = documentations
									.get(i);
							addDocumentation(i, documentation);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						showErrorMessage(caught.getLocalizedMessage());
					}
				});
	}

	private void addDocumentation(int row, GwtDocumentation documentation) {
		documentationGrid.setWidget(row, 0,
				createDocumentationEntry(documentation));
		documentationGrid.setWidget(row, 2,
				createDeleteButton(documentation.getId(), row));
	}

	private Button createDeleteButton(final String id, final int row) {
		Button button = new Button(labels.delete());
		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				decisionBox.addOkClickHandler(new ClickHandler() {
					public void onClick(ClickEvent arg0) {
						docService.deleteDocumentation(id,
								new AsyncCallback<Void>() {
									public void onFailure(Throwable caught) {
										showErrorMessage(caught
												.getLocalizedMessage());
									}

									public void onSuccess(Void arg0) {
										documentationGrid.removeRow(row);
									}
								});
					}
				});
				decisionBox.center();
			}
		});
		return button;
	}

	private void print() {
		docService.createDocumentation(getChildKey(), year,
				new AsyncCallback<GwtDocumentation>() {

					@Override
					public void onSuccess(GwtDocumentation documentation) {

						int row = documentationGrid.getRowCount();
						documentationGrid.resizeRows(row + 1);
						addDocumentation(row, documentation);

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
		return new Anchor(documentation.getTitle(), documentation.getUrl(),
				"_blank");
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
