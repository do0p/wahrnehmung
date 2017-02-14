package at.brandl.wahrnehmung.it.selenium.util;

public class Constants {

	public static final String BASE_URL = "http://localhost:8080";

	public static final User ADMIN_USER = new User("admin@lws.at", true, false, false);
	public static final User TEACHER = new User("begleiter@lws.at", false, true, false);
	public static final User SECTION_ADMIN = new User("section_admin@lws.at", false, false, true);
	public static final User USER = new User("normaluser@lws.at", false, false, false);

	public static final String CONFIG_LINK = "Konfiguration";
	public static final String NOTICE_LINK = "Wahrnehmung";
	public static final String FORM_LINK = "Überblicksblätter";
	public static final String SEARCH_LINK = "Suche";
	public static final String INTERACTION_LINK = "Wer mit wem";
	public static final String DOCUMENTATION_LINK = "Dokumentation";

}
