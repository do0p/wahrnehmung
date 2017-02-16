package at.brandl.wahrnehmung.it.selenium.util;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import at.brandl.wahrnehmung.it.selenium.user.UserAdminPage;

public class Navigation {

	private final Set<User> created = new HashSet<>();

	private Page page;
	private boolean loggedIn;
	private boolean asAdmin;
	private User user;

	public void goTo(Page page) {
		if (!page.equals(this.page)) {
			page.goTo();
			this.page = page;
		}
	}

	public void goTo(String linkText) {
		TestContext.getInstance().getDriver().findElement(By.linkText(linkText)).click();
	}

	void login(User user) {
		if (loggedIn) {
			if (this.user.equals(user)) {
				return;
			}
			logout();
		}

		if (!created.contains(user)) {
			createUser(user);
			created.add(user);
		}

		Login.login(user.getEmail(), false);
		loggedIn = true;
		this.user = user;
	}

	User getLoggedInUser() {
		return user;
	}

	private void login() {
		if (loggedIn) {
			if (asAdmin) {
				return;
			}
			logout();
		}

		Login.login("email@example.com", true);
		loggedIn = true;
		asAdmin = true;
	}

	private void logout() {
		if (loggedIn) {
			Login.logout();
			loggedIn = false;
			asAdmin = false;
			user = null;
		}
	}

	private void createUser(User user) {
		login();
		goTo(Constants.CONFIG_LINK);
		UserAdminPage page = new UserAdminPage();
		Configurations.navigateTo(page);
		if (!page.userListContains(user.getEmail())) {
			page.enterUser(user.getEmail() + Keys.TAB);
			page.checkAdmin(user.isAdmin());
			page.checkSeeAll(user.isTeacher());
			page.checkEditSection(user.isSectionAdmin());
			page.clickSave();
		}
		logout();
	}

}
