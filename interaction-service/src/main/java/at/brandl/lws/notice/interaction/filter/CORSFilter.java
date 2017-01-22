package at.brandl.lws.notice.interaction.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CORSFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.err.println("in init of " + getClass().getName());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.err.println("in filter of " + getClass().getName());
		if(response instanceof HttpServletResponse) {
			System.err.println("adding cors");
			((HttpServletResponse) response).addHeader("Access-Control-Allow-Credentials", "true");
			((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
