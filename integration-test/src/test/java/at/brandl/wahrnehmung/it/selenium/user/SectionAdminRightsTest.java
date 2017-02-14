package at.brandl.wahrnehmung.it.selenium.user;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminPage;
import at.brandl.wahrnehmung.it.selenium.dialogues.DialogueAdminPage;
import at.brandl.wahrnehmung.it.selenium.forms.FormsAdminPage;
import at.brandl.wahrnehmung.it.selenium.section.SectionAdminPage;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

public class SectionAdminRightsTest {
	private static TestContext testContext;

	@Before
	public void setUp() {
		testContext = TestContext.getInstance();
		testContext.login(Constants.SECTION_ADMIN);
	}
	
	@Test
	public void availableNavigation() {
		Assert.assertNotNull(testContext.getLink(Constants.NOTICE_LINK));
		Assert.assertNull(testContext.getLink(Constants.FORM_LINK));
		Assert.assertNotNull(testContext.getLink(Constants.SEARCH_LINK));
		Assert.assertNull(testContext.getLink(Constants.INTERACTION_LINK));
		Assert.assertNull(testContext.getLink(Constants.DOCUMENTATION_LINK));
		Assert.assertNotNull(testContext.getLink(Constants.CONFIG_LINK));
	
		testContext.getNavigation().goTo(Constants.CONFIG_LINK);
		Assert.assertNull(testContext.getLinkByDebugId(ChildAdminPage.PAGE_NAME));
		Assert.assertNotNull(testContext.getLinkByDebugId(SectionAdminPage.PAGE_NAME));
		Assert.assertNull(testContext.getLinkByDebugId(UserAdminPage.PAGE_NAME));
		Assert.assertNull(testContext.getLinkByDebugId(DialogueAdminPage.PAGE_NAME));
		Assert.assertNull(testContext.getLinkByDebugId(FormsAdminPage.PAGE_NAME));
	}

	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}
}
