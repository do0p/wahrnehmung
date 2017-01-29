package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.chrome.ChromeDriver;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

@RunWith(Suite.class)
@SuiteClasses({AdminSuite.class})
public class ChromeSuiteIT {

	@BeforeClass
	public static void setUpClass() {
		WebDriverProvider.driver = new ChromeDriver();
		WebDriverProvider.driver.get("http://localhost:8080");
	}

	@AfterClass
	public static void tearDownClass() {
		WebDriverProvider.driver.close();
		WebDriverProvider.driver.quit();
		WebDriverProvider.driver = null;
	}
}
