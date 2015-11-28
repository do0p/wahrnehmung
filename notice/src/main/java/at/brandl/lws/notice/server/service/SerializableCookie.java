package at.brandl.lws.notice.server.service;

import java.io.Serializable;

import javax.servlet.http.Cookie;

public class SerializableCookie extends Cookie implements Serializable {

	private static final long serialVersionUID = -2276613579788689724L;

	public SerializableCookie(String name, String value) {
		super(name, value);
	}

	public static Cookie valueOf(Cookie cookie) {
		SerializableCookie serializableCookie = new SerializableCookie(
				cookie.getName(), cookie.getValue());
		serializableCookie.setComment(cookie.getComment());
		if (cookie.getDomain() != null) {
			serializableCookie.setDomain(cookie.getDomain());
		}
		serializableCookie.setMaxAge(cookie.getMaxAge());
		serializableCookie.setPath(cookie.getPath());
		serializableCookie.setSecure(cookie.getSecure());
		serializableCookie.setVersion(cookie.getVersion());
		return serializableCookie;
	}
}
