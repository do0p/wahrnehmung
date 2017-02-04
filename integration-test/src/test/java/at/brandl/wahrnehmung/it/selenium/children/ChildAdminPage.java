package at.brandl.wahrnehmung.it.selenium.children;

import java.util.List;

import org.openqa.selenium.Keys;
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

	public void sendChild(Child child) {
		enterFirstname(child.firstName);
		enterLastname(child.lastName);
		enterBirthday(child.birthDay + Keys.TAB);
		selectBeginYear(child.beginYear);
		selectBeginGrade(child.beginGrade);
		checkArchived(child.archived);
		Configurations.clickSave();
	}

	public void checkArchived(boolean archived) {
		WebElement archivedField = getArchivedField();
		boolean selected = archivedField.isSelected();
		if (selected != archived) {
			archivedField.sendKeys(Keys.SPACE);
		}
	}

	public void selectBeginGrade(Integer beginGrade) {
		getBeginGradeSelect().selectByVisibleText(Integer.toString(beginGrade));
	}

	public void selectBeginYear(Integer beginYear) {
		getBeginYearSelect().selectByVisibleText(Integer.toString(beginYear));
	}

	public void enterBirthday(String birthDay) {
		Utils.clearAndSendKeys(getBirthdayField(), birthDay);
	}

	public void enterLastname(String lastName) {
		Utils.clearAndSendKeys(getLastnameField(), lastName);
	}

	public void enterFirstname(String firstName) {
		Utils.clearAndSendKeys(getFirstnameField(), firstName);
	}

	public WebElement getArchivedField() {
		return Utils.getByDebugId("archived-input");
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
		return getPosInChildlist(child) >= 0;
	}

	public int getPosInChildlist(Child child) {
		Select childList = getChildListSelect();
		List<WebElement> options = childList.getOptions();
		int index = -1;
		for (int i = 0; i < options.size(); i++) {
			WebElement element = options.get(i);
			if (element.getText().equals(child.fullName())) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void deleteChild(Child child) {
		
		Configurations.clickDelete();
		Configurations.clickOk();
	}

	public void selectInChildList(Child child) {
		Select childListSelect = getChildListSelect();
		childListSelect.selectByVisibleText(child.fullName());
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
