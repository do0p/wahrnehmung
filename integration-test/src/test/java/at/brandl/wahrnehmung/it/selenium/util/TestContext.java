package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.WebDriver;

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

	public void login() {
		navigation.login();
	}

	public void login(User user) {
		navigation.login(user);
	}
	
	public void logout() {
		navigation.logout();
	}
}
