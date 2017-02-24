package at.brandl.wahrnehmung.it.selenium.dialogues;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.User;

public class DialogueAdminPage implements Page {

	public static final String PAGE_NAME = "DevDatesAdmin";

	@Override
	public void goTo() {
		TestContext context = TestContext.getInstance();
		context.login(this);
		context.getNavigation().goTo(Constants.CONFIG_LINK);
		Configurations.navigateTo(this);
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public boolean isAllowed(User user) {
		return user.isTeacher();
	}

	@Override
	public User getDefaultUser() {
		return Constants.TEACHER;
	}

}
