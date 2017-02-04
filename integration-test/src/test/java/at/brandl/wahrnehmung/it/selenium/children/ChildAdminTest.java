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
	public static void tearDownClass() {
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

		assertChildListContainsNot(child);
		page.sendChild(child);

		assertChildListContains(child);
		page.selectInChildList(child);
		assertChild(child);

		page.deleteChild(child);
		assertChildListContainsNot(child);
	}

	@Test
	public void duplicateCreate() {

		assertChildListContainsNot(child);
		page.sendChild(child);
		assertChildListContains(child);

		page.sendChild(child);
		assertDialogBoxShows();
		Configurations.clickClose();
		page.selectInChildList(child);
		assertChild(child);

		page.deleteChild(child);
		assertChildListContainsNot(child);
	}

	@Test
	public void update() {

		assertChildListContainsNot(child);
		page.sendChild(child);
		assertChildListContains(child);

		page.selectInChildList(child);
		Child updatedChild = new Child("Franzi", "Jones", "3.1.03", 2011, 2);
		updatedChild.archived = true;
		page.sendChild(updatedChild);

		assertChildListContainsNot(child);
		assertChildListContains(updatedChild);
		page.selectInChildList(updatedChild);
		assertChild(updatedChild);

		page.deleteChild(updatedChild);
		assertChildListContainsNot(updatedChild);
	}

	@Test
	public void delete() throws InterruptedException {

		page.sendChild(child);
		assertChildListContains(child);

		page.selectInChildList(child);
		Configurations.clickDelete();
		assertDecisionBoxShows();
		Configurations.clickCancel();

		Thread.sleep(500);

		assertChildListContains(child);
		page.selectInChildList(child);
		Configurations.clickDelete();
		assertDecisionBoxShows();
		Configurations.clickOk();
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
		Configurations.clickSave();
		assertChildListContains(child);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());

		// selecting existing child activates delete and cancel button
		page.selectInChildList(child);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertTrue(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		Configurations.clickDelete();
		Configurations.clickOk();
		assertChildListContainsNot(child);

		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertFalse(cancelButton.isEnabled());
	}

	@Test
	public void orderInChildList() {

		assertChildListContainsNot(child);
		page.sendChild(child);
		assertChildListContains(child);

		Child secondChild = new Child("Franz", "Arno", "3.1.03", 2011, 2);
		page.sendChild(secondChild);
		assertChildListContains(secondChild);

		Child thirdChild = new Child("Arno", "Jonas", "3.1.03", 2011, 2);
		page.sendChild(thirdChild);
		assertChildListContains(thirdChild);

		Child fourthChild = new Child("Franz", "Jonas", "3.1.04", 2011, 2);
		page.sendChild(fourthChild);
		assertChildListContains(fourthChild);

		Child fifthChild =  new Child("Arno", "Arno", "3.1.04", 2011, 2);
		fifthChild.archived = true;
		page.sendChild(fifthChild);
		assertChildListContains(fifthChild);
		
		int posOfChild = page.getPosInChildlist(child);
		int posOfSecondChild = page.getPosInChildlist(secondChild);
		int posOfThirdChild = page.getPosInChildlist(thirdChild);
		int posOfFourthChild = page.getPosInChildlist(fourthChild);
		int posOfFifthChild = page.getPosInChildlist(fifthChild);

		Assert.assertTrue("alphabetic order lastname", posOfChild > posOfSecondChild);
		Assert.assertTrue("alphabetic order firstname", posOfChild > posOfThirdChild);
		Assert.assertTrue("order by birthday", posOfChild > posOfFourthChild);
		Assert.assertTrue("order by archived", posOfChild < posOfFifthChild);
		
		page.selectInChildList(child);
		page.deleteChild(child);
		assertChildListContainsNot(child);
		
		page.selectInChildList(secondChild);
		page.deleteChild(secondChild);
		assertChildListContainsNot(secondChild);
		
		page.selectInChildList(thirdChild);
		page.deleteChild(thirdChild);
		assertChildListContainsNot(thirdChild);
		
		page.selectInChildList(fourthChild);
		page.deleteChild(fourthChild);
		assertChildListContainsNot(fourthChild);
		
		page.selectInChildList(fifthChild);
		page.deleteChild(fifthChild);
		assertChildListContainsNot(fifthChild);
	}

	@After
	public void tearDown() {

		if (page.childListContains(child)) {
			page.selectInChildList(child);
			page.deleteChild(child);
		}
	}

	private void assertChild(Child child) {
		Assert.assertEquals(child.firstName, page.getFirstnameField().getAttribute("value"));
		Assert.assertEquals(child.lastName, page.getLastnameField().getAttribute("value"));
		Assert.assertEquals(child.birthDay, page.getBirthdayField().getAttribute("value"));
		Assert.assertEquals(child.beginYear.toString(), page.getBeginYearSelect().getFirstSelectedOption().getText());
		Assert.assertEquals(child.beginGrade.toString(), page.getBeginGradeSelect().getFirstSelectedOption().getText());
		Assert.assertEquals(child.archived, page.getArchivedField().isSelected());
	}

	private void assertDialogBoxShows() {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return Utils.getByDebugId("dialog").isDisplayed();
			}
		});
	}

	private void assertDecisionBoxShows() {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return Utils.getByDebugId("decision").isDisplayed();
			}
		});
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
