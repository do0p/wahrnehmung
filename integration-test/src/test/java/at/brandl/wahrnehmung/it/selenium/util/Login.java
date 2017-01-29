package at.brandl.wahrnehmung.it.selenium.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login {

	public static void login(String email, boolean admin) {

		
		List<WebElement> elements = WebDriverProvider.driver.findElements(By.linkText("abmelden"));
		if(!elements.isEmpty()) {
			elements.get(0).click();
		}
		
		// wait until javascript is loaded	
		(new WebDriverWait(WebDriverProvider.driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return !WebDriverProvider.driver.findElements(By.id("email")).isEmpty();
            }
        });
		
		WebElement element = WebDriverProvider.driver.findElement(By.id("email"));
		element.sendKeys(email);

		if (admin) {
			element = WebDriverProvider.driver.findElement(By.id("isAdmin"));
			element.click();
		}

		element.submit();
		
		// wait until javascript is loaded	
		(new WebDriverWait(WebDriverProvider.driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return !WebDriverProvider.driver.findElements(By.linkText("Konfiguration")).isEmpty();
            }
        });
	}

	public static void logout() {
		WebDriverProvider.driver.findElement(By.linkText("abmelden"));
	}

}
