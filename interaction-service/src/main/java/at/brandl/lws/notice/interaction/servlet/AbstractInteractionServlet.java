package at.brandl.lws.notice.interaction.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.interaction.dao.InteractionDsDao;

public abstract class AbstractInteractionServlet extends HttpServlet {

	private static final long serialVersionUID = 890615481550277098L;
	protected static final String KEY_PARAM = "childKey";
	
	private final String appId = SystemProperty.applicationId.get();
	private final InteractionDsDao interactionDao = DaoRegistry.get(InteractionDsDao.class);

	protected final InteractionDsDao getInteractionDao() {
		return interactionDao;
	}

	protected final Date getDateValue(HttpServletRequest req, String paramName) {
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

	protected final boolean checkAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String inboundAppId = req.getHeader("X-Appengine-Inbound-Appid");
		if (appId.equals(inboundAppId)) {
			return true;
		}
		System.err.println(inboundAppId + " not allowed in " + appId);
		resp.sendError(403);
		return false;
	}

}