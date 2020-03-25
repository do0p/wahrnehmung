package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class InteractionServlet extends AbstractInteractionServlet {

	private static final String FROM_PARAM = "from";
	private static final String TO_PARAM = "to";
	private static final long serialVersionUID = -7318489147891141902L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!checkAccess(req, resp)) {
			return;
		}

		String childKey = req.getParameter(KEY_PARAM);
		Date fromDate = getDateValue(req, FROM_PARAM);
		Date toDate = getDateValue(req, TO_PARAM);
		fromDate = DateUtils.getStartOfDay(fromDate);
		toDate = DateUtils.getEndOfDay(toDate);

		JsonGenerator generator = new JsonFactory().createGenerator(resp.getOutputStream());
		generator.writeStartArray();

		if (childKey == null) {

			Map<String, Map<String, Integer>> allInteractions = getInteractionDao().getAllInteractions(fromDate, toDate);
			for (Entry<String, Map<String, Integer>> interaction : allInteractions.entrySet()) {
				generator.writeStartObject();
				generator.writeFieldName(interaction.getKey());
				generator.writeStartArray();
				writeAllEntries(generator, interaction.getValue());
				generator.writeEndArray();
				generator.writeEndObject();
			}

		} else {
			Map<String, Integer> interactions = getInteractionDao().getInteractions(childKey, fromDate, toDate);

			writeAllEntries(generator, interactions);
		}
		generator.writeEndArray();
		generator.close();
	}

	private void writeAllEntries(JsonGenerator generator, Map<String, Integer> interactions) throws IOException {
		for (Entry<String, Integer> interaction : interactions.entrySet()) {
			writeEntry(generator, interaction);
		}
	}

	private void writeEntry(JsonGenerator generator, Entry<String, Integer> interaction) throws IOException {
		generator.writeStartObject();
		generator.writeFieldName(interaction.getKey());
		generator.writeNumber(interaction.getValue());
		generator.writeEndObject();
	}

}
