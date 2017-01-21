package at.brandl.lws.notice.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
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

	private Grid interactions;

	public Interactions(Authorization authorization) {
		PopUp dialogBox = new PopUp();
		interactions = new Grid();
		nameSelection = new NameSelection(dialogBox);
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				fetch();
			}
		});
		add(nameSelection);
		add(interactions);
		add(new Anchor(labels.showNetwork(), "/vijs/vijs.html", "#"));
	}

	private void fetch() {

		interactionService.getInteractions(nameSelection.getSelectedChildKey(), new AsyncCallback<List<GwtInteraction>>() {
			
			@Override
			public void onSuccess(List<GwtInteraction> result) {
				int rows = result.size();
				interactions.resize(rows + 1, 2);
				interactions.setWidget(0, 0, new Label(labels.with()));
				interactions.setWidget(0, 1, new Label(labels.numberOfContacts()));
				for(int i = 1; i <= rows; i++) {
					final GwtInteraction interaction = result.get(i - 1);
					Anchor link = new Anchor(interaction.getChildName());
					interactions.setWidget(i, 0, link);
					NumberLabel<Integer> count = new NumberLabel<>();
					count.setValue(Integer.valueOf(interaction.getCount()));
					interactions.setWidget(i, 1, count);
					link.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							nameSelection.setSelected(interaction.getChildKey());
							fetch();
						}
					});
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				interactions.resize(1, 1);
				interactions.setWidget(0, 0, new Label(caught.getMessage()));
				
			}
		});

	}

}
