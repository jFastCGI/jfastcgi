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

import com.fastcgi.FCGIInterface;

/**
 * A dummy fastcgi application in java.
 *
 * @author jrialland
 */
public class SampleServer implements Runnable {

    public static final int DEFAULT_PORT = 59812;

    private int port;

    public SampleServer(int port) {
        this.port = port;
    }

    public SampleServer() {
        this(DEFAULT_PORT);
    }

    /**
     * Answers request by sending back an html page.
     */
    public void run() {
        int count = 0;
        System.setProperty("FCGI_PORT", "" + port);
        FCGIInterface itf = new FCGIInterface();
        while (itf.FCGIaccept() >= 0) {
            count++;
            System.out.println("Content-type: text/html\n\n");
            System.out.println("<html>");
            System.out.println("<head><TITLE>FastCGI-Hello Java stdio</TITLE></head>");
            System.out.println("<body>");
            System.out.println("<H3>FastCGI-HelloJava stdio</H3>");
            System.out.println("request number " + count + " running on host " + System.getProperty("SERVER_NAME"));
            System.out.println("</body>");
            System.out.println("</html>");
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * it accepts a tcp port number to bind as parameter
     *
     * @param args
     */
    public static void main(String... args) {
        int port = DEFAULT_PORT;
        // serve forever
        if (args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        new SampleServer(port).run();
    }
}
