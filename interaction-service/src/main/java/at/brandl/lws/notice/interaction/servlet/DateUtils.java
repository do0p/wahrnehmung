package at.brandl.lws.notice.interaction.servlet;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private DateUtils() {
	}

	public static Date getStartOfDay(Date date) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("CET"), Locale.GERMAN);
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date getEndOfDay(Date date) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("CET"), Locale.GERMAN);
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}

}
