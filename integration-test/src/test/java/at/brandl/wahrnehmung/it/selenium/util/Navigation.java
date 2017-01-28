package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Navigation {

	private static final WebDriver driver = WebDriverProvider.driver;
	
	public static void goTo(String linkText) {
		WebElement element = driver.findElement(By.linkText(linkText));
		element.click();
	}
}
