package at.brandl.lws.notice.client.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

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
	private static final String TITLE = "h3";
	private static final String BORDER_0 = "border=\"0\"";
	private static final String BR = "<br/>";
	private static final String COLSPAN_2 = "colspan=\"2\"";
	private static final String BOLD = "b";
	private static final String CELL = "td";
	private static final String ROW = "tr";
	private static final String TABLE = "table";
	private static final String INPUT = "input";
	public static final String MAIN_ELEMENT = "content";
	public static final String LOGOUT_ELEMENT = "logout";
	// public static final String TITLE_ELEMENT = "title";
	public static final String NAVIGATION_ELEMENT = "navigation";
	public static final String PIXEL = "px";
	public static final String HUNDRED_PERCENT = "100%";
	public static final String LINE_BREAK = BR;
	public static final String SEND_BUTTON_STYLE = "sendButton";
	public static final String DELETED_STYLE = "deleted";
	public static final String ARCHIVED_STYLE = "archived";

	public static final String SHORTEN_POSTFIX = "...";

	public static final String UP_ARROW = "↑";
	public static final String DOWN_ARROW = "↓";

	public static final String DATE_FORMAT_STRING = "d.M.yy";
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
	public static final int QUESTIONNAIRE_WIDTH = 600;

	public static String createPrintHtml(GwtBeobachtung beobachtung) {

		final String childName = beobachtung.getChildName();
		final String date = DATE_FORMAT.format(beobachtung.getDate());
		final String duration = beobachtung.getDuration() == null ? ""
				: beobachtung.getDuration().getText();
		final String socialForm = beobachtung.getSocial() == null ? ""
				: beobachtung.getSocial().getText();
		final String author = beobachtung.getUser();

		StringBuilder one = new StringBuilder();

		one.append(row(cell(COLSPAN_2, bold(childName))));
		one.append(row(cell("Datum:") + cell(date)));
		one.append(row(cell("Bereich:") + cell(beobachtung.getSectionName())));
		one.append(row(cell("Dauer:") + cell(duration)));
		one.append(row(cell("Sozialform:" + cell(socialForm))));
		one.append(row(cell("Begleiter:" + cell(author))));
		one.append(row(cell(COLSPAN_2, LINE_BREAK + beobachtung.getText())));

		return table(BORDER_0, one.toString());
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

	public static String formatChildName(GwtChild child) {
		final String firstName = child.getFirstName();
		final String lastName = child.getLastName();
		final Date birthDay = child.getBirthDay();

		final StringBuilder builder = new StringBuilder();
		if (at.brandl.lws.notice.shared.Utils.isNotEmpty(firstName)) {
			builder.append(firstName);
			builder.append(" ");
		}
		if (at.brandl.lws.notice.shared.Utils.isNotEmpty(lastName)) {
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

	public static String addDashes(String text) {
		return "- " + text + " -";
	}

	public static String createPrintQuestionnaire(
			GwtQuestionnaire questionnaire, GwtQuestionnaireAnswers answers) {

		StringBuilder content = new StringBuilder();

		content.append(row(cell(COLSPAN_2, title(questionnaire.getTitle()))));

		for (GwtQuestionGroup group : questionnaire.getGroups()) {

			content.append(createGroup(group, answers));
		}

		return table(content.toString());
	}

	private static String createGroup(GwtQuestionGroup group,
			GwtQuestionnaireAnswers answers) {

		StringBuilder groupStr = new StringBuilder();

		boolean isGroup = group.getTitle() != null;

		if (isGroup) {
			groupStr.append(row(cell(COLSPAN_2, bold(group.getTitle()))));
		}

		for (GwtQuestion question : group.getQuestions()) {

			GwtAnswer answer = answers.getAnswer(question.getKey());
			groupStr.append(createQuestion(question, answer, isGroup));
		}

		return groupStr.toString();
	}

	private static String createQuestion(GwtQuestion question,
			GwtAnswer answer, boolean isGroup) {

		String labelStr = createLabel(isGroup, question);
		String answerStr = createAnswer(question.getTemplate(), answer);

		return row(labelStr + answerStr);
	}

	private static String createLabel(boolean isGroup, GwtQuestion question) {

		String label = question.getLabel();
		return cell(isGroup ? label : bold(label));
	}

	private static String createAnswer(GwtAnswerTemplate template,
			GwtAnswer answer) {

		if (template instanceof GwtMultipleChoiceAnswerTemplate) {

			StringBuilder optionsStr = new StringBuilder();

			GwtMultipleChoiceAnswerTemplate multipleChoiceTemplate = (GwtMultipleChoiceAnswerTemplate) template;
			GwtMultipleChoiceAnswer multipleChoiceAnswer = (GwtMultipleChoiceAnswer) answer;

			Collection<String> answerValue;
			if (multipleChoiceAnswer == null) {
				answerValue = Collections.emptyList();
			} else {
				answerValue = multipleChoiceAnswer.getValue();
			}

			for (GwtMultipleChoiceOption option : multipleChoiceTemplate
					.getOptions()) {

				optionsStr.append(createOption(answerValue, option));
			}

			return cell(table(BORDER_0, optionsStr.toString()));
		}

		throw new IllegalArgumentException("unknown template type " + template);
	}

	private static String createOption(Collection<String> answerValue,
			GwtMultipleChoiceOption option) {

		boolean checked = answerValue != null
				&& answerValue.contains(option.getValue());
		return row(cell(option.getLabel()) + cell(checkBox(checked)));
	}

	private static String checkBox(boolean checked) {
		
		String attributes = "type=\"checkbox\"";
		if (checked) {
			attributes += " checked";
		}
		return tag(INPUT, attributes, "");
	}

	private static String table(String contents) {
		return tag(TABLE, "", contents);
	}

	private static String table(String attributes, String contents) {
		return tag(TABLE, attributes, contents);
	}

	private static String row(String contents) {
		return row("", contents);
	}

	private static String row(String attributes, String contents) {
		return tag(ROW, attributes, contents);
	}

	private static String cell(String contents) {
		return cell("", contents);
	}

	private static String cell(String attributes, String contents) {
		return tag(CELL, attributes, contents);
	}

	private static String title(String contents) {
		return tag(TITLE, "", contents);
	}
	
	private static String bold(String contents) {
		return tag(BOLD, "", contents);
	}

	private static String tag(String tagName, String attributes, String contents) {
		return startTag(tagName, attributes) + contents + endTag(tagName);
	}

	private static String startTag(String tagName, String attributes) {
		return "<" + tagName + " " + attributes + " >";
	}

	private static String endTag(String tagName) {
		return "</" + tagName + ">";
	}

}
