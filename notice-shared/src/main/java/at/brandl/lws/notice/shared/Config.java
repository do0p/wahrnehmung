package at.brandl.lws.notice.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

public class Config extends Properties {

	private static final long serialVersionUID = 4642424314615927394L;
	private static final String DOT = "-dot-";

	private static class ConfigProvider {

		private static final String PROPERTIES_FILE_NAME = "config.properties";
		private final Config config;

		private ConfigProvider() {
			config = new Config();
			InputStream inputStream = getClass().getClassLoader()
					.getResourceAsStream(PROPERTIES_FILE_NAME);
			try {
				config.load(inputStream);
			} catch (IOException e) {
				throw new RuntimeException("could not read properties from "
						+ PROPERTIES_FILE_NAME, e);
			}
		}

		private Config getConfig() {
			return config;
		}

	}

	public static Config getInstance() {
		return new ConfigProvider().getConfig();
	}

	public String getDocumentationGroupName() {
		return getProperty("documentation.group.name");
	}

	public String getApplicationName() {
		return SystemProperty.applicationId.get();
	}

	public String getInteractionServiceUrl() {
		boolean localDev = SystemProperty.environment.value().equals(Environment.Value.Development);
		String interactionServiceId = "interaction-service";
		if(localDev) {
			return "http://" + ModulesServiceFactory.getModulesService().getVersionHostname(interactionServiceId ,null);
		} else {
			return "https://" + interactionServiceId + DOT + getApplicationHostname();
		}
	}
	
	public String getBucketName() {
		return getApplicationHostname();
	}

	private String getApplicationHostname() {
		return getApplicationName() + ".appspot.com";
	}
}
