package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TestContext {

	private static ThreadLocal<TestContext> instance = new ThreadLocal<>();

	public static TestContext getInstance() {
		TestContext testContext = instance.get();
		if (testContext == null) {
			testContext = new TestContext();
			instance.set(testContext);
		}
		return testContext;
	}

	private final WebDriverProvider driverProvider = new WebDriverProvider();
	private final Navigation navigation = new Navigation();

	public WebDriverProvider getDriverProvider() {
		return driverProvider;
	}

	public Navigation getNavigation() {
		return navigation;
	}

	public WebDriver getDriver() {
		return driverProvider.get();
	}

	public void returnDriver() {
		driverProvider.returnDriver();
	}

	public void goTo(Page page) {
		navigation.goTo(page);
	}

	public void login(Page page) {
		User loggedInUser = getLoggedInUser();
		if (loggedInUser == null || !page.isAllowed(loggedInUser)) {
			login(page.getDefaultUser());
		}
	}
	
	public void login(User user) {
		navigation.login(user);
	}

	public User getLoggedInUser() {
		return navigation.getLoggedInUser();
	}
	
	public WebElement getLink(String linkText) {
		return Utils.getLink(linkText);
	}

	public WebElement getLinkByDebugId(String debugId) {
		return Utils.getLinkByDebugId(debugId);
		
	}
}
