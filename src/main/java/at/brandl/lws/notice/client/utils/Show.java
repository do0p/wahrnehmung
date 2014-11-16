package at.brandl.lws.notice.client.utils;

import java.util.Collection;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class Show extends DialogBox {

	private final Labels labels = GWT.create(Labels.class);

	private final Button closeButton;

	private FocusWidget[] focusOnClose;

	private HTML serverResponseLabel;

	private Grid dialogVPanel;

	public Show() {
		serverResponseLabel = new HTML("no entries selected");
		closeButton = new Button(labels.close());
		closeButton.getElement().setId("closeButton");
		closeButton.addClickHandler(new CloseButtonHandler());
		setText("Beobachtungen");
		setAnimationEnabled(true);
		setWidget(createPanel());
		addStyleName("beobachtungen");
	}

	public void setDisableWhileShown(FocusWidget... focusWidget) {
		focusOnClose = focusWidget;
	}

	private Grid createPanel() {
		dialogVPanel = new Grid(2, 1);
		dialogVPanel.setWidget(0, 0, serverResponseLabel);
		dialogVPanel.setWidget(1, 0, closeButton);
		return dialogVPanel;
	}

	public void setBeobachtungen(Collection<GwtBeobachtung> beobachtungen) {
		if (beobachtungen == null || beobachtungen.isEmpty()) {
			dialogVPanel.setWidget(0, 0, serverResponseLabel);
		} else {
			final HTML beobachtungenPanel = new HTML(Utils.createPrintHtml(beobachtungen));
			final ScrollPanel scroll = new ScrollPanel(beobachtungenPanel);
			scroll.setSize(615 +"px", 600 + "px");
			dialogVPanel.setWidget(0, 0, scroll);
		
		}
		closeButton.setFocus(true);
	}

	private class CloseButtonHandler implements ClickHandler {
		public void onClick(ClickEvent event) {
			hide();
		}
	}

	@Override
	public void hide() {
		if (focusOnClose != null) {
			for (FocusWidget widget : focusOnClose) {
				widget.setEnabled(true);
				widget.setFocus(true);
			}
		}
		super.hide();
	}

	@Override
	public void center() {
		if (focusOnClose != null) {
			for (FocusWidget widget : focusOnClose) {
				widget.setEnabled(false);
			}
		}
		super.center();
	}

}
