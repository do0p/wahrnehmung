package at.brandl.lws.notice.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {

	private static final long serialVersionUID = 4642424314615927394L;

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
		return getProperty("application.name");
	}

	public String getBucketName() {
		return getApplicationName() + ".appspot.com";
	}
}
