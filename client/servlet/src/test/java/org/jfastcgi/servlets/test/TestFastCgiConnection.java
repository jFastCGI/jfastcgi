/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developers.
 * Released under BSD License, see LICENSE_JRIALLAND.txt
 */
/*
 Copyright (c) 2013 - the jFastCGI project developers.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.jfastcgi.servlets.test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.jfastcgi.servlet.FastCGIServlet;
import org.jfastcgi.client.FastCGIHandlerFactory;
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
