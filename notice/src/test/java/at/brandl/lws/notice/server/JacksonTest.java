package at.brandl.lws.notice.server;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;

public class JacksonTest {

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	@Test
	public void mapOrder() throws IOException {
		
		LinkedHashMap<String, Object> innerMap = new LinkedHashMap<>();
		innerMap.put("z", "3");
		innerMap.put("a", "1");
		innerMap.put("m", "2");
		
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("z", "3");
		map.put("a", innerMap);
		map.put("m", "2");
		
		Writer writer = new StringWriter();
		JsonGenerator jsonGenerator = JSON_FACTORY.createJsonGenerator(writer);
		
		jsonGenerator.enablePrettyPrint();
		jsonGenerator.serialize(map);
		jsonGenerator.close();
		
		System.out.println(writer);
	}
}
