package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login {

	private static final WebDriver driver = WebDriverProvider.driver;

	public static void login(String email, boolean admin) {

		WebElement element = driver.findElement(By.id("email"));
		element.sendKeys(email);

		if (admin) {
			element = driver.findElement(By.id("isAdmin"));
			element.click();
		}

		element.submit();
		
		// wait until javascript is loaded	
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return !driver.findElements(By.linkText("Konfiguration")).isEmpty();
            }
        });
	}

	public static void logout() {
		driver.findElement(By.linkText("abmelden"));
	}

}
