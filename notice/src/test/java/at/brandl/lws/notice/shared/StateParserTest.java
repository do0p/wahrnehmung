package at.brandl.lws.notice.shared;

import org.junit.Assert;
import org.junit.Test;

import at.brandl.lws.notice.shared.service.StateParser;

public class StateParserTest {

	private static final String STATE = "YWhKemZuZGhhSEp1WldodGRXNW5MWFJsYzNSeUZBc1NCME5vYVd4a1JITVlnSUNBZ0tfbG5Bb006MjAxNQ==";
	private static final String CHILD_KEY = "ahJzfndhaHJuZWhtdW5nLXRlc3RyFAsSB0NoaWxkRHMYgICAgK_lnAoM";
	private static final int YEAR = 2015;
	
	@Test
	public void decode() {
		StateParser parser = new StateParser(STATE);
		Assert.assertEquals(CHILD_KEY, parser.getChildKey());
		Assert.assertEquals(YEAR, parser.getYear());
	}
	
	@Test
	public void encode() {
		StateParser parser = new StateParser(CHILD_KEY, YEAR);
		Assert.assertEquals(STATE, parser.getState());
	}
}

