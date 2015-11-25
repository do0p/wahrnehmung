package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.script.ScriptScopes;

class Utils {

	static class StateParser {

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
			String decodedState = new String(Base64.decodeBase64(state));
			return decodedState.split(":");
		}

		private String encodeState(String childKey, boolean overwrite, int year) {
			String state = childKey + ":" + (overwrite ? "y" : "n") + ":"
					+ year;
			return Base64.encodeBase64URLSafeString(state.getBytes());
		}
	}

	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	static final HttpTransport HTTP_TRANSPORT;
	private static final DataStoreFactory DATA_STORE_FACTORY = AppEngineDataStoreFactory
			.getDefaultInstance();

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			throw new IllegalStateException();
		}
	}

	static String getRedirectUri(HttpServletRequest req) {
		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
		url.setRawPath("/oauth2docscallback");
		return url.build();
	}

	static GoogleAuthorizationCodeFlow newFlow() {
		try {
			return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
					JSON_FACTORY, getClientCredential(),
					Arrays.asList("https://www.googleapis.com/auth/documents", ScriptScopes.DRIVE))
					.setDataStoreFactory(DATA_STORE_FACTORY)
					.setAccessType("offline").build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static GoogleClientSecrets getClientCredential() {
		try {
			return GoogleClientSecrets.load(
					JSON_FACTORY,
					new InputStreamReader(Utils.class
							.getResourceAsStream("/client_secret.json"),
							"UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException("could not read secrects", e);
		}
	}

}