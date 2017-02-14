package at.brandl.wahrnehmung.it.selenium.user;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

public class UserRightsTest {
	private static TestContext testContext;

	@Before
	public void setUp() {
		testContext = TestContext.getInstance();
		testContext.login(Constants.USER);
	}
	
	@Test
	public void availableNavigation() {
		Assert.assertNotNull(testContext.getLink(Constants.NOTICE_LINK));
		Assert.assertNull(testContext.getLink(Constants.FORM_LINK));
		Assert.assertNotNull(testContext.getLink(Constants.SEARCH_LINK));
		Assert.assertNull(testContext.getLink(Constants.INTERACTION_LINK));
		Assert.assertNull(testContext.getLink(Constants.DOCUMENTATION_LINK));
		Assert.assertNull(testContext.getLink(Constants.CONFIG_LINK));
	}

	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}
}
