package net.jr.fastcgi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.FastCGIHandlerFactory;
import net.jr.fastcgi.impl.PortletRequestAdapter;
import net.jr.fastcgi.impl.PortletResponseAdapter;

/**
 * Porlet version is quite experimental, but functionnal.
 * 
 * TODO : add examples.
 * 
 * @author jrialland
 *
 */
public class FastCGIPortlet extends GenericPortlet {

	private FastCGIHandler handler = new FastCGIHandler();

	@Override
	public void init(PortletConfig portletConfig) throws PortletException {
		super.init(portletConfig);
		Map<String, String> config = new TreeMap<String, String>();
		for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
			config.put(paramName, getInitParameter(paramName));
		}
		handler = FastCGIHandlerFactory.create(config);
	}

	@Override
	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		try {
			handler.service(new PortletRequestAdapter(request),
					new PortletResponseAdapter(response));
		} catch (ServletException e) {
			throw new PortletException(e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		handler.destroy();
	}
}
