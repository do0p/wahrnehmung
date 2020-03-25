package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.shared.util.Constants;

public class ArchiveInteractionServlet extends AbstractInteractionServlet {

	private static final long serialVersionUID = 5495998583956606624L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String queueName = req.getHeader("X-AppEngine-QueueName");
		if (!Constants.INTERACTION_QUEUE_NAME.equals(queueName)) {
			System.err.println(queueName + " not allowed.");
			resp.sendError(403);
			return;
		}

		String childKey = req.getParameter(KEY_PARAM);
		if (childKey == null) {
			throw new IllegalArgumentException(KEY_PARAM + " is missing");
		}

		getInteractionDao().archive(childKey);
	}

}
