package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;

public class SectionSelectionBox extends ListBox {

	private final String defaultEntry;

	public SectionSelectionBox(String defaultEntry) {
		this.defaultEntry = defaultEntry;
		init();
	}

	private void init() {
		addItem(defaultEntry, "");
	}

	@Override
	public void clear() {
		super.clear();
		init();
	}

	public String getSelectedValue() {
		if (isEnabled()) {
			final int index = getSelectedIndex();
			if (index != -1) {
				return getValue(index);
			}
		}
		return null;
	}

	public List<String> getValues() {
		final List<String> result = new ArrayList<String>();
		for (int i = 0; i < getItemCount(); i++) {
			final String value = getValue(i);
			if (value != null) {
				result.add(value);
			}
		}
		return result;
	}

	@Override
	public String getValue(int index) {
		final String value = super.getValue(index);
		return value == null || value.isEmpty() ? null : value;
	}

}
