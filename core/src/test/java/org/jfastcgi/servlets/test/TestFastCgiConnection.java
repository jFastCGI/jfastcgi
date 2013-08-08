package org.jfastcgi.servlets.test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import net.jr.fastcgi.FastCGIServlet;
import net.jr.fastcgi.impl.FastCGIHandlerFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertEquals;

public class TestFastCgiConnection {
    private FastCGIServlet fastCGIServlet;
    private SampleServer sampleServer = new SampleServer();

    private Thread sampleServerThread = null;
    private Server jettyServer = null;

    @Before
    public void setUp() throws Exception {
        // start dummy fastcgi app
        sampleServerThread = new Thread(sampleServer);
        sampleServerThread.setDaemon(true);
        sampleServerThread.start();


        jettyServer = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jettyServer.setHandler(context);

        ServletHolder holder = context.addServlet(FastCGIServlet.class, "/*");
        holder.setInitParameter(FastCGIHandlerFactory.PARAM_SERVER_ADDRESS, "localhost:" + sampleServer.getPort());

        jettyServer.start();
//        jettyServer.join();

    }

    //    @Override
    @After
    public void tearDown() throws Exception {
        // stop fastcgi thread
        sampleServerThread.interrupt();

        // stop jetty
        jettyServer.stop();
    }

    /**
     * Simple test : just send a request, and assert that the answer is ok
     *
     * @throws Exception
     */
    @Test
    public void testItWorks() throws Exception {
        WebConversation wc = new WebConversation();

        WebRequest request = new GetMethodWebRequest("http://localhost:8080/");
        WebResponse response = wc.getResponse(request);

        assertEquals(response.getResponseCode(), HttpServletResponse.SC_OK);
        assertEquals(response.getElementsByTagName("H3").length, 1);
        assertEquals(response.getTitle(), "FastCGI-Hello Java stdio");
    }

}
