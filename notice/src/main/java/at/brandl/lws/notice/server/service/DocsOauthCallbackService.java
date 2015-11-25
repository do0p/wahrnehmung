package at.brandl.lws.notice.server.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.model.UserGrantRequiredException;
import at.brandl.lws.notice.server.service.Utils.StateParser;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.gwt.user.server.rpc.RPCServletUtils;

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
	
	    StringBuffer buf = req.getRequestURL();
	    if (req.getQueryString() != null) {
	      buf.append('?').append(req.getQueryString());
	    }
	    AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(buf.toString());
		StateParser stateParser = new StateParser(responseUrl.getState());
		
		try {
			String docUrl = new DocServiceImpl().printDocumentation(stateParser.getChildKey(), stateParser.getOverwrite(), stateParser.getYear());
			writeResponse(req, resp, docUrl);
		} catch (UserGrantRequiredException e) {
			e.printStackTrace();
		}
	}
	
	  private void writeResponse(HttpServletRequest request,
		      HttpServletResponse response, String responsePayload) throws IOException {
		    boolean gzipEncode = RPCServletUtils.acceptsGzipEncoding(request)
		        && shouldCompressResponse(request, response, responsePayload);

		    RPCServletUtils.writeResponse(getServletContext(), response,
		        responsePayload, gzipEncode);

	  }
	  private boolean shouldCompressResponse(HttpServletRequest request,
		      HttpServletResponse response, String responsePayload) {
		    return RPCServletUtils.exceedsUncompressedContentLengthLimit(responsePayload);
		  }
}
