package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminTest;

@RunWith(Suite.class)
@SuiteClasses({ChildAdminTest.class})
public class AdminConfigurationSuite {

}
