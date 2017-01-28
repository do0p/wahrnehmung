package org.openqa.selenium.example;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Ignore
public class Selenium2ExampleIT  {
	
	private static ChromeDriver driver;

	@BeforeClass
	public static void setUp() {
		driver = new ChromeDriver();
	}
	
	@Test
    public void main() {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.

        // And now use this to visit Google
        driver.get("http://www.google.com");
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        // Find the text input element by its name
        WebElement element = driver.findElement(By.name("q"));

        // Enter something to search for
        element.sendKeys("Cheese!");

        // Now submit the form. WebDriver will find the form for us from the element
        element.submit();

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        
        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith("cheese!");
            }
        });

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());
        
    }
	
	@Test
    public void main2() throws InterruptedException {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.

        // And now use this to visit Google
        driver.get("http://localhost:8080");
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        String email = "test@example.com";
        boolean admin = true;
        login(email, admin);

//        // Check the title of the page
//        System.out.println("Page title is: " + driver.getTitle());
//        
//        // Google's search is rendered dynamically with JavaScript.
//        // Wait for the page to load, timeout after 10 seconds
//        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
//            public Boolean apply(WebDriver d) {
//                return d.getTitle().toLowerCase().startsWith("cheese!");
//            }
//        });
//
//        // Should see: "cheese! - Google Search"
//        System.out.println("Page title is: " + driver.getTitle());
        
        Thread.sleep(10000);
        
    }

	private void login(String email, boolean admin) {
		// Find the text input element by its name
        WebElement element = driver.findElement(By.id("email"));

        // Enter something to search for
		element.sendKeys(email);

		if(admin) {
        element = driver.findElement(By.id("isAdmin"));
        element.click();
		}
        // Now submit the form. WebDriver will find the form for us from the element
        element.submit();
	}
	
	@AfterClass
	public static void shutDownClass() {
        //Close the browser
        driver.quit();
	}
}