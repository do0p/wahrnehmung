package at.lws.wnm.client.utils;

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

	public Long getSelectedValue() {
		if (isEnabled()) {
			final int index = getSelectedIndex();
			if (index != -1) {
				return getLongValue(index);
			}
		}
		return null;
	}

	public Long getLongValue(int index) {
		final String value = getValue(index);
		if (!value.isEmpty()) {
			return Long.valueOf(value);
		}
		return null;
	}

	public List<Long> getValues() {
		final List<Long> result = new ArrayList<Long>();
		for(int i = 0; i < getItemCount(); i++)
		{
			final Long value = getLongValue(i);
			if(value != null)
			{
				result.add(value);
			}
		}
		return result;
	}
	
}
