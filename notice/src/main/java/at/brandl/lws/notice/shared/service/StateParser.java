package at.brandl.lws.notice.shared.service;

import com.google.common.io.BaseEncoding;
import com.google.gwt.core.shared.GWT;


public class StateParser {

	private String childKey;
	private boolean overwrite;
	private int year;
	private String state;

	public StateParser(String childKey, boolean overwrite, int year) {
		this.childKey = childKey;
		this.overwrite = overwrite;
		this.year = year;
		this.state = encodeState(childKey, overwrite, year);
	}

	public StateParser(String state) {
		String[] decodedState = decodeState(state);
		this.childKey = decodedState[0];
		this.overwrite = decodedState[1] == "y";
		this.year = Integer.parseInt(decodedState[2]);
		this.state = state;
		GWT.log("decoded " + childKey);
	}

	public String getChildKey() {
		return childKey;
	}

	public boolean getOverwrite() {
		return overwrite;
	}

	public int getYear() {
		return year;
	}

	public String getState() {
		return state;
	}

	private String[] decodeState(String state) {
		
		String decodedState = new String(BaseEncoding.base64Url().decode(state));
		return decodedState.split(":");
	}

	private String encodeState(String childKey, boolean overwrite, int year) {
		
		String state = childKey + ":" + (overwrite ? "y" : "n") + ":" + year;
		return BaseEncoding.base64Url().encode(state.getBytes());
	}
}