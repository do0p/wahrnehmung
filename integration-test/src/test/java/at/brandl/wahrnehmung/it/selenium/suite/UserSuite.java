package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.user.UserRightsTest;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

@RunWith(Suite.class)
@SuiteClasses({UserRightsTest.class})
public class UserSuite {
	
	
	@BeforeClass
	public static void setUpClass() {
		TestContext.getInstance().login(Constants.USER);
	}

	@AfterClass
	public static void tearDownClass() {
		TestContext.getInstance().logout();
	}
	
}
