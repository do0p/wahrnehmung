package at.brandl.lws.notice.client.utils;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import at.brandl.lws.notice.client.Labels;

public class YearSelection extends ListBox {

	private static final String DIALOGUE = "dialogue";

	public static class YearSelectionResult {
		Date startDate;
		Date endDate;
		boolean sinceLastDevelopementDialogue;

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public boolean isSinceLastDevelopementDialogue() {
			return sinceLastDevelopementDialogue;
		}

		public Date[] getTimeRange() {
			return new Date[] { startDate, endDate };
		}
	}

	private final Labels labels = (Labels) GWT.create(Labels.class);

	public YearSelection(int startYear) {
		addItem(labels.developementDialogueDate(), DIALOGUE);
		final Date now = new Date();
		int endYear = now.getYear() + 1900;
		if (now.getMonth() < 7) {
			endYear--;
		}
		for (int y = endYear; y >= startYear; y--) {
			addItem(y + " - " + (y + 1), Integer.toString(y));
		}
	}

	public YearSelectionResult getSelectedTimeRange() {
		final int index = getSelectedIndex();
		if (index != -1) {
			return createTimeSelectionResult(getValue(index));
		}
		return createTimeSelectionResult(DIALOGUE);
	}

	private YearSelectionResult createTimeSelectionResult(String value) {

		YearSelectionResult result = new YearSelectionResult();
		if (value.equals(DIALOGUE)) {
			result.sinceLastDevelopementDialogue = true;
		} else {

			int startYear = Integer.parseInt(value);
			result.startDate = createDate(startYear);
			result.endDate = createDate(startYear + 1);
		}
		return result;
	}

	private Date createDate(int year) {
		final Date date = new Date();
		date.setYear(year - 1900);
		date.setMonth(7);
		CalendarUtil.setToFirstDayOfMonth(date);
		return date;
	}

}
