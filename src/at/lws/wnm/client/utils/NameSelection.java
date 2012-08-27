package at.lws.wnm.client.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.client.service.ChildService;
import at.lws.wnm.client.service.ChildServiceAsync;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;

public class NameSelection extends SuggestBox {

	public static final String WIDTH = "300px";

	private final PopUp dialogBox;

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final Map<String, Long> childMap = new HashMap<String, Long>();

	public NameSelection(PopUp dialogBox) {
		super(new MultiWordSuggestOracle());
		this.dialogBox = dialogBox;
		updateChildList();
	}

	public Long getSelectedChildKey() {
		return childMap.get(getValue());
	}
	
	public void refresh()
	{
		updateChildList();
	}
	
	private void updateChildList() {
		childService.queryChildren(new AsyncCallback<List<GwtChild>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtChild> result) {
				final MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) getSuggestOracle();
				oracle.clear();
				childMap.clear();
				for (GwtChild child : result) {
					final String formattedChildName = Utils
							.formatChildName(child);
					childMap.put(formattedChildName, child.getKey());
					oracle.add(formattedChildName);
				}
			}

		});

	}

	public void reset() {
		setText("");
	}

}
