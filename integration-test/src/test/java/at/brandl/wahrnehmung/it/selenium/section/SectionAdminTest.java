package at.brandl.wahrnehmung.it.selenium.section;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;

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
	public void addSection() {
		assertSectionNotExists(section);

		page.enterSection(section);
		page.clickSave();

		assertSectionExists(section);
		
		// delete section
		page.deleteSection(section);
		Configurations.clickOk();
		page.clickSave();
		
		assertSectionNotExists(section);
	}

	@AfterClass
	public static void tearDownClass() {
		testContext.returnDriver();
	}

	private void assertSectionNotExists(final String section) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.getTreeId(section) == null;
			}
		});
	}


	private void assertSectionExists(final String section) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		(new WebDriverWait(testContext.getDriver(), 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return page.getTreeId(section) != null;
			}
		});

	}
}
