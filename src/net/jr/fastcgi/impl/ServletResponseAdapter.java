package net.jr.fastcgi.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

public class ServletResponseAdapter implements ResponseAdapter {

	private HttpServletResponse httpServletResponse;

	public ServletResponseAdapter(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}

	public void addHeader(String key, String value) {
		httpServletResponse.addHeader(key, value);
	}

	public void sendError(int errorCode) {
		try {
			httpServletResponse.sendError(errorCode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendRedirect(String targetUrl) {
		try {
			httpServletResponse.sendRedirect(targetUrl);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setStatus(int statusCode) {
		httpServletResponse.setStatus(statusCode);
	}

	public OutputStream getOutputStream() {
		try {
			return httpServletResponse.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
