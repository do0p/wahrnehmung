package at.brandl.lws.notice.service.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.service.dao.NoticeArchiveDsDao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class MoveGroupsServlet extends HttpServlet {

	private static final long serialVersionUID = -7318489147891141902L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Key parentNoticeKey = KeyFactory.stringToKey(req.getParameter(MoveAllServlet.KEY_PARAM));
		new NoticeArchiveDsDao().moveGroupsToArchive(parentNoticeKey);
	}



}
