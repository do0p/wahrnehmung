package at.lws.wnm.client.utils;

import java.util.Collection;
import java.util.Date;

import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class Utils {
	public static final String MAIN_ELEMENT = "content";
	public static final String LOGOUT_ELEMENT = "logout";
	public static final String TITLE_ELEMENT = "title";
	public static final String NAVIGATION_ELEMENT = "navigation";
	public static final String PIXEL = "px";
	public static final String HUNDERT_PERCENT = "100%";
	public static final String LINE_BREAK = "<br/>";
	public static final String SEND_BUTTON_STYLE = "sendButton";
	public static final String DELETED_STYLE = "deleted";

	public static final String DATE_FORMAT_STRING = "d.M.yy";
	public static final String SHORTEN_POSTFIX = "...";

	public static final String UP_ARROW = "↑";
	public static final String DOWN_ARROW = "↓";

	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat(DATE_FORMAT_STRING);
	public static final Format DATEBOX_FORMAT = new DateBox.DefaultFormat(
			DATE_FORMAT);
	public static final int SPACING = 3;
	public static final int BUTTON_WIDTH = 80;
	public static final int ROW_HEIGHT = 30;
	public static final int LISTBOX_WIDTH = 135;
	public static final int DATEBOX_WIDTH = 80;
	public static final int APP_WIDTH = 905; // must be at least 870
	public static final int APP_HEIGHT = 700; // must be at least 350
	public static final int NAMESELECTION_WIDTH = 200;
	public static final int BUTTON_CONTAINER_WIDTH = 170;

	public static String formatChildName(GwtChild child) {
		final String firstName = child.getFirstName();
		final String lastName = child.getLastName();
		final Date birthDay = child.getBirthDay();

		final StringBuilder builder = new StringBuilder();
		if (Utils.isNotEmpty(firstName)) {
			builder.append(firstName);
			builder.append(" ");
		}
		if (Utils.isNotEmpty(lastName)) {
			builder.append(lastName);
			builder.append(" ");
		}
		if (birthDay != null) {
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
		if (text == null || text.length() <= length) {
			return text;
		}
		if (length < SHORTEN_POSTFIX.length()) {
			return text.substring(0, length);
		}
		return text.substring(0, length - SHORTEN_POSTFIX.length())
				+ SHORTEN_POSTFIX;
	}

	public static void formatLeftCenter(Panel panel, Widget widget, int width,
			int height) {
		format(panel, widget, width, height, HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public static void formatRightCenter(Panel panel, Widget widget, int width,
			int height) {
		format(panel, widget, width, height,
				HasHorizontalAlignment.ALIGN_RIGHT,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public static void formatLeftTop(Panel panel, Widget widget, int width,
			int height) {
		format(panel, widget, width, height, HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_TOP);
	}

	public static void formatCenter(Panel panel, Widget widget, int width,
			int height) {
		format(panel, widget, width, height,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public static void formatCenter(Panel panel, Widget widget) {
		formatCenter(panel, widget, -1, -1);
	}

	public static void format(Panel panel, Widget widget, int widthPx,
			int heightPx, final HorizontalAlignmentConstant horizontalAlign,
			final VerticalAlignmentConstant verticalAlign) {
		panel.add(widget);
		String width = null;
		String height = null;
		if (widthPx >= 0) {
			width = widthPx + Utils.PIXEL;
			widget.setWidth(width);
		}
		if (heightPx >= 0) {
			height = heightPx + Utils.PIXEL;
			widget.setHeight(height);
		}
		if (panel instanceof CellPanel) {
			CellPanel cPanel = (CellPanel) panel;
			cPanel.setCellVerticalAlignment(widget, verticalAlign);
			cPanel.setCellHorizontalAlignment(widget, horizontalAlign);
			if (width != null) {
				cPanel.setCellWidth(widget, width);
			}
		}
	}

	public static int min(int int1, int int2) {
		if (int1 < int2) {
			return int1;
		}
		return int2;
	}

	public static String createPrintHtml(Collection<GwtBeobachtung> selectedSet) {

		final StringBuilder all = new StringBuilder();
		all.append("<div>");
		
		if (selectedSet != null) {
			for (GwtBeobachtung beobachtung : selectedSet) {
				final String one = createPrintHtml(beobachtung);
				all.append(one);
				all.append("<hr/>");
			}
		}
		all.append("</div>");
		
		return all.toString();
	}

	public static String createPrintHtml(GwtBeobachtung beobachtung) {

		final String childName = beobachtung.getChildName();
		final String date = Utils.DATE_FORMAT.format(beobachtung.getDate());
		final String duration = beobachtung.getDuration() == null ? ""
				: beobachtung.getDuration().getText();
		final String socialForm = beobachtung.getSocial() == null ? ""
				: beobachtung.getSocial().getText();
		final String author = beobachtung.getUser();

		
		final StringBuilder one = new StringBuilder();
		one.append("<table><tr>");
		one.append("<td colspan=3>");
		one.append("<b>" + childName + "</b>");
		one.append("</td></tr><tr><td>");
		one.append("Datum:");
		one.append("</td><td>&nbsp;&nbsp;</td><td>");
		one.append(date);
		one.append("</td></tr><tr><td>");
		one.append("Bereich:");
		one.append("</td><td>&nbsp;&nbsp;</td><td>");
		one.append(beobachtung.getSectionName());
		one.append("</td></tr><tr><td>");
		one.append("Dauer:");
		one.append("</td><td>&nbsp;&nbsp;</td><td>");
		one.append(duration);
		one.append("</td></tr><tr><td>");
		one.append("Sozialform:");
		one.append("</td><td>&nbsp;&nbsp;</td><td>");
		one.append(socialForm);
		one.append("</td></tr><tr><td>");
		one.append("Begleiter:");
		one.append("</td><td>&nbsp;&nbsp;</td><td>");
		one.append(author);
		one.append("</td></tr><tr><td colspan=3>");
		one.append("<br/>");
		one.append(beobachtung.getText());
		one.append("</td></tr></table>");
		return one.toString();
	}

}
