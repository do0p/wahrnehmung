package at.brandl.lws.notice.shared.service;

import com.google.common.io.BaseEncoding;
import com.google.gwt.core.shared.GWT;


public class StateParser {

	private static final String SEPARATOR = ":";
	private String childKey;
	private int year;
	private String state;

	public StateParser(String childKey, int year) {
		this.childKey = childKey;
		this.year = year;
		this.state = encodeState(childKey, year);
	}

	public StateParser(String state) {
		String[] decodedState = decodeState(state);
		this.childKey = decodedState[0];
		this.year = Integer.parseInt(decodedState[1]);
		this.state = state;
		GWT.log("decoded " + childKey);
	}

	public String getChildKey() {
		return childKey;
	}

	public int getYear() {
		return year;
	}

	public String getState() {
		return state;
	}

	private String[] decodeState(String state) {
		
		String decodedState = new String(BaseEncoding.base64Url().decode(state));
		return decodedState.split(SEPARATOR);
	}

	private String encodeState(String childKey, int year) {
		
		String state = childKey + SEPARATOR + year;
		return BaseEncoding.base64Url().encode(state.getBytes());
	}
}