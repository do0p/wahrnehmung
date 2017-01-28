package at.brandl.wahrnehmung.it.selenium.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminTest;
import at.brandl.wahrnehmung.it.selenium.util.Navigation;

@RunWith(Suite.class)
@SuiteClasses({ChildAdminTest.class})
public class AdminConfigurationSuite {

	@BeforeClass
	public static void setUpClass() {
		Navigation.goTo("Konfiguration");
	}
	
}
