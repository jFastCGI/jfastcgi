/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.portlet.RenderResponse;

public class PortletResponseAdapter implements ResponseAdapter {

	private RenderResponse renderResponse;

	public PortletResponseAdapter(RenderResponse renderResponse)
	{
		this.renderResponse = renderResponse;
	}
	
	public void addHeader(String key, String value) {
		//can't add anything with portlets.
	}

	public OutputStream getOutputStream() {
		try {
			return renderResponse.getPortletOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendError(int errorCode) {
		// does nothing
	}

	public void sendRedirect(String targetUrl) {
		// does nothing
	}

	public void setStatus(int statusCode) {
		// does nothing
	}

}
