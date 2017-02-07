package at.brandl.wahrnehmung.it.selenium.user;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import at.brandl.wahrnehmung.it.selenium.util.TestContext;

public class UserAdminTest {

	private String user;
	private static UserAdminPage page;
	private static TestContext testContext;



	@Before
	public void setUp() {
		user = "user@gmail.com";
		page = new UserAdminPage();
		testContext = TestContext.getInstance();
		testContext.goTo(page);
	}

	@Test
	public void create() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user);

		page.clickDelete();
		assertUserListContainsNot(user);
	}
	
	private void assertUser(String user) {
		Assert.assertEquals(user, page.getUserField().getAttribute("value"));
	}

	@After
	public void tearDown() {

		if (page.userListContains(user)) {
			page.selectInUserList(user);
			page.clickDelete();
		}
	}
	
	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}
	
	private void assertUserListContains(final String user) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.userListContains(user);
			}
		});
	}

	private void assertUserListContainsNot(final String user) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return !page.userListContains(user);
			}
		});
	}
}
