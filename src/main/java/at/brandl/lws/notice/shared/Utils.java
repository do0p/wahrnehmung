package at.brandl.lws.notice.shared;

import java.util.Collection;

import at.brandl.lws.notice.shared.model.GwtBeobachtung;

import com.google.gwt.i18n.client.DateTimeFormat;

public class Utils {

	public static String createPrintHtml(GwtBeobachtung beobachtung) {
	
		final String childName = beobachtung.getChildName();
		final String date = Utils.DATE_FORMAT.format(beobachtung.getDate());
		final String duration = beobachtung.getDuration() == null ? ""
				: beobachtung.getDuration().getText();
		final String socialForm = beobachtung.getSocial() == null ? ""
				: beobachtung.getSocial().getText();
		final String author = beobachtung.getUser();
	
		
		final StringBuilder one = new StringBuilder();
		one.append("<table border=\"0\" ><tr>");
		one.append("<td colspan=\"2\">");
		one.append("<b>" + childName + "</b>");
		one.append("</td></tr><tr><td>");
		one.append("Datum:");
		one.append("</td><td>");
		one.append(date);
		one.append("</td></tr><tr><td>");
		one.append("Bereich:");
		one.append("</td><td>");
		one.append(beobachtung.getSectionName());
		one.append("</td></tr><tr><td>");
		one.append("Dauer:");
		one.append("</td><td>");
		one.append(duration);
		one.append("</td></tr><tr><td>");
		one.append("Sozialform:");
		one.append("</td><td>");
		one.append(socialForm);
		one.append("</td></tr><tr><td>");
		one.append("Begleiter:");
		one.append("</td><td>");
		one.append(author);
		one.append("</td></tr><tr><td colspan=\"2\">");
		one.append("<br/>");
		one.append(beobachtung.getText());
		one.append("</td></tr></table>");
		return one.toString();
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

	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat
	.getFormat(Utils.DATE_FORMAT_STRING);
	public static final String DATE_FORMAT_STRING = "d.M.yy";
	public static final String GS_BUCKET_NAME = "wahrnehmung-test.appspot.com";

}
