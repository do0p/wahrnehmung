package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminTest;
import at.brandl.wahrnehmung.it.selenium.user.AdminRightsTest;
import at.brandl.wahrnehmung.it.selenium.user.UserAdminTest;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

@RunWith(Suite.class)
@SuiteClasses({ AdminRightsTest.class, ChildAdminTest.class, UserAdminTest.class })
public class AdminSuite {

	@BeforeClass
	public static void setUpClass() {
		TestContext.getInstance().login(Constants.ADMIN_USER);
	}

}
