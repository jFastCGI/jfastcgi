package net.jr.fastcgi;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.ServletRequestAdapter;
import net.jr.fastcgi.impl.ServletResponseAdapter;
import net.jr.fastcgi.impl.SingleConnectionFactory;

public class FastCGIServlet extends HttpServlet {

	private static final long serialVersionUID = -8597795652806478718L;

	private FastCGIHandler handler = new FastCGIHandler();

	public void init() throws ServletException {

		String serverAddress = getInitParameter("server-address");
		if(serverAddress != null) {
			handler.setConnectionFactory(new SingleConnectionFactory(serverAddress));
		} 

		String startProcess = getInitParameter("start-executable");
		if (startProcess != null) {
			try {
				handler.startProcess(startProcess);
			} catch (IOException e) {
				throw new ServletException(e);
			}
		}
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
