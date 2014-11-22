package at.brandl.lws.notice.client.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;

public class NameSelection extends SuggestBox {

	private final PopUp dialogBox;

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final Map<String, String> childMap = new HashMap<String, String>();
	private final Map<String, String> reverseChildMap = new HashMap<String, String>();
	private boolean updated;

	private String selectedChildKey;

	public NameSelection(PopUp dialogBox) {
		super(new MultiWordSuggestOracle());
		this.dialogBox = dialogBox;
		updateChildList();
	}

	public String getSelectedChildKey() {
		return childMap.get(getValue());
	}

	// public void refresh()
	// {
	// updateChildList();
	// }

	private void updateChildList() {
		childService.getAll(new AsyncCallback<List<GwtChild>>() {

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
				reverseChildMap.clear();
				for (GwtChild child : result) {
					final String formattedChildName = Utils
							.formatChildName(child);
					childMap.put(formattedChildName, child.getKey());
					reverseChildMap.put(child.getKey(), formattedChildName);
					oracle.add(formattedChildName);
				}
				updated = true;
				if(selectedChildKey != null)
				{
					setSelectedInternal(selectedChildKey);
					selectedChildKey = null;
				}
			}

		});

	}

	public void reset() {
		setText("");
	}

	public void setSelected(String childKey) {
		if (updated) {
			setSelectedInternal(childKey);
		} else {
			selectedChildKey = childKey;
		}
	}

	private void setSelectedInternal(String childKey) {
		setText(reverseChildMap.get(childKey));
	}

	public boolean hasSelection() {
		String value = getValue();
		return value != null && !value.isEmpty();
	}

}
