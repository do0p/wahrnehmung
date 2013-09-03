package at.lws.wnm.client.utils;

import java.util.Date;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class YearSelection extends ListBox {

	private int defaultValue;

	public YearSelection(int startYear) {
		final Date now = new Date();
		int endYear = now.getYear() + 1900;
		if (now.getMonth() < 7) {
			endYear--;
		}
		defaultValue = endYear;
		for (int y = endYear; y >= startYear; y--) {
			addItem(y + " - " + (y + 1), Integer.toString(y));
		}
	}

	public Date[] getSelectedTimeRange() {
		final int index = getSelectedIndex();
		if (index != -1) {
			return createTimeRange(Integer.parseInt(getValue(index)));
		}
		return createTimeRange(defaultValue);
	}

	private Date[] createTimeRange(int startYear) {
		
		final Date startDate = createDate(startYear);
		final Date endDate = createDate(startYear + 1);;
		return new Date[] {startDate, endDate} ;
	}

	private Date createDate(int year) {
		final Date date = new Date();
		date.setYear(year-1900);
		date.setMonth(7);
		CalendarUtil.setToFirstDayOfMonth(date);	
		return date;
	}

}
