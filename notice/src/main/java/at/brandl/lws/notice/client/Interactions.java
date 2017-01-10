package at.brandl.lws.notice.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.model.Authorization;

public class Interactions extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);

	private final NameSelection nameSelection;

	private TextArea textArea;

	public Interactions(Authorization authorization) {
		PopUp dialogBox = new PopUp();
		textArea = new TextArea();
		nameSelection = new NameSelection(dialogBox);
		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				fetch(nameSelection.getSelectedChildKey());
			}
		});
		add(nameSelection);
		add(textArea);
	}

	private void fetch(String childKey) {

		String url = "http://localhost:9090/interactions?childKey=" + childKey;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					textArea.setText(exception.getMessage());
				}

				public void onResponseReceived(Request request, Response response) {
					textArea.setText(response.getText());
				}
			});
		} catch (RequestException e) {
			textArea.setText(e.getMessage());
		}

	}

}
