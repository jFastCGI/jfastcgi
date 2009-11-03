package net.jr.fastcgi.impl;

import java.io.InputStream;
import java.util.Enumeration;

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
