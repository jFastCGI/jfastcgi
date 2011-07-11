package net.jr.fastcgi.test;

import javax.servlet.http.HttpServletResponse;

import net.jr.fastcgi.FastCGIServlet;
import net.jr.fastcgi.impl.FastCGIHandlerFactory;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.servlet.ServletTestCaseAdapter;

public class TestFastCgiConnection extends ServletTestCaseAdapter {

	private SampleServer sampleServer = new SampleServer();

	private Thread sampleServerThread = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		getWebMockObjectFactory().getMockServletConfig().setInitParameter(FastCGIHandlerFactory.PARAM_SERVER_ADDRESS, "localhost:" + sampleServer.getPort());
		createServlet(FastCGIServlet.class);

		// start dummy fastcgi app
		sampleServerThread = new Thread(sampleServer);
		sampleServerThread.setDaemon(true);
		sampleServerThread.start();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		// stop fastcgi thread
		sampleServerThread.interrupt();
	}

	/**
	 * Simple test : just send a request, and assert that the answer is ok
	 * @throws Exception
	 */
	public void testItWorks() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setBodyContent("");
		MockHttpServletResponse response = new MockHttpServletResponse();

		getServlet().service(request, response);

		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
	}

}
