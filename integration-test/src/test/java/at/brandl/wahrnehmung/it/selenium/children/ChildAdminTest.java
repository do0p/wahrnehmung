package at.brandl.wahrnehmung.it.selenium.children;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

public class ChildAdminTest {

	private static final WebDriver driver = WebDriverProvider.driver;
	
	@BeforeClass
	public static void setUpClass() {
		WebElement element = driver.findElement(By.id("gwt-debug-0"));
		element.click();
	}
	
	@Test
	public void crud() {
		WebElement element = driver.findElement(By.id("gwt-debug-firstname"));
		element.sendKeys("Franz");
		element = driver.findElement(By.id("gwt-debug-lastname"));
		element.sendKeys("Jonas");
		element = driver.findElement(By.id("gwt-debug-birthday"));
		element.sendKeys("02.10.2003");
		element = driver.findElement(By.id("gwt-debug-beginYear"));
		Select select = new Select(element);
		select.selectByVisibleText("2009");
		element = driver.findElement(By.id("gwt-debug-beginGrade"));
		select = new Select(element);
		select.selectByVisibleText("1");
		element = driver.findElement(By.id("gwt-debug-save"));
		element.click();
	}
}
