package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ConfigurationNavigation {

	private static final WebDriver driver = WebDriverProvider.driver;

	public static void navigateToChildAdmin() {
		WebElement element = driver.findElement(By.id("gwt-debug-0"));
		element.click();
	}

	
	
}
