package at.brandl.wahrnehmung.it.selenium.section;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.By.ByXPath;

import at.brandl.wahrnehmung.it.selenium.util.Configurations;
import at.brandl.wahrnehmung.it.selenium.util.Constants;
import at.brandl.wahrnehmung.it.selenium.util.Page;
import at.brandl.wahrnehmung.it.selenium.util.TestContext;
import at.brandl.wahrnehmung.it.selenium.util.User;
import at.brandl.wahrnehmung.it.selenium.util.Utils;

public class SectionAdminPage implements Page {

	private static final String ID_PREFIX = "gwt-uid-";
	public static final String PAGE_NAME = "SectionAdmin";

	@Override
	public void goTo() {
		TestContext context = TestContext.getInstance();
		context.login(this);
		context.getNavigation().goTo(Constants.CONFIG_LINK);
		Configurations.navigateTo(this);
	}

	public void enterSection(String section) {
		Utils.clearAndSendKeys(getSectionField(), section);
	}

	public void enterChildSection(String section, String childSection) {
		Utils.clearAndSendKeys(getChildSectionField(section), childSection);
	}

	public void changeSection(String section, String changedSection) {
		String elementId = clickSectionLink(section, "ändern");

		Utils.clearAndSendKeys(getSectionField(elementId), changedSection);
	}

	public void archiveSection(String section) {
		clickSectionLink(section, "archivieren");
	}

	public void activateSection(String section) {
		clickSectionLink(section, "aktivieren");
	}

	public void deleteSection(String section) {
		clickSectionLink(section, "entfernen");
	}

	public void moveDown(String section) {
		clickSectionLink(section, "↓");
	}

	public void moveUp(String section) {
		clickSectionLink(section, "↑");
	}

	public void clickPlusMinusSign(String section) {
		WebElement element = getRootElement(section);
		element.findElement(ByXPath.xpath("table/tbody/tr/td/img")).click();
	}

	public WebElement getChildSectionField(String section) {
		WebElement element = getRootElement(section);
		return element.findElement(ByXPath.xpath("div/div/div/input"));
	}

	public void clickCreate(String section) {
		WebElement element = getRootElement(section);
		element.findElement(ByXPath.xpath("div/div/div/a")).click();
	}

	public boolean sectionLinkExists(String section, String linkText) {
		String elementId = getTreeId(section);
		if (elementId == null) {
			throw new IllegalArgumentException("no such section " + section);
		}
		return !Utils.filterByText(Utils.findByCss("#" + elementId + " .gwt-Anchor"), linkText).isEmpty();
	}

	private String clickSectionLink(String section, String linkText) {
		String elementId = getTreeId(section);
		if (elementId == null) {
			throw new IllegalArgumentException("no such section " + section);
		}
		WebElement link = getSectionLink(elementId, linkText);
		link.click();
		return elementId;
	}

	private WebElement getSectionLink(String elementId, String linkText) {

		List<WebElement> sectionLinks = Utils.filterByText(Utils.findByCss("#" + elementId + " .gwt-Anchor"), linkText);
		if (sectionLinks.size() != 1) {
			throw new IllegalStateException("no deletelink found for " + elementId);
		}
		return sectionLinks.get(0);
	}

	public String getTreeId(String section) {
		List<WebElement> labels = Utils.filterByText(Utils.findByCss(".gwt-TreeItem .gwt-InlineLabel"), section);

		for (WebElement label : labels) {

			WebElement parent = Utils.getParent(label);
			while (parent != null) {
				String idAttribute = parent.getAttribute("id");
				if (idAttribute != null && idAttribute.startsWith(ID_PREFIX)) {
					return idAttribute;
				}
				parent = Utils.getParent(parent);
			}
		}
		return null;
	}

	public void clickSave() {
		getSaveButton().click();
	}

	public void clickCancel() {
		getCancelButton().click();
	}

	public WebElement getSectionField() {
		return Utils.getByCss(".gwt-TreeItem>.gwt-TextBox");
	}

	public WebElement getSectionField(String elementId) {
		return Utils.getByCss("#" + elementId + " > .gwt-TextBox");
	}

	public WebElement getSaveButton() {
		return Utils.getByDebugId("save" + getPageName());
	}

	public WebElement getCancelButton() {
		return Utils.getByDebugId("cancel" + getPageName());
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public boolean isAllowed(User user) {
		return user.isSectionAdmin();
	}

	@Override
	public User getDefaultUser() {
		return Constants.SECTION_ADMIN;
	}

	public int getIntTreeId(String section) {
		String elementId = getTreeId(section);
		if (elementId == null) {
			throw new IllegalArgumentException("no such section " + section);
		}
		return Integer.parseInt(elementId.substring(ID_PREFIX.length()));
	}

	private WebElement getRootElement(String section) {
		String elementId = getTreeId(section);
		if (elementId == null) {
			throw new IllegalArgumentException("no such section " + section);
		}
		WebElement element = Utils.getByCss("#" + elementId);
		return element.findElement(ByXPath.xpath("../../../../.."));
	}

}
