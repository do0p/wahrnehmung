package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

@Ignore("does not trigger focus and change events in when browserwindow is not active")
@RunWith(Suite.class)
@SuiteClasses({AdminSuite.class})
public class FirefoxSuiteIT {

	@BeforeClass
	public static void setUpClass() {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("focusmanager.testmode",true);
		profile.setEnableNativeEvents(true);
		WebDriverProvider.driver = new FirefoxDriver(profile);
		WebDriverProvider.driver.get("http://localhost:8080");
	}

	@AfterClass
	public static void tearDownClass() {
		WebDriverProvider.driver.close();
		WebDriverProvider.driver.quit();
		WebDriverProvider.driver = null;
	}
}
