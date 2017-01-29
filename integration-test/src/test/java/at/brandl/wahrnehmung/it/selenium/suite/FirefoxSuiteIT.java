package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.firefox.FirefoxDriver;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

@RunWith(Suite.class)
@SuiteClasses({AdminSuite.class})
public class FirefoxSuiteIT {

	@BeforeClass
	public static void setUpClass() {
		WebDriverProvider.driver = new FirefoxDriver();
		WebDriverProvider.driver.get("http://localhost:8080");
	}

	@AfterClass
	public static void tearDownClass() {
		WebDriverProvider.driver.close();
		WebDriverProvider.driver.quit();
		WebDriverProvider.driver = null;
	}
}
