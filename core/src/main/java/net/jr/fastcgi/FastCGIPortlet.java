/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi;

import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.FastCGIHandlerFactory;
import net.jr.fastcgi.impl.PortletRequestAdapter;
import net.jr.fastcgi.impl.PortletResponseAdapter;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Porlet version is quite experimental, but functionnal.
 *
 * @author jrialland
 */
public class FastCGIPortlet extends GenericPortlet {

    private FastCGIHandler handler = new FastCGIHandler();

    @Override
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        Map<String, String> config = new TreeMap<String, String>();
        for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
            String value = portletConfig.getInitParameter(paramName);
            if (value != null) {
                config.put(paramName, getInitParameter(paramName));
            }
        }
        handler = FastCGIHandlerFactory.create(config);
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        handler.service(new PortletRequestAdapter(request),
                new PortletResponseAdapter(response));
    }

    @Override
    public void destroy() {
        super.destroy();
        handler.destroy();
    }
}