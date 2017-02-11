package at.brandl.wahrnehmung.it.selenium.user;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
	public void createUser() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, false, false, false);

		page.clickDelete();
		assertUserListContainsNot(user);
	}

	@Test
	public void createAdminUser() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.checkAdmin(true);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, true, false, false);

		page.clickDelete();
		assertUserListContainsNot(user);
	}

	@Test
	public void createTeacher() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.checkSeeAll(true);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, false, true, false);

		page.clickDelete();
		assertUserListContainsNot(user);
	}

	@Test
	public void createSectionAdmin() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.checkEditSection(true);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, false, false, true);

		page.clickDelete();
		assertUserListContainsNot(user);
	}

	@Test
	public void createSuperUser() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.checkEditSection(true);
		page.checkSeeAll(true);
		page.checkAdmin(true);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, true, true, true);

		page.clickDelete();
		assertUserListContainsNot(user);
	}

	@Test
	public void updateUser() {

		assertUserListContainsNot(user);
		page.enterUser(user + Keys.TAB);
		page.clickSave();

		assertUserListContains(user);
		page.selectInUserList(user);
		assertUser(user, false, false, false);

		String newUser = "newUser@gmail.com";
		page.enterUser(newUser + Keys.TAB);
		page.checkEditSection(true);
		page.checkSeeAll(true);
		page.checkAdmin(true);
		page.clickSave();

		assertUserListContainsNot(user);
		assertUserListContains(newUser);
		page.selectInUserList(newUser);
		assertUser(newUser, true, true, true);

		page.clickDelete();
		assertUserListContainsNot(newUser);
	}

	@Test
	public void buttonState() {

		WebElement saveButton = page.getSaveButton();
		WebElement deleteButton = page.getDeleteButton();
		WebElement cancelButton = page.getCancelButton();

		// initially all is disabled
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());

		// as soon as there are changes, cancel is enabled
		page.checkAdmin(true);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		// only user is mandatory
		page.checkAdmin(false);
		page.enterUser(user + Keys.TAB);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		// save empties the form
		page.clickSave();
		assertUserListContains(user);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());

		// selecting existing child activates delete and cancel button
		page.selectInUserList(user);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertTrue(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		page.clickDelete();
		assertUserListContainsNot(user);

		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());
	}

	@Test
	public void orderInUserList() {
		String dora = "dora@gmail.com";
		page.enterUser(dora + Keys.TAB);
		page.clickSave();
		assertUserListContains(dora);
		
		String alfred = "alfred@zoo.com";
		page.enterUser(alfred + Keys.TAB);
		page.clickSave();
		assertUserListContains(alfred);

		int posDora = page.getPosInUserList(dora);
		int posAlfred = page.getPosInUserList(alfred);
		Assert.assertTrue(posDora > posAlfred);
		
		page.selectInUserList(dora);
		page.clickDelete();
		
		page.selectInUserList(alfred);
		page.clickDelete();
		
	}

	private void assertUser(String user, boolean admin, boolean seeAll, boolean sectionAdmin) {
		Assert.assertEquals(user, page.getUserField().getAttribute("value"));
		Assert.assertEquals(admin, page.getAdminField().isSelected());
		Assert.assertEquals(seeAll, page.getSeeAllField().isSelected());
		Assert.assertEquals(sectionAdmin, page.getEditSectionField().isSelected());
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
