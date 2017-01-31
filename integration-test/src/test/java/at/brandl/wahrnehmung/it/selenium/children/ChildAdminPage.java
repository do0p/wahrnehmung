package at.brandl.wahrnehmung.it.selenium.children;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.Navigation;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class ChildAdminPage implements Page {

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

	public void createChild(Child child) {
		enterFirstname(child.firstName);
		enterLastname(child.lastName);
		enterBirthday(child.birthDay);
		selectBeginYear(child.beginYear);
		selectBeginGrade(child.beginGrade);
		Configurations.save();
	}

	public void selectBeginGrade(Integer beginGrade) {
		getBeginGradeSelect().selectByVisibleText(Integer.toString(beginGrade));
	}

	public void selectBeginYear(Integer beginYear) {
		getBeginYearSelect().selectByVisibleText(Integer.toString(beginYear));
	}

	public void enterBirthday(String birthDay) {
		getBirthdayField().sendKeys(birthDay);
	}

	public void enterLastname(String lastName) {
		getLastnameField().sendKeys(lastName);
	}

	public void enterFirstname(String firstName) {
		getFirstnameField().sendKeys(firstName);
	}

	public Select getBeginGradeSelect() {
		return new Select(Utils.getByDebugId("beginGrade"));
	}

	public Select getBeginYearSelect() {
		return new Select(Utils.getByDebugId("beginYear"));
	}

	public WebElement getBirthdayField() {
		return Utils.getByDebugId("birthday");
	}

	public WebElement getLastnameField() {
		return Utils.getByDebugId("lastname");
	}

	public WebElement getFirstnameField() {
		return Utils.getByDebugId("firstname");
	}

	public Boolean childListContains(Child child) {
		Select childList = getChildListSelect();
		for (WebElement element : childList.getOptions()) {
			if (element.getText().equals(child.fullName())) {
				return true;
			}
		}
		return false;
	}

	public void deleteChild(Child child) {
		selectInChildList(child);
		Configurations.delete();
		Configurations.clickOk();
	}

	public void selectInChildList(Child child) {
		getChildListSelect().selectByVisibleText(child.fullName());
	}

	public Select getChildListSelect() {
		return new Select(Utils.getByDebugId("childlist"));
	}

	@Override
	public void goTo() {
		Navigation navigation = TestContext.getInstance().getNavigation();
		navigation.login(true);
		navigation.goTo("Konfiguration");
		Configurations.navigateToChildAdmin();
	}

}
