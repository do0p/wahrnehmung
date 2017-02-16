package at.brandl.wahrnehmung.it.selenium.util;

public interface Page {

	void goTo();

	String getPageName();
	
	boolean isAllowed(User user);

	User getDefaultUser();
}
