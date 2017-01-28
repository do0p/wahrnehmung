package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.util.Login;

@RunWith(Suite.class)
@SuiteClasses({AdminConfigurationSuite.class})
public class AdminSuite {
	
	
	@BeforeClass
	public static void setUpClass() {
		Login.login("email@example.com", true);
	}

	@AfterClass
	public static void tearDownClass() {
		Login.logout();
	}
	
}
