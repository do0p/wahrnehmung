package at.brandl.wahrnehmung.it.selenium.section;

import static org.junit.Assert.*;
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
		assertSectionLinksExist(section, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(section, "aktivieren");

		// update section
		String updatedSection = section + " updated";
		assertSectionNotExists(updatedSection);

		page.changeSection(section, updatedSection);
		page.clickSave();
		assertSectionExists(updatedSection);
		assertSectionNotExists(section);
		assertSectionLinksExist(updatedSection, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(updatedSection, "aktivieren");

		// delete section
		page.deleteSection(updatedSection);
		Configurations.clickOk();
		page.clickSave();

		assertSectionNotExists(updatedSection);
	}

	@Test
	public void crudChildSection() {
		String childSection = section + "Child";
		assertSectionNotExists(section);

		enterSection(section);

		page.clickPlusMinusSign(section);
		page.clickCreate(section);
		page.enterChildSection(section, childSection);

		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);

		assertSectionExists(childSection);
		assertSectionLinksExist(childSection, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(childSection, "aktivieren");
		
		// update section
		String updatedSection = childSection + " Updated";
		assertSectionNotExists(updatedSection);

		page.changeSection(childSection, updatedSection);
		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);

		assertSectionExists(updatedSection);
		assertSectionNotExists(childSection);
		assertSectionLinksExist(updatedSection, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(updatedSection, "aktivieren");

		// delete section
		deleteSection(updatedSection);
		deleteSection(section);
	}

	@Test
	public void duplicateName() {

		assertSectionNotExists(section);

		enterSection(section);

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
		enterSection(firstSection);
		enterSection(secondSection);

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

	@Test
	public void archiveSection() {

		enterSection(section);
		assertSectionLinksExist(section, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(section, "aktivieren");

		// archive section
		page.archiveSection(section);
		page.clickSave();

		assertSectionExists(section);
		assertSectionLinksExist(section, "aktivieren", "entfernen");
		assertSectionLinksNotExist(section, "archivieren", "ändern");

		// delete section
		deleteSection(section);
	}
	
	@Test
	public void archiveChildSection() {

		String childSection = section + "Child";
		
		// enter section
		enterSection(section);
		
		enterChildSection(section, childSection);
		assertSectionLinksExist(childSection, "archivieren", "ändern", "entfernen");
		assertSectionLinksNotExist(childSection, "aktivieren");

		// archive section
		page.archiveSection(childSection);
		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);

		assertSectionExists(childSection);
		assertSectionLinksExist(childSection, "aktivieren", "entfernen");
		assertSectionLinksNotExist(childSection, "archivieren", "ändern");

		// delete section
		deleteSection(section);
	}

	private void enterSection(String section) {
		page.enterSection(section);
		page.clickSave();
		assertSectionExists(section);
	}

	private void enterChildSection(String section, String childSection) {
		page.clickPlusMinusSign(section);
		page.clickCreate(section);
		page.enterChildSection(section, childSection);

		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);

		assertSectionExists(childSection);
		page.clickPlusMinusSign(section);
	}

	@Test
	public void archivedSectionMoveToBottom() {

		String archivedSection1 = "archivedSection1";
		String archivedSection2 = "archivedSection2";

		// enter section
		enterSection(archivedSection1);
		enterSection(archivedSection2);
		enterSection(section);

		// initially sorting is alphabetically
		int archivedId1 = page.getIntTreeId(archivedSection1);
		int archivedId2 = page.getIntTreeId(archivedSection2);
		int sectionId = page.getIntTreeId(section);
		Assert.assertTrue(archivedId1 < archivedId2);
		Assert.assertTrue(archivedId2 < sectionId);

		// archiving moves archived section down
		page.archiveSection(archivedSection1);
		page.clickSave();

		assertSectionExists(archivedSection1);

		archivedId1 = page.getIntTreeId(archivedSection1);
		archivedId2 = page.getIntTreeId(archivedSection2);
		sectionId = page.getIntTreeId(section);
		Assert.assertTrue(archivedId1 > sectionId);
		Assert.assertTrue(sectionId > archivedId2);

		// archiving moves archived section down
		page.archiveSection(archivedSection2);
		page.clickSave();

		assertSectionExists(archivedSection2);

		archivedId1 = page.getIntTreeId(archivedSection1);
		archivedId2 = page.getIntTreeId(archivedSection2);
		sectionId = page.getIntTreeId(section);
		Assert.assertTrue(archivedId2 > archivedId1);
		Assert.assertTrue(archivedId1 > sectionId);

		// unarchiving moves archived section up again
		page.activateSection(archivedSection2);
		page.clickSave();

		assertSectionExists(archivedSection2);

		archivedId1 = page.getIntTreeId(archivedSection1);
		archivedId2 = page.getIntTreeId(archivedSection2);
		sectionId = page.getIntTreeId(section);
		Assert.assertTrue(archivedId1 > sectionId);
		Assert.assertTrue(sectionId > archivedId2);

		// delete sections
		deleteSection(archivedSection1);
		deleteSection(archivedSection2);
		deleteSection(section);

	}

	@Test
	public void archivedChildSectionMoveToBottom() {

		String secondSection = "zSection";
		String childSection = "childSection";
		String archivedChildSection1 = "archivedChildSection1";
		String archivedChildSection2 = "archivedChildSection2";

		enterSection(section);
		enterSection(secondSection);
		
		enterChildSection(section, archivedChildSection1);
		enterChildSection(section, archivedChildSection2);
		enterChildSection(section, childSection);
		
		page.clickPlusMinusSign(section);

		// initially sorting is alphabetically
//		int sectionId = page.getIntTreeId(section);
//		int secondSectionId = page.getIntTreeId(secondSection);
//		int archivedId1 = page.getIntTreeId(archivedChildSection1);
//		int archivedId2 = page.getIntTreeId(archivedChildSection2);
//		int childSectionId = page.getIntTreeId(childSection);
//		Assert.assertTrue(sectionId < archivedId1);
//		Assert.assertTrue(archivedId1 < archivedId2);
//		Assert.assertTrue(archivedId2 < childSectionId);
//		Assert.assertTrue(childSectionId < secondSectionId);

		// archiving moves archived section down
		page.archiveSection(archivedChildSection1);
		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);
		assertSectionExists(archivedChildSection1);

		int archivedId1 = page.getIntTreeId(archivedChildSection1);
		int archivedId2 = page.getIntTreeId(archivedChildSection2);
		int childSectionId = page.getIntTreeId(childSection);
		Assert.assertTrue(archivedId1 > childSectionId);
		Assert.assertTrue(archivedId1 > archivedId2);

		// archiving moves archived section down
		page.archiveSection(archivedChildSection2);
		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);
		assertSectionExists(archivedChildSection2);

		archivedId1 = page.getIntTreeId(archivedChildSection1);
		archivedId2 = page.getIntTreeId(archivedChildSection2);
		childSectionId = page.getIntTreeId(childSection);
		Assert.assertTrue(archivedId2 > childSectionId);
		Assert.assertTrue(archivedId1 > childSectionId);

		// unarchiving moves archived section up again
		page.activateSection(archivedChildSection2);
		page.clickSave();

		assertSectionExists(section);
		page.clickPlusMinusSign(section);
		assertSectionExists(archivedChildSection2);

		archivedId1 = page.getIntTreeId(archivedChildSection1);
		archivedId2 = page.getIntTreeId(archivedChildSection2);
		childSectionId = page.getIntTreeId(childSection);
		Assert.assertTrue(archivedId1 > childSectionId);
		Assert.assertTrue(childSectionId > archivedId2);

		// delete sections
		deleteSection(section);
		deleteSection(secondSection);

	}
	
	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}

	private void assertSectionLinksExist(String section, String... linkTexts) {
		for (String linkText : linkTexts) {
			assertTrue(page.sectionLinkExists(section, linkText));
		}
	}

	private void assertSectionLinksNotExist(String section, String... linkTexts) {
		for (String linkText : linkTexts) {
			assertFalse(page.sectionLinkExists(section, linkText));
		}
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
