package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.drive.DriveScopes;

class Utils {
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
		url.setRawPath("/oauth2callback");
		return url.build();
	}

	static GoogleAuthorizationCodeFlow newFlow()  {
		try {
			return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
					JSON_FACTORY, getClientCredential(), DriveScopes.all())
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