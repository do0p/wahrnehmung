package at.brandl.lws.notice.server.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class SerializableRequest implements HttpServletRequest, Serializable {

	private static final long serialVersionUID = 5808722421644106536L;
	private static final int BUFFER_SIZE = 4096;
	private String method;
	private String contentType;
	private byte[] content;
	private ListMultimap<String, String> headers;
	private Map<String, String[]> parameters;
	private String contextPath;
	private String characterEncoding;
	private Map<String, Object> attributes = new HashMap<>();
	private String protocol;
	private String scheme;
	private String serverName;
	private int serverPort;
	private String remoteAddr;
	private String remoteHost;
	private List<Locale> locales;
	private boolean secure;
	private String localName;
	private int remotePort;
	private int localPort;
	private String localAddr;
	private Cookie[] cookies;
	private String authType;
	private String pathInfo;
	private String pathTranslated;
	private String queryString;
	private String remoteUser;
	private String requestedSessionId;
	private String requestURI;
	private StringBuffer requestURL;
	private String servletPath;

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;

	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		characterEncoding = env;

	}

	@Override
	public int getContentLength() {
		return content.length;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStream() {

			private final ByteArrayInputStream inputStream = new ByteArrayInputStream(
					content);

			@Override
			public int read() throws IOException {
				return inputStream.read();
			}
		};
	}

	@Override
	public String getParameter(String name) {
		String[] values = parameters.get(name);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {

		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {

		return parameters.get(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {

		return Collections.unmodifiableMap(parameters);
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(content), getCharacterEncoding()));
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return locales.get(0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getLocales() {
		return Collections.enumeration(locales);
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {

		System.err.println("get dispatcher");
		return null;
	}

	@Override
	public String getRealPath(String path) {
		System.err.println("get real path");
		return null;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getLocalAddr() {
		return localAddr;
	}

	@Override
	public int getLocalPort() {
		return localPort;

	}

	@Override
	public String getAuthType() {
		return authType;
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	@Override
	public long getDateHeader(String name) {
		System.err.println("get date header");
		return -1;
	}

	@Override
	public String getHeader(String name) {

		String lowerName = name.toLowerCase();
		if (headers.containsKey(lowerName)) {
			return headers.get(lowerName).get(0);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaders(String name) {

		return Collections.enumeration(headers.get(name.toLowerCase()));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaderNames() {

		return Collections.enumeration(headers.keySet());
	}

	@Override
	public int getIntHeader(String name) {

		String header = getHeader(name);
		if (header == null) {
			return -1;
		}
		return Integer.parseInt(header);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return pathTranslated;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return remoteUser;
	}

	@Override
	public boolean isUserInRole(String role) {
		System.err.println("user in role? " + role);
		return true;
	}

	@Override
	public Principal getUserPrincipal() {
		System.err.println("get user principal");
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		return requestURL;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession(boolean create) {
		
		System.err.println("get session " + create);
		return null;
	}

	@Override
	public HttpSession getSession() {

		System.err.println("get session");
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {

		System.err.println("session id valid?");
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {

		System.err.println("session id from cookie?");
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {

		System.err.println("session id from cookie?");
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {

		System.err.println("session id from cookie?");
		return false;
	}

	public static HttpServletRequest valueOf(HttpServletRequest request) {

		SerializableRequest serializableRequest = new SerializableRequest();
		serializableRequest.method = request.getMethod();
		serializableRequest.contentType = request.getContentType();
		serializableRequest.content = getContent(request);
		serializableRequest.headers = getHeaders(request);
		serializableRequest.contextPath = request.getContextPath();
		serializableRequest.characterEncoding = request.getCharacterEncoding();
		serializableRequest.parameters = getParameters(request);
		serializableRequest.protocol = request.getProtocol();
		serializableRequest.scheme = request.getScheme();
		serializableRequest.serverName = request.getServerName();
		serializableRequest.serverPort = request.getServerPort();
		serializableRequest.remoteAddr = request.getRemoteAddr();
		serializableRequest.remoteHost = request.getRemoteHost();
		serializableRequest.locales = getLocales(request);
		serializableRequest.secure = request.isSecure();
		serializableRequest.localName = request.getLocalName();
		serializableRequest.remotePort = request.getRemotePort();
		serializableRequest.localPort = request.getLocalPort();
		serializableRequest.localAddr = request.getLocalAddr();
		serializableRequest.cookies = getCookies(request);
		serializableRequest.authType = request.getAuthType();
		serializableRequest.pathInfo = request.getPathInfo();
		serializableRequest.pathTranslated = request.getPathTranslated();
		serializableRequest.queryString = request.getQueryString();
		serializableRequest.remoteUser = request.getRemoteUser();
		serializableRequest.requestedSessionId = request.getRequestedSessionId();
		serializableRequest.requestURI = request.getRequestURI();
		serializableRequest.requestURL = request.getRequestURL();
		serializableRequest.servletPath = request.getServletPath();
		return serializableRequest;
	}
	

	private static List<Locale> getLocales(HttpServletRequest request) {
		List<Locale> localeList = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Enumeration<Locale> locales = request.getLocales();
		while(locales.hasMoreElements()) {
			localeList.add(locales.nextElement());
		}
		return localeList;
	}

	private static Cookie[] getCookies(HttpServletRequest request) {
		int numCookies = request.getCookies().length;
		if(numCookies < 1) {
			return null;
		}
		Cookie[] cookies = new Cookie[numCookies];
		for(int i = 0; i < numCookies; i++) {
			cookies[i] = SerializableCookie.valueOf(request.getCookies()[i]);
		}
		return cookies;
	}

	private static Map<String, String[]> getParameters(
			HttpServletRequest request) {

		Map<String, String[]> parameters = new HashMap<>();
		@SuppressWarnings("unchecked")
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {

			String parameterName = parameterNames.nextElement().toLowerCase();
			String[] values = request.getParameterValues(parameterName);
			parameters.put(parameterName, values);
		}
		return parameters;
	}

	private static ListMultimap<String, String> getHeaders(
			HttpServletRequest request) {

		ListMultimap<String, String> multiMap = ArrayListMultimap.create();
		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {

			String headerName = headerNames.nextElement().toLowerCase();
			@SuppressWarnings("unchecked")
			Enumeration<String> values = request.getHeaders(headerName);
			while (values.hasMoreElements()) {

				String value = values.nextElement();
				multiMap.put(headerName, value);
			}
		}
		return multiMap;
	}

	private static byte[] getContent(HttpServletRequest request) {

		try (InputStream in = request.getInputStream()) {
			byte[] buffer = new byte[BUFFER_SIZE];
			ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
			while (true) {
				int byteCount = in.read(buffer);
				if (byteCount == -1) {
					break;
				}
				out.write(buffer, 0, byteCount);
			}
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
