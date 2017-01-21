package at.brandl.lws.notice.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.appengine.repackaged.com.fasterxml.jackson.core.JsonGenerator.Feature;

import at.brandl.lws.notice.model.GwtInteraction;

public class ViJsTest {

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
	
	@Test
	public void convertInteractions() throws JsonParseException, IOException {
		Map<String, List<GwtInteraction>> allInteractions = parseResponse(readFromFile("interactions_json.txt"));
		Assert.assertFalse(allInteractions.isEmpty());
		
		Map<String, Node> nodes = getNodes(allInteractions.keySet());
		List<Edge> edges = getEdges(allInteractions, nodes);

		printNodes(nodes.values());
		System.out.println("");
		printEdges(edges);

	}

	private Map<String, Node> getNodes(Collection<String> childKeys) {
		Map<String, Node> nodes = new HashMap<>();
		int i = 1;
		for(String childKey : childKeys) {
			nodes.put(childKey, new Node(i, Integer.toString(i)));
			i++;
		}
		return nodes;
	}

	private void printNodes(Collection<Node> nodes) throws IOException {
		JsonGenerator generator = new JsonFactory().createGenerator(System.out);
		generator.writeStartArray();

		for(Node node : nodes) {
			generator.writeStartObject();
			generator.writeFieldName("id");
			generator.writeNumber(node.id);
			generator.writeFieldName("label");
			generator.writeString(node.label);
			generator.writeEndObject();
		}


		generator.writeEndArray();
		generator.flush();
//		generator.close();		
	}

	private void printEdges(Collection<Edge> edges) throws IOException {
		JsonGenerator generator = new JsonFactory().createGenerator(System.out);
		generator.writeStartArray();

		for(Edge edge : edges) {

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
		generator.close();

	}

	private List<Edge> getEdges(Map<String, List<GwtInteraction>> allInteractions, Map<String, Node> nodes) {
		
		List<Edge> edges = new ArrayList<>();
		Set<String> done = new HashSet<>();
		
		for(Entry<String, List<GwtInteraction>> entry : allInteractions.entrySet()){
			String child = entry.getKey();
			done.add(child);
			for(GwtInteraction interaction : entry.getValue()) {
				String childOther = interaction.getChildKey();
				if(done.contains(childOther)) {
					continue;
				}
				edges.add(new Edge(nodes.get(child).id, nodes.get(childOther).id, interaction.getCount()));
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

	private InputStream readFromFile(String fileName) {
		return this.getClass().getClassLoader().getResourceAsStream(fileName);

	}
}
