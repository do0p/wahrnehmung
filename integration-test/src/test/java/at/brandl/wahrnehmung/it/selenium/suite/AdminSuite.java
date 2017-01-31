package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.util.Login;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

@RunWith(Suite.class)
@SuiteClasses({AdminConfigurationSuite.class})
public class AdminSuite {
	
	
	@BeforeClass
	public static void setUpClass() {
		TestContext.getInstance().login(true);
	}

	@AfterClass
	public static void tearDownClass() {
		TestContext.getInstance().logout();
	}
	
}
