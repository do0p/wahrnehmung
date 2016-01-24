package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DragTargetLabel extends DragTargetPanel<DragTargetLabel> {

	private final Label label;

	private DragTargetLabel(String data, VerticalPanel parent,
			boolean insideGroup) {
		super(parent, insideGroup);
		label = new Label(data);
		add(label);
	}

	public static DragTargetLabel valueOf(String data, VerticalPanel panel) {
		return valueOf(data, panel, false);
	}
	
	static DragTargetLabel valueOf(String data, VerticalPanel panel,
			boolean insideGroup) {
		return new DragTargetLabel(data, panel, insideGroup);
	}
}
