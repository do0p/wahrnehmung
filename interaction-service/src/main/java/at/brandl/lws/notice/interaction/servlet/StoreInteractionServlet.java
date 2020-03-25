package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.shared.util.Constants;

public class StoreInteractionServlet extends AbstractInteractionServlet {

	private static final long serialVersionUID = 5495998583956606624L;

	private static final String DATE_PARAM = "date";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String queueName = req.getHeader("X-AppEngine-QueueName");
		if (!Constants.INTERACTION_QUEUE_NAME.equals(queueName)) {
			System.err.println(queueName + " not allowed.");
			resp.sendError(403);
			return;
		}

		String[] childKeys = req.getParameterValues(KEY_PARAM);
		if (childKeys == null) {
			throw new IllegalArgumentException(KEY_PARAM + " is missing");
		}
		if (childKeys.length != 2) {
			throw new IllegalArgumentException("wrong number of " + KEY_PARAM + " params: " + childKeys.length);
		}

		Date date = getDateValue(req, DATE_PARAM);
		if(date == null) {
				throw new IllegalArgumentException(DATE_PARAM + " is missing.");
		}
		getInteractionDao().incrementInteraction(childKeys[0], childKeys[1], DateUtils.getStartOfDay(date), 1);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String queueName = req.getHeader("X-AppEngine-QueueName");
		if (!Constants.INTERACTION_QUEUE_NAME.equals(queueName)) {
			System.err.println(queueName + " not allowed.");
			resp.sendError(403);
			return;
		}

		String childKey = req.getParameter(KEY_PARAM);
		getInteractionDao().deleteAllInteractions(childKey);
	}


}
