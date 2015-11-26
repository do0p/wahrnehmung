package at.brandl.lws.notice.server.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;

public class DocsOauthCallbackService extends
		AbstractAppEngineAuthorizationCodeCallbackServlet {

	private static final long serialVersionUID = 1915436781531154806L;

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException,
			IOException {

		return Utils.newFlow();
	}

	@Override
	protected String getRedirectUri(HttpServletRequest req)
			throws ServletException, IOException {

		return Utils.getRedirectUri(req);
	}

	@Override
	protected void onSuccess(HttpServletRequest req, HttpServletResponse resp,
			Credential credential) throws ServletException, IOException {

		HttpServletRequest storedRequest = retrieveStoredRequest(req);
		new DocServiceImpl().service(storedRequest, resp);

	}

	private HttpServletRequest retrieveStoredRequest(HttpServletRequest req) {

		AuthorizationCodeResponseUrl responseUrl = getAuthResponseUrl(req);
		return Utils.decodeState(responseUrl.getState());
	}

	private AuthorizationCodeResponseUrl getAuthResponseUrl(
			HttpServletRequest req) {

		StringBuffer buf = req.getRequestURL();
		if (req.getQueryString() != null) {
			buf.append('?').append(req.getQueryString());
		}
		return new AuthorizationCodeResponseUrl(buf.toString());
	}

}
