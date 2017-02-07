package at.brandl.wahrnehmung.it.selenium.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class Utils {

	public static WebElement getByDebugId(String id) {
		return getById("gwt-debug-" + id);
	}
	
	public static WebElement getById(String id) {
		return TestContext.getInstance().getDriver().findElement(By.id(id));
	}

	public static void clearAndSendKeys(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
	}

	public static void selectCheckBox(WebElement checkBox, boolean select) {
		boolean selected = checkBox.isSelected();
		if (selected != select) {
			checkBox.sendKeys(Keys.SPACE);
		}
	}

	public static int getPosInSelect(Select select, String text) {
		List<WebElement> options = select.getOptions();
		int index = -1;
		for (int i = 0; i < options.size(); i++) {
			WebElement element = options.get(i);
			if (element.getText().equals(text)) {
				index = i;
				break;
			}
		}
		return index;
	}
}
