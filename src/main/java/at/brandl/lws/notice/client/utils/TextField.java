package at.brandl.lws.notice.client.utils;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

public class TextField extends Grid {

	private final RichTextArea textArea;
	private final RichTextToolbar toolBar;
	private final Widget addOn;

	public TextField() {
		this(new HorizontalPanel());
	}
	
	public TextField(Widget addOn) {
		super(2, 1);
		
		this.addOn = addOn;
		textArea = new RichTextArea();
		toolBar = new RichTextToolbar(textArea);
		
	
		layout();

	}

	private void layout() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.setSpacing(Utils.SPACING * 3);
		bar.add(toolBar);
		bar.add(addOn);
		
		setStyleName("cw-RichText");
		setWidget(0, 0, bar);
		setWidget(1, 0, textArea);
	}

	public void addKeyPressHandler(KeyPressHandler keyPressHandler) {
		textArea.addKeyPressHandler(keyPressHandler);
	}

	public void addBlurHandler(BlurHandler blurHandler) {
		textArea.addBlurHandler(blurHandler);
	}

	public void addMouseOutHandler(MouseOutHandler mouseOutHandler) {
		textArea.addMouseOutHandler(mouseOutHandler);
	}

	public String getText() {
		return textArea.getHTML();
	}

	public void setText(String html) {
		textArea.setHTML(html);
	}

	public void setEnabled(boolean enabled) {
		textArea.setEnabled(enabled);
		toolBar.setEnabled(enabled);
	}

	@Override
	public void setSize(String width, String height) {
		textArea.setSize(width, height);
	}

	public void addText(String text) {
		textArea.setHTML(textArea.getHTML() + text);
	}
}
