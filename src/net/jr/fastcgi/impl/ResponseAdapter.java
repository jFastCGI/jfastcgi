package net.jr.fastcgi.impl;

import java.io.OutputStream;

public interface ResponseAdapter {

	public void sendError(int errorCode);
	
	public void setStatus(int statusCode);
	
	public void sendRedirect(String targetUrl);
	
	public void addHeader(String key, String value);
	
	public OutputStream getOutputStream();
}
