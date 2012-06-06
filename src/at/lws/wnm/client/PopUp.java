package at.lws.wnm.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PopUp extends DialogBox {

	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private HTML serverResponseLabel;
	private Button closeButton;

	private final FocusWidget focusOnClose;

	public PopUp(FocusWidget focusOnClose) {
		this.focusOnClose = focusOnClose;
		serverResponseLabel = new HTML();
		closeButton = new Button("Close");
		closeButton.getElement().setId("closeButton");
		closeButton.addClickHandler(new CloseButtonHandler());
		setText("Remote Procedure Call");
		setAnimationEnabled(true);
		setWidget(createPanel());
	}

	private VerticalPanel createPanel() {
		final VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		return dialogVPanel;
	}

	public void setErrorMessage() {
		setErrorMessage(SERVER_ERROR);
	}

	public void setErrorMessage(String errorMessage) {
		setText("Remote Procedure Call - Failure");
		serverResponseLabel.addStyleName("serverResponseLabelError");
		serverResponseLabel.setHTML(errorMessage);
		closeButton.setFocus(true);
	}

	public void setMessage(String message) {
		setText("Remote Procedure Call");
		serverResponseLabel.removeStyleName("serverResponseLabelError");
		serverResponseLabel.setHTML(message);
		closeButton.setFocus(true);
	}

	private class CloseButtonHandler implements ClickHandler {
		public void onClick(ClickEvent event) {
			hide();
			focusOnClose.setEnabled(true);
			focusOnClose.setFocus(true);
		}
	}

}
