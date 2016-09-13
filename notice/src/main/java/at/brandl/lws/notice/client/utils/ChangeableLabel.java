package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

public class ChangeableLabel extends SimplePanel implements ChangesAware {

	private static final Logger LOGGER = Logger.getLogger("ChangeableLabel");
	private final Collection<ChangeListener> listeners = new ArrayList<>(); 
	private Label label;
	private TextBox textBox;

   
	
	public ChangeableLabel(String data, String style) {
		label = new Label(data);
		label.setStyleName(style);
		textBox = new TextBox();
		textBox.setStyleName(style);
		textBox.getElement().setDraggable(Element.DRAGGABLE_TRUE);
//		textBox.getElement().setDraggable(Element.DRAGGABLE_FALSE);
//		textBox.addMouseDownHandler(new MouseDownHandler() {
//			@Override
//			public void onMouseDown(MouseDownEvent event) {
//				event.stopPropagation();	
//			}
//		});
		textBox.addDragStartHandler(new DragStartHandler() {
			
			@Override
			public void onDragStart(DragStartEvent event) {
				LOGGER.log(Level.FINE, "in onDragStart of " + this);
				event.preventDefault();
				event.stopPropagation();
			}
		});
		
		label.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				textBox.setText(label.getText());
				setWidget(textBox);
			}
		});

		textBox.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				int charCode = event.getNativeKeyCode();
				LOGGER.log(Level.FINE, "keycode: " + charCode);
				if (KeyCodes.KEY_ENTER == charCode) {
					label.setText(textBox.getText());
					setWidget(label);
					notifyChanges();
				} else if (KeyCodes.KEY_ESCAPE == charCode) {
					setWidget(label);
				}
				event.stopPropagation();
			}
		});
		setWidget(label);
	}

	public String getText() {

		return label.getText();
	}

	@Override
	public String toString() {
		return "ChangeableLabel: " + label.getText();
	}

	@Override
	public void registerChangeListener(ChangeListener listener) {
		LOGGER.log(Level.INFO, "register listener " + listener);
		listeners.add(listener);
	}
	
	private void notifyChanges() {
		LOGGER.log(Level.INFO, "notify listeners");
		for (ChangeListener listener : listeners) {
			LOGGER.log(Level.INFO, "notify listener " + listener);
			listener.notifyChange();
		}
	}
}
