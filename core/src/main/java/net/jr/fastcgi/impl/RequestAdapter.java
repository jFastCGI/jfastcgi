/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.io.InputStream;
import java.util.Enumeration;

/**
 * part of the portlet / servlet request interface used by FastCGIHandler. Allows to use the same code
 * for both portlet and servlet environment.
 * 
 * @author jrialland
 *
 */
public interface RequestAdapter {

	public InputStream getInputStream();

	public String getRequestURI();

	public String getMethod();

	public String getServerName();

	public int getServerPort();

	public String getRemoteAddr();

	public String getRemoteUser();

	public String getAuthType();

	public String getProtocol();

	public String getQueryString();

	public String getServletPath();

	public String getRealPath(String relPath);
	
	public String getContextPath();

	public int getContentLength();

	public Enumeration<String> getHeaderNames();

	public String getHeader(String key);
}
