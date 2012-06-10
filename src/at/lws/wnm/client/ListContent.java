package at.lws.wnm.client;

import com.google.gwt.user.client.ui.HorizontalPanel;

public class ListContent extends AbstractTextContent {

	public ListContent(String width) {
		super(width);
	}

	@Override
	protected HorizontalPanel createButtonContainer() {
		return new HorizontalPanel();
	}


}
