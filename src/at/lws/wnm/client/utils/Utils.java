package at.lws.wnm.client.utils;

import java.util.Date;

import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class Utils {
	public static final String SHORTEN_POSTFIX = "...";
	public static final String DEL = "entf";
	public static final String EDIT = "edit";
	public static final String NEW = "neu";
	public static final String OK = "ok";
	public static final String SAVE = "speichern";
	public static final String FILTER = "filter";
	public static final String CANCEL = "abbrechen";
	public static final String ADD = "anlegen";
	public static final String CHANGE = "&auml;ndern";
	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat("d.M.yy");
	public static final Format DATEBOX_FORMAT = new DateBox.DefaultFormat(
			DATE_FORMAT);
	public static final int BUTTON_SPACING = 5;
	public static final String FIELD_HEIGHT = "20px";
	public static final String BUTTON_WIDTH = "80px";
	public static final String ROW_HEIGHT = "40px";
	public static final String LISTBOX_WIDTH = "135px";

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

	public static String shorten(String text, int length) {
		if(text == null||text.length() <= length)
		{
			return text;
		}
		if(length < SHORTEN_POSTFIX.length())
		{
			return text.substring(0, length);
		}
		return text.substring(0, length - SHORTEN_POSTFIX.length()) + SHORTEN_POSTFIX;
	}

	public static void formatLeftCenter(CellPanel panel, Widget widget, String width, String height) {
		panel.add(widget);
		panel.setCellVerticalAlignment(widget,
				HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setCellHorizontalAlignment(widget,
				HasHorizontalAlignment.ALIGN_LEFT);
		widget.setSize(width, height);
		panel.setCellWidth(widget, width + "px");
	}


	


}
