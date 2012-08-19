package at.lws.wnm.server.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.lws.wnm.server.dao.AuthorizationDao;
import at.lws.wnm.server.dao.DaoRegistry;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AuthorizationFilter implements Filter {

	private static final Logger LOGGER = Logger
			.getLogger(AuthorizationFilter.class.getName());
	private AuthorizationDao authorizationDao;



	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		final UserService userService = UserServiceFactory.getUserService();
		final User currentUser = userService.getCurrentUser();
		if (currentUser == null) {
			redirect(response,
					userService.createLoginURL(((HttpServletRequest) request)
							.getRequestURI()));
			return;
		}

		if (authorizationDao.isAuthorized(currentUser)) {
			chain.doFilter(request, response);
			return;
		}

		LOGGER.warning("unknown user " + currentUser.getEmail()
				+ " tried to log in");
		redirect(response,
				userService.createLogoutURL(((HttpServletRequest) request)
						.getRequestURI()));

	}

	private void redirect(ServletResponse response, String redirectUrl)
			throws IOException {
		if (response instanceof HttpServletResponse) {
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			final String urlWithSessionID = httpResponse
					.encodeRedirectURL(redirectUrl);
			httpResponse.sendRedirect(urlWithSessionID);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		authorizationDao = DaoRegistry.get(AuthorizationDao.class);
	}

}
