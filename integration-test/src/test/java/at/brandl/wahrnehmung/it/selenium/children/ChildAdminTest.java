package at.brandl.wahrnehmung.it.selenium.children;

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

import at.brandl.wahrnehmung.it.selenium.children.ChildAdminPage.Child;
import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class ChildAdminTest {


	private Child child;
	private static ChildAdminPage page;
	private static TestContext testContext;
	

	@AfterClass 
	public static void tearDownClass(){
		testContext.returnDriver();
	}
	
	@Before
	public void setUp() {
		child = new Child("Franz", "Jonas", "2.10.03", 2010, 1);
		page = new ChildAdminPage();
		testContext = TestContext.getInstance();
		testContext.goTo(page);
	}

	@Test
	public void create() {

		page.createChild(child);
		assertChildListContains(child);
		page.deleteChild(child);
		assertChildListContainsNot(child);
	}

	@Test
	public void duplicateCreate() {

		page.createChild(child);
		assertChildListContains(child);

		page.createChild(child);
		assertDialogBoxShows();
		Configurations.clickClose();

		page.deleteChild(child);
		assertChildListContainsNot(child);
	}

	private void assertDialogBoxShows() {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return Utils.getByDebugId("dialog").isDisplayed();
			}
		});
	}

	@After
	public void tearDown() {
		Configurations.cancel();

	}

	@Test
	public void buttonState() {

		WebElement saveButton = Configurations.getSaveButton();
		WebElement deleteButton = Configurations.getDeleteButton();
		WebElement cancelButton = Configurations.getCancelButton();

		// initially all is disabled
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());

		// as soon as there are changes, cancel is enabled
		page.enterFirstname(child.firstName + Keys.TAB);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		// firstname, lastname and birthday are mandatory and needed to
		// enable save
		page.enterLastname(child.lastName + Keys.TAB);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		page.enterBirthday(child.birthDay + Keys.TAB);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		// save empties the form
		Configurations.save();
		assertChildListContains(child);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());

		// selecting existing child activates delete and cancel button
		page.selectInChildList(child);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertTrue(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		Configurations.delete();
		Configurations.clickOk();
		assertChildListContainsNot(child);

		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());
	}

	private void assertChildListContains(final Child child) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.childListContains(child);
			}
		});
	}

	private void assertChildListContainsNot(final Child child) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return !page.childListContains(child);
			}
		});
	}
}
