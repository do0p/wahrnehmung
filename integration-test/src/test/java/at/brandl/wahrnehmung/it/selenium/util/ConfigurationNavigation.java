package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ConfigurationNavigation {

	public static void navigateToChildAdmin() {
		WebElement element = WebDriverProvider.driver.findElement(By.id("gwt-debug-0"));
		element.click();
	}

	
	
}
