package at.brandl.wahrnehmung.it.selenium.section;

import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.User;

public class SectionAdminPage implements Page {

	public static final String PAGE_NAME = "SectionAdmin";

	@Override
	public void goTo() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public boolean isAllowed(User user) {
		return user.isSectionAdmin();
	}

	@Override
	public User getDefaultUser() {
		return Constants.SECTION_ADMIN;
	}

}
