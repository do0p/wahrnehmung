package at.brandl.lws.notice.client.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.brandl.lws.notice.model.GwtChild;
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
	private final Map<String, GwtChild> childMap = new HashMap<>();
	private final Map<String, String> reverseChildMap = new HashMap<String, String>();
	private boolean updated;
	private boolean includeArchived;

	private String selectedChildKey;

	public NameSelection(PopUp dialogBox) {
		super(new MultiWordSuggestOracle());
		this.dialogBox = dialogBox;
		updateChildList();
	}

	public String getSelectedChildKey() {
		GwtChild child = childMap.get(getValue());
		if (child == null) {
			return null;
		}
		return child.getKey();
	}

	// public void refresh()
	// {
	// updateChildList();
	// }

	private void updateChildList() {
		childService.queryChildren(new AsyncCallback<List<GwtChild>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtChild> result) {
				childMap.clear();
				reverseChildMap.clear();
				for (GwtChild child : result) {
					final String formattedChildName = Utils
							.formatChildName(child);
					childMap.put(formattedChildName, child);
					reverseChildMap.put(child.getKey(), formattedChildName);
				}

				updateOracle();

				updated = true;
				if (selectedChildKey != null) {
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

	public void setIncludeArchived(boolean includeArchived) {
		this.includeArchived = includeArchived;
		updateOracle();
	}

	private void updateOracle() {
		final MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) getSuggestOracle();
		oracle.clear();
		for (Entry<String, GwtChild> childEntry : childMap.entrySet()) {
			GwtChild child = childEntry.getValue();
			if (includeArchived || child.getArchived() == null
					|| !child.getArchived()) {
				oracle.add(childEntry.getKey());
			}
		}

	}

}
