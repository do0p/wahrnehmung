package at.brandl.wahrnehmung.it.selenium.children;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import at.brandl.wahrnehmung.it.selenium.util.WebDriverProvider;

public class Children {

	private static final WebDriver driver = WebDriverProvider.driver;

	public static class Child {

		public String firstName;
		public String lastName;
		public String birthDay;
		public Integer beginYear;
		public Integer beginGrade;
		public boolean archived;

		public Child(String firstName, String lastName, String birthDay, Integer beginYear, Integer beginGrade) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthDay = birthDay;
			this.beginYear = beginYear;
			this.beginGrade = beginGrade;
		}

		public Child() {

		}

		public String fullName() {
			return String.format("%s %s (%s)", firstName, lastName, birthDay);
		}
	}

	public static void createChild(Child child) {
		WebElement element = driver.findElement(By.id("gwt-debug-firstname"));
		element.sendKeys(child.firstName);
		element = driver.findElement(By.id("gwt-debug-lastname"));
		element.sendKeys(child.lastName);
		element = driver.findElement(By.id("gwt-debug-birthday"));
		element.sendKeys(child.birthDay);
		element = driver.findElement(By.id("gwt-debug-beginYear"));
		Select select = new Select(element);
		select.selectByVisibleText(Integer.toString(child.beginYear));
		element = driver.findElement(By.id("gwt-debug-beginGrade"));
		select = new Select(element);
		select.selectByVisibleText(Integer.toString(child.beginGrade));
		element = driver.findElement(By.id("gwt-debug-save"));
		element.click();
	}
	
	public static Boolean childListContains(Child child) {
		Select childList = new Select(driver.findElement(By.id("gwt-debug-childlist")));
		for(WebElement element : childList.getOptions()) {
			if(element.getText().equals(child.fullName())) {
				return true;
			}
		}
		return false;
	}
}
