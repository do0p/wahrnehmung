package at.brandl.lws.notice.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.appengine.api.utils.SystemProperty;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtInteraction;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.server.service.AuthorizationServiceImpl;
import at.brandl.lws.notice.shared.Config;

public class ViJsServlet extends HttpServlet {

	private static final long serialVersionUID = -310249949623718456L;
	private static final int READ_TIMEOUT = 60000;
	private static final int CONNECTION_TIMEOUT = 5000;
	// private static final String FROM_PARAM = "from";
	// private static final String TO_PARAM = "to";

	private static final class Node {
		private final int id;
		private final String label;

		private Node(int id, String label) {
			this.id = id;
			this.label = label;
		}
	}

	private static final class Edge {
		private final int from;
		private final int to;
		private final int count;

		private Edge(int from, int to, int count) {
			this.from = from;
			this.to = to;
			this.count = count;
		}
	}

	private final AuthorizationServiceImpl authService = new AuthorizationServiceImpl();
	private final ChildDsDao childDsDao = DaoRegistry.get(ChildDsDao.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!authService.currentUserIsTeacher()) {
			resp.sendError(403);
			return;
		}

		// Date fromDate = getDateValue(req, FROM_PARAM);
		// Date toDate = getDateValue(req, TO_PARAM);

		Map<String, List<GwtInteraction>> allInteractions = parseResponse(createUrlConnection().getInputStream());

		Map<String, Node> nodes = getNodes(allInteractions.keySet());
		List<Edge> edges = getEdges(allInteractions, nodes);

		JsonGenerator generator = new JsonFactory().createGenerator(resp.getOutputStream());
		generator.writeStartObject();
		generator.writeFieldName("nodes");
		printNodes(nodes.values(), generator);
		generator.writeFieldName("edges");
		printEdges(edges, generator);
		generator.writeEndObject();
		generator.close();
	}

	private Map<String, Node> getNodes(Collection<String> childKeys) {
		Map<String, Node> nodes = new HashMap<>();
		int i = 1;
		for (String childKey : childKeys) {
			try {
				nodes.put(childKey, new Node(i, childDsDao.getChildName(childKey)));
				i++;
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
			}
		}
		return nodes;
	}

	private void printNodes(Collection<Node> nodes, JsonGenerator generator) throws IOException {

		generator.writeStartArray();
		for (Node node : nodes) {
			generator.writeStartObject();
			generator.writeFieldName("id");
			generator.writeNumber(node.id);
			generator.writeFieldName("label");
			generator.writeString(node.label);
			generator.writeEndObject();
		}
		generator.writeEndArray();
	}

	private void printEdges(Collection<Edge> edges, JsonGenerator generator) throws IOException {

		generator.writeStartArray();
		for (Edge edge : edges) {
			generator.writeStartObject();
			generator.writeFieldName("from");
			generator.writeNumber(edge.from);
			generator.writeFieldName("to");
			generator.writeNumber(edge.to);
			generator.writeFieldName("count");
			generator.writeNumber(edge.count);
			generator.writeEndObject();
		}
		generator.writeEndArray();
	}

	private List<Edge> getEdges(Map<String, List<GwtInteraction>> allInteractions, Map<String, Node> nodes) {

		List<Edge> edges = new ArrayList<>();
		Set<String> done = new HashSet<>();

		for (Entry<String, List<GwtInteraction>> entry : allInteractions.entrySet()) {
			String child = entry.getKey();
			done.add(child);
			Node childNode = nodes.get(child);
			if(childNode == null) {
				continue;
			}
			for (GwtInteraction interaction : entry.getValue()) {
				String childOther = interaction.getChildKey();
				if (done.contains(childOther)) {
					continue;
				}
				Node childOtherNode = nodes.get(childOther);
				if(childOtherNode == null) {
					continue;
				}
				edges.add(new Edge(childNode.id, childOtherNode.id, interaction.getCount()));
			}
		}

		return edges;
	}

	private Map<String, List<GwtInteraction>> parseResponse(InputStream inputStream)
			throws IOException, JsonParseException {
		Map<String, List<GwtInteraction>> allInteractions = new HashMap<>();
		JsonParser parser = new JsonFactory().createParser(inputStream);
		parser.nextToken(); // start array
		while (JsonToken.START_OBJECT.equals(parser.nextToken())) {
			parser.nextToken();
			String childKey = parser.getText();
			parser.nextToken(); // start array
			List<GwtInteraction> interactions = new ArrayList<>();
			while (JsonToken.START_OBJECT.equals(parser.nextToken())) {
				GwtInteraction interaction = new GwtInteraction();
				parser.nextToken(); // childKey as fieldname
				interaction.setChildKey(parser.getText());
				parser.nextToken(); // count as value
				interaction.setCount(parser.getIntValue());
				parser.nextToken(); // end object
				interactions.add(interaction);
			}
			Collections.sort(interactions);
			allInteractions.put(childKey, interactions);
			parser.nextToken(); // end object
		}
		return allInteractions;
	}

	private URLConnection createUrlConnection() throws MalformedURLException, IOException {

		// String host = "http://localhost:9090";
		String serviceUrl = Config.getInstance().getInteractionServiceUrl();
		// String dateQuery = buildDateQuery(fromDate, toDate);
		URL url = new URL(serviceUrl + "/interactions");// + "?childKey=" +
														// childKey +
														// dateQuery);
		// System.err.println("opening connection to " + url);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(CONNECTION_TIMEOUT);
		con.setReadTimeout(READ_TIMEOUT);
		con.setRequestProperty("X-Appengine-Inbound-Appid", SystemProperty.applicationId.get());
		if (con instanceof HttpURLConnection) {
			((HttpURLConnection) con).setInstanceFollowRedirects(false);
		}
		return con;
	}

	// private Date getDateValue(HttpServletRequest req, String paramName) {
	// String longValue = req.getParameter(paramName);
	// if (longValue != null) {
	// try {
	// return new Date(Long.parseLong(longValue));
	// } catch (NumberFormatException e) {
	// throw new IllegalArgumentException("not a long value");
	// }
	// }
	// return null;
	// }
}
