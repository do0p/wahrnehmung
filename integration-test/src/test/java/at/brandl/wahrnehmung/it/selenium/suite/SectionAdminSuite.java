package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.section.SectionAdminTest;
import at.brandl.wahrnehmung.it.selenium.user.SectionAdminRightsTest;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

@RunWith(Suite.class)
@SuiteClasses({ SectionAdminRightsTest.class, SectionAdminTest.class })
public class SectionAdminSuite {

	@BeforeClass
	public static void setUpClass() {
		TestContext.getInstance().login(Constants.SECTION_ADMIN);
	}

}
