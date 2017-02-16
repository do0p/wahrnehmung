package at.brandl.wahrnehmung.it.selenium.children;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.User;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class ChildAdminPage implements Page {

	public static final String PAGE_NAME = "ChildAdmin";

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
		clickSave();
	}

	public void checkArchived(boolean archived) {
		Utils.selectCheckBox(getArchivedField(), archived);
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

	public void clickSave() {
		getSaveButton().click();
	}

	public void clickDelete() {
		getDeleteButton().click();
	}

	public void clickCancel() {
		getCancelButton().click();
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

	public WebElement getSaveButton() {
		return Utils.getByDebugId("save" + getPageName());
	}

	public WebElement getDeleteButton() {
		return Utils.getByDebugId("delete" + getPageName());
	}

	public WebElement getCancelButton() {
		return Utils.getByDebugId("cancel" + getPageName());
	}

	public Boolean childListContains(Child child) {
		return getPosInChildlist(child) >= 0;
	}

	public int getPosInChildlist(Child child) {
		return Utils.getPosInSelect(getChildListSelect(), child.fullName());
	}

	public void selectInChildList(Child child) {
		getChildListSelect().selectByVisibleText(child.fullName());
	}

	public Select getChildListSelect() {
		return new Select(Utils.getByDebugId("childlist"));
	}

	public void deleteChild(Child child) {

		clickDelete();
		Configurations.clickOk();
	}

	@Override
	public void goTo() {
		TestContext context = TestContext.getInstance();
		context.login(this);
		context.getNavigation().goTo(Constants.CONFIG_LINK);
		Configurations.navigateTo(this);
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public boolean isAllowed(User user) {
		return user.isAdmin();
	}
	
	public User getDefaultUser() {
		return Constants.ADMIN_USER;
	}

}
