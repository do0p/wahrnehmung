package at.lws.wnm.client.utils;

import java.util.Date;
import java.util.Set;

import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class Utils {
	public static final String MAIN_ELEMENT = "content";
	public static final String DATE_FORMAT_STRING = "d.M.yy";
	public static final String SHORTEN_POSTFIX = "...";

	public static final String LINE_BREAK = "<br/>";

	public static final String UP_ARROW = "↑";
	public static final String DOWN_ARROW = "↓";

	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat(DATE_FORMAT_STRING);
	public static final Format DATEBOX_FORMAT = new DateBox.DefaultFormat(
			DATE_FORMAT);
	public static final int BUTTON_SPACING = 5;
	public static final int FIELD_HEIGHT = 20;
	public static final int BUTTON_WIDTH = 80;
	public static final int ROW_HEIGHT = 40;
	public static final int LISTBOX_WIDTH = 135;

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

	public static void formatLeftCenter(Panel panel, Widget widget,
			int width, int height) {
		format(panel, widget, width, height, HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public static void formatRightCenter(Panel panel, Widget widget,
			int width, int height) {
		format(panel, widget, width, height,
				HasHorizontalAlignment.ALIGN_RIGHT,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public static void formatLeftTop(Panel panel,
			Widget widget, int width, int height) {
		format(panel, widget, width, height,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_TOP);
	}

	public static void formatCenter(Panel panel,
			Widget widget, int width, int height) {
		format(panel, widget, width, height,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);
	}
	
	public static void format(Panel panel, Widget widget, int widthPx,
			int heightPx, final HorizontalAlignmentConstant horizontalAlign,
			final VerticalAlignmentConstant verticalAlign) {
		panel.add(widget);
		final String width = "" + widthPx + "px";
		final String height = "" + heightPx + "px";
		widget.setSize(width , height);
		if (panel instanceof CellPanel) {
			CellPanel cPanel = (CellPanel) panel;
			cPanel.setCellVerticalAlignment(widget, verticalAlign);
			cPanel.setCellHorizontalAlignment(widget, horizontalAlign);
			cPanel.setCellWidth(widget, width);
		}
	}

	public static int min(int int1, int int2) {
		if (int1 < int2) {
			return int1;
		}
		return int2;
	}

	public static UIObject createPrintHtml(Set<GwtBeobachtung> selectedSet) {

		final VerticalPanel all = new VerticalPanel();

		for (GwtBeobachtung beobachtung : selectedSet) {
			final VerticalPanel one = createPrintHtml(beobachtung);
			all.add(one);
			all.add(new HTML("<HR/>"));
		}

		return all;
	}

	public static VerticalPanel createPrintHtml(GwtBeobachtung beobachtung) {
		final DateLabel dateLabel = new DateLabel(Utils.DATE_FORMAT);
		dateLabel.setValue(beobachtung.getDate());
		final HorizontalPanel header = new HorizontalPanel();
		header.setSpacing(10);
		header.add(new Label(beobachtung.getChildName()));
		header.add(new Label("am"));
		header.add(dateLabel);

		final Label sectionName = new Label(beobachtung.getSectionName());
		final Label duration = new Label(beobachtung.getDuration() == null ? ""
				: beobachtung.getDuration().getText());
		final Label socialForm = new Label(beobachtung.getSocial() == null ? ""
				: beobachtung.getSocial().getText());
		final HorizontalPanel section = new HorizontalPanel();
		section.setSpacing(10);
		section.add(sectionName);
		section.add(duration);
		section.add(socialForm);

		final Label text = new Label(beobachtung.getText(), true);
		final Label author = new Label(beobachtung.getUser());

		final VerticalPanel one = new VerticalPanel();
		one.add(header);
		one.add(section);
		one.add(new HTML(LINE_BREAK));
		one.add(text);
		one.add(new HTML(LINE_BREAK));
		one.add(author);
		return one;
	}




	


}
