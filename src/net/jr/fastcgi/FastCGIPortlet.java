package net.jr.fastcgi;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.PortletRequestAdapter;
import net.jr.fastcgi.impl.PortletResponseAdapter;
import net.jr.fastcgi.impl.SingleConnectionFactory;

public class FastCGIPortlet extends GenericPortlet {

	private FastCGIHandler handler = new FastCGIHandler();

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		String serverAddress = getInitParameter("server-address");
		if (serverAddress != null) {
			handler.setConnectionFactory(new SingleConnectionFactory(serverAddress));
		}
		
		String startProcess = getInitParameter("start-executable");
		if (startProcess != null) {
			try {
				handler.startProcess(startProcess);
			} catch (IOException e) {
				throw new PortletException(e);
			}
		}
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
