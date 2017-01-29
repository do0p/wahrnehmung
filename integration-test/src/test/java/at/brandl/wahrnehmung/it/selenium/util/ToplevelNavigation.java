package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ToplevelNavigation {

	public static void goTo(String linkText) {
		WebElement element = WebDriverProvider.driver.findElement(By.linkText(linkText));
		element.click();
	}
}
