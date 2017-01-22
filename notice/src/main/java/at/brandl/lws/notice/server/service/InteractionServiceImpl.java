package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtInteraction;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.shared.Config;
import at.brandl.lws.notice.shared.service.InteractionService;

public class InteractionServiceImpl extends RemoteServiceServlet implements InteractionService {

	private static final int READ_TIMEOUT = 10000;
	private static final int CONNECTION_TIMEOUT = 5000;
	private static final long serialVersionUID = -8046489780384721894L;
	private ChildDsDao childDao = DaoRegistry.get(ChildDsDao.class);

	@Override
	public List<GwtInteraction> getInteractions(String childKey, Date fromDate, Date toDate) throws IOException {

		URLConnection con = createUrlConnection(childKey, fromDate, toDate);
		return parseResponse(con);
	}

	private List<GwtInteraction> parseResponse(URLConnection con) throws IOException, JsonParseException {
		List<GwtInteraction> interactions = new ArrayList<>();
		JsonParser parser = new JsonFactory().createParser(con.getInputStream());
		parser.nextToken();
		while (JsonToken.START_OBJECT.equals(parser.nextToken())) {
			GwtInteraction interaction = new GwtInteraction();
			parser.nextToken(); // childKey as fieldname
			String childKey = parser.getText();
			interaction.setChildKey(childKey);
			interaction.setChildName(getChildName(childKey));
			parser.nextToken(); // count as value
			interaction.setCount(parser.getIntValue());
			parser.nextToken(); // end object
			interactions.add(interaction);
		}
		Collections.sort(interactions);
		return interactions;
	}

	private URLConnection createUrlConnection(String childKey, Date fromDate, Date toDate) throws MalformedURLException, IOException {

		// String host = "http://localhost:9090";
		String host = "https://interaction-service-dot-" + Config.getInstance().getApplicationName() + ".appspot.com";
		String dateQuery = buildDateQuery(fromDate, toDate);
		URL url = new URL(host + "/interactions?childKey=" + childKey + dateQuery);
		// System.err.println("opening connection to " + url);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(CONNECTION_TIMEOUT);
		con.setReadTimeout(READ_TIMEOUT);
		if (con instanceof HttpURLConnection) {
			((HttpURLConnection) con).setInstanceFollowRedirects(false);
		}
		return con;
	}

	private String buildDateQuery(Date fromDate, Date toDate) {
		StringBuilder dateQuery = new StringBuilder();
		if(fromDate != null) {
//			System.err.println("from: " + fromDate);
			dateQuery.append("&from=" + fromDate.getTime());
		}
		if(toDate != null) {
//			System.err.println("to: " + toDate);
			dateQuery.append("&to=" + toDate.getTime());
		}
		return dateQuery.toString() ;
	}

	private String getChildName(String childKey) {
		return childDao.getChildName(childKey);
	}
}