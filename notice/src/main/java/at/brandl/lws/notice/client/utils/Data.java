package at.brandl.lws.notice.client.utils;

public class Data {

	private static final char SEPARATOR = '|';
	private final String key;
	private final String value;
	
	public Data(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return key + SEPARATOR + value;
	}
	
	public static Data valueOf(String data) {
		int pos = data.indexOf(SEPARATOR);
		if(pos < 0) {
			throw new IllegalArgumentException("no valid data string");
		}
		String key = data.substring(0, pos);
		String value = data.substring(pos + 1);
		return new Data(key, value);
	}
}
