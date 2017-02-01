package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Utils {

	public static WebElement getByDebugId(String id) {
		return getById("gwt-debug-" + id);
	}
	
	public static WebElement getById(String id) {
		return TestContext.getInstance().getDriver().findElement(By.id(id));
	}

	public static void clearAndSendKeys(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
	}
}
