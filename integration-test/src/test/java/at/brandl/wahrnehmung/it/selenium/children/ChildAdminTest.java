package at.brandl.wahrnehmung.it.selenium.children;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import at.brandl.wahrnehmung.it.selenium.children.Children.Child;
import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

public class ChildAdminTest {

	@BeforeClass
	public static void setUpClass() {
		Configurations.navigateToChildAdmin();
	}

	private Child child;

	@Before
	public void setUp() {
		child = new Child("Franz", "Jonas", "2.10.03", 2010, 1);
	}

	@Test
	public void create() {

		Children.createChild(child);
		assertChildListContains(child);
		Children.deleteChild(child);
		assertChildListContainsNot(child);
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
		Children.enterFirstname(child.firstName + Keys.TAB);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());

		// firstname, lastname and birthday are mandatory and needed to
		// enable save
		Children.enterLastname(child.lastName + Keys.TAB);
		Assert.assertFalse(saveButton.isEnabled());
		Assert.assertFalse(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());
		
		Children.enterBirthday(child.birthDay + Keys.TAB);
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
		Children.selectInChildList(child);
		Assert.assertTrue(saveButton.isEnabled());
		Assert.assertTrue(deleteButton.isEnabled());
		Assert.assertTrue(cancelButton.isEnabled());
			
		Configurations.delete();
		Configurations.clickOk();
	}

	private void assertChildListContains(final Child child) {
		(new WebDriverWait(WebDriverProvider.driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return Children.childListContains(child);
			}
		});
	}

	private void assertChildListContainsNot(final Child child) {
		(new WebDriverWait(WebDriverProvider.driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return !Children.childListContains(child);
			}
		});
	}
}
