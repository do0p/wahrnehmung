package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminTest;
import at.brandl.wahrnehmung.it.selenium.user.UserAdminTest;

@RunWith(Suite.class)
@SuiteClasses({ ChildAdminTest.class, UserAdminTest.class })
public class AdminConfigurationSuite {

}
