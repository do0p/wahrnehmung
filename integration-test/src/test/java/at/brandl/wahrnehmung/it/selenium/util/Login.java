package at.brandl.wahrnehmung.it.selenium.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login {

	static void login(String email, boolean admin) {

		final WebDriver driver = TestContext.getInstance().getDriver();

		List<WebElement> elements = driver.findElements(By.linkText("abmelden"));
		if (!elements.isEmpty()) {
			elements.get(0).click();
		}

		// wait until javascript is loaded
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return !driver.findElements(By.id("email")).isEmpty();
			}
		});

		WebElement element = driver.findElement(By.id("email"));
		element.clear();
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

	static void logout() {
		TestContext.getInstance().getDriver().findElement(By.linkText("abmelden"));
	}

}
