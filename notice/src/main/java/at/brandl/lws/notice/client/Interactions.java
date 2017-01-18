package at.brandl.lws.notice.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.GwtInteraction;
import at.brandl.lws.notice.shared.service.InteractionService;
import at.brandl.lws.notice.shared.service.InteractionServiceAsync;

public class Interactions extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final InteractionServiceAsync interactionService = (InteractionServiceAsync) GWT
			.create(InteractionService.class);
	private final NameSelection nameSelection;

	private TextArea textArea;

	public Interactions(Authorization authorization) {
		PopUp dialogBox = new PopUp();
		textArea = new TextArea();
		nameSelection = new NameSelection(dialogBox);
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				fetch();
			}
		});
		add(nameSelection);
		add(textArea);
	}

	private void fetch() {

		interactionService.getInteractions(nameSelection.getSelectedChildKey(), new AsyncCallback<List<GwtInteraction>>() {
			
			@Override
			public void onSuccess(List<GwtInteraction> result) {
				textArea.setValue(result.toString());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				textArea.setValue(caught.getMessage());
				
			}
		});

	}

}
