package at.brandl.lws.notice.client.utils;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import at.brandl.lws.notice.client.Labels;

public class DecisionBox extends DialogBox {

	private final Labels labels = GWT.create(Labels.class);
	
	public class HideClickHandler implements ClickHandler {
			@Override
			public void onClick(ClickEvent event) {
				DecisionBox.this.hide();
			}
	}

	private final Button okButton;
	private final Button cancelButton;
	private final HTML text;
	private HandlerRegistration okHandler;
	private HandlerRegistration cancelHandler;
	
	public DecisionBox()
	{
		super(false, true);
		okButton = new Button(labels.ok());
		okButton.ensureDebugId("ok");
		okButton.addClickHandler(new HideClickHandler());
		cancelButton = new Button(labels.cancel());
		cancelButton.ensureDebugId("cancel");
		cancelButton.addClickHandler(new HideClickHandler());
		text = new HTML();
		
		final VerticalPanel root = new VerticalPanel();
		root.add(text);
		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setSpacing(Utils.SPACING);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		root.add(buttonPanel);
		
		add(root);
	}

	public void setText(String text)
	{
		this.text.setHTML(text);
	}
	
	public void addOkClickHandler(ClickHandler okClickHandler) {
		if(okHandler != null)
		{
			okHandler.removeHandler();
		}
		okHandler = okButton.addClickHandler(okClickHandler);
	}

	public void addCancelClickHandler(ClickHandler cancelClickHandler) {
		if(cancelHandler != null)
		{
			cancelHandler.removeHandler();
		}
		cancelHandler = cancelButton.addClickHandler(cancelClickHandler);
	}
}
