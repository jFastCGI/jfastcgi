package net.jr.fastcgi.impl;

import java.io.OutputStream;

/**
 / part of the portlet / servlet response interface used by FastCGIHandler. Allows to use the same code
 * for both portlet and servlet environment.
 * 
 * @author jrialland
 *
 */
public interface ResponseAdapter {

	public void sendError(int errorCode);
	
	public void setStatus(int statusCode);
	
	public void sendRedirect(String targetUrl);
	
	public void addHeader(String key, String value);
	
	public OutputStream getOutputStream();
}
