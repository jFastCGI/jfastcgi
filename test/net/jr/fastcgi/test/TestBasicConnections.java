package net.jr.fastcgi.test;

import net.jr.fastcgi.FastCGIServlet;

import com.mockrunner.servlet.ServletTestCaseAdapter;

public class TestBasicConnections extends ServletTestCaseAdapter {
	
	private SampleServer sampleServer = new SampleServer();
	
	private Thread sampleServerThread = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		sampleServerThread = new Thread(sampleServer);
		sampleServerThread.setDaemon(true);
		sampleServerThread.start();
		
		
		getWebMockObjectFactory().getMockServletConfig().setInitParameter(
				"server-address", "localhost:"+sampleServer.getPort());
		createServlet(FastCGIServlet.class);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		sampleServerThread.interrupt();
	}
	
	public void testItWorks() throws Exception {
		doPost();
		System.out.println(getOutput());
	}

}
