package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.Label;

public class DragTargetLabel extends DragTarget<DragTargetLabel> {

	private final Label label;

	private DragTargetLabel(String data, DragContainer parent) {
		super(parent);
		label = new Label(data);
		add(label);
	}

	public static DragTargetLabel valueOf(String data, DragContainer parent) {
		return new DragTargetLabel(data, parent);
	}
	
	@Override
	public String toString() {
		return "DragTargetLabel: " + label.getText();
	}
}
