package at.brandl.wahrnehmung.it.selenium.children;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import at.brandl.wahrnehmung.it.selenium.children.Children.Child;
import at.brandl.wahrnehmung.it.selenium.util.ConfigurationNavigation;
import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

public class ChildAdminTest {

	public static final WebDriver driver = WebDriverProvider.driver;

	@BeforeClass
	public static void setUpClass() {
		ConfigurationNavigation.navigateToChildAdmin();
	}

	@Test
	public void create() {
		final Child child = new Child("Franz", "Jonas", "2.10.03", 2009, 1);
		Children.createChild(child);
		assertChildListContains(child);
	}

	private void assertChildListContains(final Child child) {
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return Children.childListContains(child);
            }
        });
	}
}
