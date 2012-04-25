/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;

import javax.portlet.PortletRequest;

public class PortletRequestAdapter implements RequestAdapter {

	private PortletRequest portletRequest;
	
	public PortletRequestAdapter(PortletRequest portletRequest)
	{
		this.portletRequest = portletRequest;
	}
	
	public String getAuthType() {
		return portletRequest.getAuthType();
	}

	public int getContentLength() {
		return 0;
	}

	public String getContextPath() {
		return portletRequest.getContextPath();
	}

	public String getHeader(String key) {
		return "";
	}
	
@SuppressWarnings("unchecked")
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(Collections.EMPTY_SET);
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(new byte[]{});
	}

	public String getMethod() {
		return "POST";
	}

	public String getProtocol() {
		return "http";
	}

	public String getQueryString() {
		return "";
	}

	public String getRemoteAddr() {
		return "";
	}

	public String getRemoteUser() {
		return portletRequest.getRemoteUser();
	}

	public String getRequestURI() {
		return "/";
	}

	public String getServerName() {
		return portletRequest.getServerName();
	}

	public int getServerPort() {
		return portletRequest.getServerPort();
	}

	public String getServletPath() {
		return portletRequest.getContextPath();
	}

	public String getRealPath(String relPath) {
		return relPath;
	}
}
