package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.WebElement;

public class Configurations {

	public static void navigateTo(Page page) {
		Utils.getByDebugId(page.getPageName()).click();
	}

	public static WebElement getCloseButton() {
		return Utils.getById("closeButton");
	}

	public static WebElement getOkButton() {
		return Utils.getByDebugId("ok");
	}

	public static void clickOk() {
		getOkButton().click();
	}

	public static void clickClose() {
		getCloseButton().click();
	}

}
