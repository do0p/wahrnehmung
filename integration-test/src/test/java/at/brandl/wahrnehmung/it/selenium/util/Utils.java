package at.brandl.wahrnehmung.it.selenium.util;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class Utils {

	public static WebElement getByDebugId(String id) {
		return getById("gwt-debug-" + id);
	}

	public static List<WebElement> findByDebugId(String id) {
		return findById("gwt-debug-" + id);
	}

	public static WebElement getById(String id) {
		return TestContext.getInstance().getDriver().findElement(By.id(id));
	}

	public static List<WebElement> findById(String id) {
		return TestContext.getInstance().getDriver().findElements(By.id(id));
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

	public static WebElement getLink(String linkText) {
		List<WebElement> elements = TestContext.getInstance().getDriver().findElements(By.linkText(linkText));
		if (elements.size() > 1) {
			throw new AssertionError("more than one link with name " + linkText);
		}
		if (elements.isEmpty()) {
			return null;
		}
		return elements.get(0);
	}
	
	public static WebElement getLinkByDebugId(String debugId) {
		List<WebElement> elements = findByDebugId(debugId);
		if (elements.size() > 1) {
			throw new AssertionError("more than one link with id " + debugId);
		}
		if (elements.isEmpty()) {
			return null;
		}
		return elements.get(0);
	}

	public static WebElement getByClass(String className) {
		return TestContext.getInstance().getDriver().findElement(By.className(className));
	}
	
	public static WebElement getByCss(String css) {
		return TestContext.getInstance().getDriver().findElement(By.cssSelector(css));
	}
	
	public static List<WebElement> findByCss(String css) {
		return TestContext.getInstance().getDriver().findElements(By.cssSelector(css));
	}

	public static WebElement getParent(WebElement element) {
		return element.findElement(By.xpath(".."));
	}

	public static List<WebElement> filterByText(List<WebElement> labels, String text) {
		List<WebElement> filtered = new ArrayList<>();
		for (WebElement label : labels) {
			if (label.getText().equals(text)) {
				filtered.add(label);
			}
		}
		return filtered;
	}
}
