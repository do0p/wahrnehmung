package at.brandl.wahrnehmung.it.selenium.user;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.Navigation;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class UserAdminPage implements Page {

	public static final String PAGE_NAME = "UserAdmin";

	@Override
	public void goTo() {
		Navigation navigation = TestContext.getInstance().getNavigation();
		navigation.login(Constants.ADMIN_USER);
		navigation.goTo(Constants.CONFIG_LINK);
		Configurations.navigateTo(this);
	}

	public void enterUser(String user) {
		Utils.clearAndSendKeys(getUserField(), user);
	}

	public void checkAdmin(boolean admin) {
		Utils.selectCheckBox(getAdminField(), admin);
	}

	public void checkSeeAll(boolean seeAll) {
		Utils.selectCheckBox(getSeeAllField(), seeAll);
	}

	public void checkEditSection(boolean editSection) {
		Utils.selectCheckBox(getEditSectionField(), editSection);
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
	
	public Boolean userListContains(String user) {
		return getPosInUserList(user) >= 0;
	}

	public int getPosInUserList(String user) {
		return Utils.getPosInSelect(getUserListSelect(), user);
	}

	public void selectInUserList(String user) {
		getUserListSelect().selectByVisibleText(user);
	}

	public WebElement getUserField() {
		return Utils.getByDebugId("user");
	}

	public WebElement getAdminField() {
		return Utils.getByDebugId("admin-input");
	}

	public WebElement getSeeAllField() {
		return Utils.getByDebugId("seeAll-input");
	}

	public WebElement getEditSectionField() {
		return Utils.getByDebugId("editSections-input");
	}

	public Select getUserListSelect() {
		return new Select(Utils.getByDebugId("userList"));
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
	
	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
