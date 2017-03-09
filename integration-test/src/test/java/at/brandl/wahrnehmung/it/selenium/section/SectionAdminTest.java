package at.brandl.wahrnehmung.it.selenium.section;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class SectionAdminTest {

	private static TestContext testContext;
	private SectionAdminPage page;
	private String section;

	@Before
	public void setUp() {
		page = new SectionAdminPage();
		testContext = TestContext.getInstance();
		testContext.goTo(page);
		section = "testSection";
	}

	@Test
	public void crud() {
		assertSectionNotExists(section);

		page.enterSection(section);
		page.clickSave();

		assertSectionExists(section);

		// update section
		String updatedSection = section + " updated";
		assertSectionNotExists(updatedSection);

		page.changeSection(section, updatedSection);
		page.clickSave();
		assertSectionExists(updatedSection);
		assertSectionNotExists(section);

		// delete section
		page.deleteSection(updatedSection);
		Configurations.clickOk();
		page.clickSave();

		assertSectionNotExists(updatedSection);
	}

	@Test
	public void duplicateName() {

		assertSectionNotExists(section);

		// enter section
		page.enterSection(section);
		page.clickSave();

		assertSectionExists(section);

		// enter same section again
		page.enterSection(section);
		page.clickSave();

		// error message is shown
		assertDialogBoxShows();
		Configurations.clickClose();

		// delete section
		deleteSection(section);
	}

	@Test
	public void changePosistion() {

		String firstSection = "cc";
		String secondSection = "bb";

		// enter sections
		page.enterSection(firstSection);
		page.clickSave();
		assertSectionExists(firstSection);
		page.enterSection(secondSection);
		page.clickSave();
		assertSectionExists(secondSection);

		// initially sorting is alphabetically
		int firstId = page.getIntTreeId(firstSection);
		int secondId = page.getIntTreeId(secondSection);
		Assert.assertTrue(firstId > secondId);
		
		// move second section down 
		page.moveDown(secondSection);
		page.clickSave();
		assertSectionExists(firstSection);
		assertSectionExists(secondSection);
		
		firstId = page.getIntTreeId(firstSection);
		secondId = page.getIntTreeId(secondSection);
		Assert.assertTrue(firstId < secondId);
		
		// delete sections
		deleteSection(firstSection);
		deleteSection(secondSection);
	}



	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}

	private void deleteSection(String section) {
		assertSectionExists(section);
		page.deleteSection(section);
		Configurations.clickOk();
		page.clickSave();
		assertSectionNotExists(section);
	}
	
	private void assertSectionNotExists(final String section) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.getTreeId(section) == null;
			}
		});
	}

	private void assertSectionExists(final String section) {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.getTreeId(section) != null;
			}
		});

	}

	private void assertDialogBoxShows() {
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return Utils.getByDebugId("dialog").isDisplayed();
			}
		});
	}
}
