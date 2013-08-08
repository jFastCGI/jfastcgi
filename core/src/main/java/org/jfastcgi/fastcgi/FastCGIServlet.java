/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package org.jfastcgi.fastcgi;

import org.jfastcgi.fastcgi.impl.FastCGIHandler;
import org.jfastcgi.fastcgi.impl.FastCGIHandlerFactory;
import org.jfastcgi.fastcgi.impl.ServletRequestAdapter;
import org.jfastcgi.fastcgi.impl.ServletResponseAdapter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jrialland
 */
public class FastCGIServlet extends HttpServlet {

    private static final long serialVersionUID = -8597795652806478718L;

    private FastCGIHandler handler = new FastCGIHandler();

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        Map<String, String> config = new TreeMap<String, String>();
        for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
            String value = servletConfig.getInitParameter(paramName);
            if (value != null) {
                config.put(paramName, getInitParameter(paramName));
            }
        }
        handler = FastCGIHandlerFactory.create(config);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handler.service(new ServletRequestAdapter(getServletContext(), request), new ServletResponseAdapter(response));
    }

    @Override
    public void destroy() {
        super.destroy();
        handler.destroy();
    }

    public FastCGIHandler getHandler() {
        return handler;
    }

    public void setHandler(FastCGIHandler handler) {
        this.handler = handler;
    }
}
