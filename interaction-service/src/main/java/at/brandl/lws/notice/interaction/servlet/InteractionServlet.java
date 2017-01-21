package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.appengine.api.utils.SystemProperty;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.interaction.dao.InteractionDsDao;

public class InteractionServlet extends HttpServlet {

	private static final String KEY_PARAM = "childKey";

	private static final String FROM_PARAM = "from";
	private static final String TO_PARAM = "to";
	private static final long serialVersionUID = -7318489147891141902L;

	private InteractionDsDao interactionDao = DaoRegistry.get(InteractionDsDao.class);
	private String appId = SystemProperty.applicationId.get();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String inboundAppId = req.getHeader("X-Appengine-Inbound-Appid");
		if (!appId.equals(inboundAppId)) {
			System.err.println(inboundAppId + " not allowed in " + appId);
			resp.sendError(403);
			return;
		}

		String childKey = req.getParameter(KEY_PARAM);
		Date fromDate = getDateValue(req, FROM_PARAM);
		Date toDate = getDateValue(req, TO_PARAM);

		JsonGenerator generator = new JsonFactory().createGenerator(resp.getOutputStream());
		generator.writeStartArray();

		if (childKey == null) {

			Map<String, Map<String, Integer>> allInteractions = interactionDao.getAllInteractions(fromDate, toDate);
			for (Entry<String, Map<String, Integer>> interaction : allInteractions.entrySet()) {
				generator.writeStartObject();
				generator.writeFieldName(interaction.getKey());
				generator.writeStartArray();
				writeAllEntries(generator, interaction.getValue());
				generator.writeEndArray();
				generator.writeEndObject();
			}

		} else {
			Map<String, Integer> interactions = interactionDao.getInteractions(childKey, fromDate, toDate);

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

	private Date getDateValue(HttpServletRequest req, String paramName) {
		String longValue = req.getParameter(paramName);
		if (longValue != null) {
			try {
				return new Date(Long.parseLong(longValue));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("not a long value");
			}
		}
		return null;
	}

}
