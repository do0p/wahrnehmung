package at.brandl.wahrnehmung.it.selenium.section;

import java.util.List;

import org.openqa.selenium.WebElement;

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

	public void deleteSection(String section) {
		WebElement deleteLink = getDeleteLink(section);
		deleteLink.click();
	}

	public WebElement getDeleteLink(String section) {
		String elementId = getTreeId(section);
		if (elementId == null) {
			throw new IllegalArgumentException("no such section " + section);
		}
		List<WebElement> deleteLinks = Utils.filterByText(Utils.findByCss("#"+elementId + " .gwt-Anchor"), "entfernen");
		if(deleteLinks.size() != 1) {
			throw new IllegalStateException("no deletelink found for " + section);
		}
		return deleteLinks.get(0);
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

}
