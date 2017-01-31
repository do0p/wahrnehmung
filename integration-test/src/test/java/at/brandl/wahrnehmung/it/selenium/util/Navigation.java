package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Navigation {

	private Page page;
	private boolean loggedIn;
	private boolean asAdmin;
	
	public void login(boolean admin) {
		if(!loggedIn && (asAdmin != admin)) {
			Login.login("email@example.com", admin);
			loggedIn = true;
			asAdmin = admin;
		}
	}
	
	public void logout() {
		if(loggedIn) {
			Login.logout();
		}
	}
	
	public void goTo(Page page) {
		if(!page.equals(this.page)) {
			this.page = page;
			goTo();
		}
	}
	
	public void goTo() {
		if(page != null) {
			page.goTo();
		}
	}
	
	
	public void goTo(String linkText) {
		WebElement element = TestContext.getInstance().getDriver().findElement(By.linkText(linkText));
		element.click();
	}
}
