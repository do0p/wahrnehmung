package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;

import at.brandl.lws.notice.interaction.dao.InteractionDsDao;

public class InteractionServlet extends HttpServlet {

	private static final String KEY_PARAM = "childKey";

	private static final String FROM_PARAM = "from";
	private static final String TO_PARAM = "to";
	private static final long serialVersionUID = -7318489147891141902L;

	private InteractionDsDao interactionDao = new InteractionDsDao();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String childKey = req.getParameter(KEY_PARAM);
		if (childKey == null) {
			throw new IllegalArgumentException("childKey is missing");
		}

		Date fromDate = getDateValue(req, FROM_PARAM);
		Date toDate = getDateValue(req, TO_PARAM);

		Map<String, Integer> interactions = interactionDao.getInteractions(childKey, fromDate, toDate);

		
		JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(resp.getOutputStream(), Charset.forName("UTF-8"));
		generator.serialize(interactions);
		resp.flushBuffer();
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
