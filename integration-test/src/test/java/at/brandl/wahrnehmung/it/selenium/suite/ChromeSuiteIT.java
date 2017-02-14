package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;
import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider.DriverType;

@RunWith(Suite.class)
@SuiteClasses({ AdminSuite.class, SectionAdminSuite.class, TeacherSuite.class, UserSuite.class })
public class ChromeSuiteIT {

	@BeforeClass
	public static void setUpClass() {
		WebDriverProvider driverProvider = TestContext.getInstance().getDriverProvider();
		driverProvider.setType(DriverType.CHROME);
		driverProvider.setManagedBySuite(true);
	}

	@AfterClass
	public static void tearDownClass() {
		TestContext.getInstance().getDriverProvider().close();
	}
}
