/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developers.
 * Released under BSD License, see LICENSE_JRIALLAND.txt
 */
/*
 Copyright (c) 2013-2016 - the jFastCGI project developers.

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jfastcgi.client.FastCGIHandlerFactory;
import org.jfastcgi.servlet.FastCGIServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class TestFastCgiConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFastCgiConnection.class);

    private Thread fastCgiServer;

    int serverPort = 0;

    int fcgiPort = 0;

    private Server jettyServer = null;

    public static List<Integer> getFreeTcpPorts(int amount) throws Exception {
        List<Integer> list = new ArrayList<Integer>(amount);
        List<ServerSocket> toClose = new ArrayList<ServerSocket>(amount);
        try {
            for (int i = 0; i < amount; i++) {
                final ServerSocket ss = new ServerSocket(0);
                list.add(ss.getLocalPort());
                toClose.add(ss);
            }
        } finally {
            for (ServerSocket ss : toClose) {
                ss.close();
            }
        }
        return list;
    }

    @Before
    public void setUp() throws Exception {

        List<Integer> freePorts = getFreeTcpPorts(2);
        fcgiPort = freePorts.get(0);
        serverPort = freePorts.get(1);

        LOGGER.debug("Starting fcgi server");
        fastCgiServer = new SampleServer(fcgiPort).start();

        LOGGER.debug("fcgi server started");

        LOGGER.debug("Starting servlet container");
        serverPort = 8080;
        jettyServer = new Server(serverPort);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setResourceBase(Files.createTempFile("jetty", "").toAbsolutePath().toString());
        context.setContextPath("/");
        jettyServer.setHandler(context);

        ServletHolder holder = context.addServlet(FastCGIServlet.class, "/*");
        holder.setInitParameter(FastCGIHandlerFactory.PARAM_SERVER_ADDRESS, "localhost:" + fcgiPort);

        jettyServer.start();
        LOGGER.debug("Servlet container started, listening on port " + serverPort);
    }

    @After
    public void tearDown() throws Exception {
        // stop jetty
        jettyServer.stop();

        // stop fastcgi server
        fastCgiServer.interrupt();
    }

    /**
     * Simple test : just send a request, and assert that the answer is ok
     *
     * @throws Exception
     */
    @Test
    public void testItWorks() throws Exception {

        WebConversation wc = new WebConversation();

        WebRequest request = new GetMethodWebRequest("http://localhost:" + serverPort + "/");
        WebResponse response = wc.getResponse(request);
        System.out.println(new String(response.getBytes()));

        assertEquals(response.getResponseCode(), HttpServletResponse.SC_OK);
        assertTrue(response.getContentLength() > 0);

        assertEquals(response.getElementsByTagName("H3").length, 1);
        assertEquals(response.getTitle(), "FastCGI-Hello");
    }

}
