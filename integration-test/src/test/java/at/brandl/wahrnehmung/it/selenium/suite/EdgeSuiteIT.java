package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.edge.EdgeDriver;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

@Ignore("EdgeDriver is broken, see https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/5238133/")
@RunWith(Suite.class)
@SuiteClasses({AdminSuite.class})
public class EdgeSuiteIT {

	@BeforeClass
	public static void setUpClass() {
		WebDriverProvider.driver = new EdgeDriver();
		WebDriverProvider.driver.get("http://localhost:8080");
	}

	@AfterClass
	public static void tearDownClass() {
		WebDriverProvider.driver.close();
		WebDriverProvider.driver = null;
	}
}
