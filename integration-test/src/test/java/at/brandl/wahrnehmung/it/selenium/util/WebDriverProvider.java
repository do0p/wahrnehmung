package at.brandl.wahrnehmung.it.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class WebDriverProvider {

	public static enum DriverType {
		CHROME, FIREFOX, IE
	}

	private DriverType type = DriverType.CHROME;
	private boolean managedBySuite;
	private WebDriver driver;

	public WebDriver get() {
		if (driver == null) {
			driver = createDriver();
			driver.get("http://localhost:8080");
		}
		return driver;
	}



	public void setType(DriverType type) {
		if (type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		this.type = type;
	}

	public void returnDriver() {
		if(!managedBySuite) {
			close();
		}
	}

	public boolean isManagedBySuite() {
		return managedBySuite;
	}



	public void setManagedBySuite(boolean managedBySuite) {
		this.managedBySuite = managedBySuite;
	}



	public void close() {
		driver.close();
		driver.quit();
		driver = null;
	}

	private WebDriver createDriver() {
		switch (type) {
		case CHROME:
			return new ChromeDriver();
		case FIREFOX:
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("focusmanager.testmode", true);
			profile.setEnableNativeEvents(true);
			return new FirefoxDriver(profile);
		case IE:
			return new EdgeDriver();
		}
		throw new AssertionError("missing factory for drivertype " + type);
	}
	
}
