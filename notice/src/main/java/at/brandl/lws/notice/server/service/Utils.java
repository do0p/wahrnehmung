package at.brandl.lws.notice.server.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.io.Writer;
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
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.DataStoreFactory;

class Utils {

	private static final String OFFLINE = "offline";
	private static final String DOCUMENTS_SCOPE = "https://www.googleapis.com/auth/documents";
	private static final String CLIENT_SECRET = "/client_secret.json";
	private static final String ENCODING = "UTF-8";
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
					Arrays.asList(DOCUMENTS_SCOPE))
					.setDataStoreFactory(DATA_STORE_FACTORY)
					.setAccessType(OFFLINE).build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static GoogleClientSecrets getClientCredential() {

		try {
			InputStream in = Utils.class.getResourceAsStream(CLIENT_SECRET);
			InputStreamReader reader = new InputStreamReader(in, ENCODING);
			return GoogleClientSecrets.load(JSON_FACTORY, reader);
		} catch (IOException e) {
			throw new RuntimeException("could not read secrects", e);
		}
	}

	static SerializableRequest decodeState(String state) {

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(
					Base64.decodeBase64(state));
			return (SerializableRequest) new ObjectInputStream(in).readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String encodeState(HttpServletRequest request) {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(request);
			return Base64.encodeBase64URLSafeString(out.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String createJsonString(Object object) {
		try {
			Writer writer = new StringWriter();
			JsonGenerator jsonGenerator = JSON_FACTORY
					.createJsonGenerator(writer);

			jsonGenerator.enablePrettyPrint();
			jsonGenerator.serialize(object);
			jsonGenerator.close();

			String debugString = writer.toString();
			return debugString;
		} catch (IOException e) {
			return object.toString();
		}
	}

}