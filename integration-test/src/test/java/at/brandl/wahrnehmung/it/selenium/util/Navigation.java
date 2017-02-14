package at.brandl.wahrnehmung.it.selenium.util;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import at.brandl.wahrnehmung.it.selenium.user.UserAdminPage;

public class Navigation {

	private final Set<User> created = new HashSet<>();

	private Page page;
	private boolean loggedIn;
	private boolean asAdmin;
	private User user;

	public void login(User user) {
		if (loggedIn) {
			if (this.user.equals(user)) {
				return;
			}
			logout();
		}

		if(!created.contains(user)){
			createUser(user);
			created.add(user);
		}
		
		Login.login(user.getEmail(), false);
		loggedIn = true;
		this.user = user;
	}


	public void login() {
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

	public void logout() {
		if (loggedIn) {
			Login.logout();
			loggedIn = false;
			asAdmin = false;
			user = null;
		}
	}

	public void goTo(Page page) {
		if (!page.equals(this.page)) {
			this.page = page;
			goTo();
		}
	}

	public void goTo() {
		if (page != null) {
			page.goTo();
		}
	}

	public void goTo(String linkText) {
		WebElement element = TestContext.getInstance().getDriver().findElement(By.linkText(linkText));
		element.click();
	}
	

	private void createUser(User user) {
		login();
		goTo("Konfiguration");
		UserAdminPage page = new UserAdminPage();
		Configurations.navigateTo(page);
		if(!page.userListContains(user.getEmail())) {
			page.enterUser(user.getEmail());
			page.checkAdmin(user.isAdmin());
			page.checkSeeAll(user.isTeacher());
			page.checkEditSection(user.isSectionAdmin());
			page.clickSave();
		}
		logout();
	}
}
