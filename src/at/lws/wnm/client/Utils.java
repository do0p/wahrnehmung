package at.lws.wnm.client;

import java.util.Date;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class Utils {
	static final String DEL = "entf";
	static final String EDIT = "edit";
	static final String NEW = "neu";
	static final String OK = "ok";
	static final String SAVE = "speichern";
	static final String CANCEL = "abbrechen";
	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat("d.M.yy");
	public static final Format DATEBOX_FORMAT = new DateBox.DefaultFormat(
			DATE_FORMAT);
	public static final int BUTTON_SPACING = 5;

	public static String formatChildName(GwtChild child) {
		final String firstName = child.getFirstName();
		final String lastName = child.getLastName();
		final Date birthDay = child.getBirthDay();
		
		final StringBuilder builder = new StringBuilder();
		if(Utils.isNotEmpty(firstName))
		{
			builder.append(firstName);
			builder.append(" ");
		}
		if(Utils.isNotEmpty(lastName))
		{
			builder.append(lastName);
			builder.append(" ");
		}
		if(birthDay != null)
		{
			builder.append("(");
			builder.append(DATE_FORMAT.format(birthDay));
			builder.append(")");
		}
		
		return builder.toString().trim();
	}

	public static boolean isNotEmpty(String lastName) {
		return lastName != null && lastName.matches(".*\\w.*");
	}


	public static boolean isEmpty(String sectionKey) {
		return !isNotEmpty(sectionKey);
	}


}
