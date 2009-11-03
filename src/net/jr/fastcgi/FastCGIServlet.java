/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.FastCGIHandlerFactory;
import net.jr.fastcgi.impl.ServletRequestAdapter;
import net.jr.fastcgi.impl.ServletResponseAdapter;

/**
 * @author jrialland
 *
 */
public class FastCGIServlet extends HttpServlet {

	private static final long serialVersionUID = -8597795652806478718L;

	private FastCGIHandler handler = new FastCGIHandler();

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		Map<String, String> config = new TreeMap<String, String>();
		for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
			String value = getInitParameter(paramName);
			if(value != null){
				config.put(paramName, getInitParameter(paramName));
			}
		}
		handler = FastCGIHandlerFactory.create(config);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handler.service(new ServletRequestAdapter(request), new ServletResponseAdapter(response));
	}
	
	@Override
	public void destroy() {
		super.destroy();
		handler.destroy();
	}
}
