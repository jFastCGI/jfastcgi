/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package org.jfastcgi.portlet.impl;

import org.jfastcgi.api.RequestAdapter;

import javax.portlet.PortletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;

public class PortletRequestAdapter implements RequestAdapter {

    private PortletRequest portletRequest;

    public PortletRequestAdapter(PortletRequest portletRequest) {
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
    
    public String getContentType() {
        return null;
    }

    public String getRemoteAddr() {
        return "";
    }

    public String getRemoteHost() {
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

    public String getPathInfo() {
        return "";
    }

    public String getRealPath(String realPath) {
        return realPath;
    }
}
