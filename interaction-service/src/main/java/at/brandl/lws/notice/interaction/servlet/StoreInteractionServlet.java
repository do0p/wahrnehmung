package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.interaction.dao.InteractionDsDao;

public class StoreInteractionServlet extends HttpServlet {

	private static final long serialVersionUID = 5495998583956606624L;

	private static final String KEY_PARAM = "childKey";

	private static final String DATE_PARAM = "date";

	private InteractionDsDao interactionDao = new InteractionDsDao();

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String[] childKeys = req.getParameterValues(KEY_PARAM);
		if (childKeys == null) {
			throw new IllegalArgumentException(KEY_PARAM + " is missing");
		}
		if (childKeys.length != 2) {
			throw new IllegalArgumentException("wrong number of " + KEY_PARAM + " params: " + childKeys.length);
		}

		Date date = getDateValue(req, DATE_PARAM);

		interactionDao.incrementInteraction(childKeys[0], childKeys[1], date, 1);
	}


	private Date getDateValue(HttpServletRequest req, String paramName) {
		String dateString = req.getParameter(paramName);
		if (dateString == null) {
				throw new IllegalArgumentException(paramName + " is missing.");
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			throw new IllegalArgumentException("cannot parse dates from " + dateString, e);
		}
	}

}
