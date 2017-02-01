package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.WebElement;

public class Configurations {

	public static void navigateToChildAdmin() {
		Utils.getByDebugId("0").click();
	}

	public static WebElement getSaveButton() {
		return Utils.getByDebugId("save");
	}

	public static WebElement getDeleteButton() {
		return Utils.getByDebugId("delete");
	}

	public static WebElement getCancelButton() {
		return Utils.getByDebugId("cancel");
	}

	public static WebElement getCloseButton() {
		return Utils.getById("closeButton");
	}

	public static WebElement getOkButton() {
		return Utils.getByDebugId("ok");
	}

	public static void clickSave() {
		getSaveButton().click();
	}

	public static void clickDelete() {
		getDeleteButton().click();
	}

	public static void clickOk() {
		getOkButton().click();
	}

	public static void clickCancel() {
		getCancelButton().click();
	}

	public static void clickClose() {
		getCloseButton().click();
	}

}
