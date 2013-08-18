/*
 Copyright (c) 1996 Open Market, Inc.
 (see LICENSE_OPEN_MARKET.txt)
 */
/*

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
package com.fastcgi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.Properties;

/*
 * This is the FastCGI interface that the application calls to communicate with the
 * FastCGI web server. This version is single threaded, and handles one request at
 * a time, which is why we can have a static variable for it.
 */
public class FCGIInterface {
    /*
    * Class variables
    */
    private static FCGIRequest request = null;
    private static boolean acceptCalled = false;
    private static boolean isFCGI = true;
    private static Properties startupProps;
    private static ServerSocket srvSocket;

    /*
    * Accepts a new request from the HTTP server and creates
    * a conventional execution environment for the request.
    * If the application was invoked as a FastCGI server,
    * the first call to FCGIaccept indicates that the application
    * has completed its initialization and is ready to accept
    * a request.  Subsequent calls to FCGI_accept indicate that
    * the application has completed its processing of the
    * current request and is ready to accept a new request.
    * If the application was invoked as a CGI program, the first
    * call to FCGIaccept is essentially a no-op and the second
    * call returns EOF (-1) as does an error. Application should exit.
    *
    * If the application was invoked as a FastCGI server,
    * and this is not the first call to this procedure,
    * FCGIaccept first flushes any buffered output to the HTTP server.
    *
    * On every call, FCGIaccept accepts the new request and
    * reads the FCGI_PARAMS stream into System.props. It also creates
    * streams that understand FastCGI protocol and take input from
    * the HTTP server send output and error output to the HTTP server,
    * and assigns these new streams to System.in, System.out and
    * System.err respectively.
    *
    * For now, we will just return an int to the caller, which is why
    * this method catches, but doen't throw Exceptions.
    *
    */
    public int FCGIaccept() {
        boolean acceptResult = accept();
        if (acceptResult) {
            return 0;
        }
        else {
            return -1;
        }
    }

    public boolean accept() {
        boolean acceptResult = true;

        /*
         * If first call, mark it and if fcgi save original system properties,
         * If not first call, and  we are cgi, we should be gone.
         */
        if (!acceptCalled) {
            isFCGI = System.getProperties().containsKey("FCGI_PORT");
            acceptCalled = true;
            if (isFCGI) {
                /*
                 * save original system properties (nonrequest)
                 * and get a server socket
                 */
                startupProps = new Properties(System.getProperties());
                String str = System.getProperty("FCGI_PORT");
                if (str.length() <= 0) {
                    return false;
                }
                int portNum = Integer.parseInt(str);

                try {
                    srvSocket = new ServerSocket(portNum);
                }
                catch (IOException e) {
                    if (request != null) {
                        request.setSocket(null);
                    }
                    srvSocket = null;
                    request = null;
                    return false;
                }
            }
        }
        else {
            if (!isFCGI) {
                return false;
            }
        }
        /*
         * If we are cgi, just leave everything as is, otherwise set up env
         */
        if (isFCGI) {
            try {
                acceptResult = FCGIAccept();
            }
            catch (IOException e) {
                return false;
            }
            if (!acceptResult) {
                return false;
            }

            /*
            * redirect stdin, stdout and stderr to fcgi socket
            */
            System.setIn(new BufferedInputStream(request.getInStream(), 8192));
            System.setOut(new PrintStream(new BufferedOutputStream(
                    request.getOutStream(), 8192)));
            System.setErr(new PrintStream(new BufferedOutputStream(
                    request.getErrStream(), 512)));
            System.setProperties(request.getParams());
        }
        return true;
    }

    /*
     * Accepts a new request from the HTTP server.
     * Finishes the request accepted by the previous call
     * to FCGI_Accept. Sets up the FCGI environment and reads
     * saved and per request environmental varaibles into
     * the request object. (This is redundant on System.props
     * as long as we can handle only one request object.)
     */
    boolean FCGIAccept() throws IOException {

        boolean isNewConnection;
        boolean errCloseEx = false;
        boolean outCloseEx = false;

        if (request != null) {
            /*
             * Complete the previous request
             */
            System.err.close();
            System.out.close();
            boolean prevRequestfailed = (errCloseEx || outCloseEx ||
                    request.getInStream().getFCGIError() != 0 ||
                    request.getInStream().getException() != null);
            if (prevRequestfailed || !request.isKeepConnection()) {
                request.getSocket().close();
                request.setSocket(null);
            }
            if (prevRequestfailed) {
                request = null;
                return false;
            }
        }
        else {
            /*
             * Get a Request and initialize some variables
             */
            request = new FCGIRequest();
            request.setSocket(null);
            request.setInStream(null);
        }
        isNewConnection = false;

        /*
         * if connection isnt open accept a new connection (blocking)
         */
        while (true) {
            if (request.getSocket() == null) {
                try {
                    request.setSocket(srvSocket.accept());
                }
                catch (IOException e) {
                    request.setSocket(null);
                    request = null;
                    return false;
                }
                isNewConnection = true;
            }

            /* Try reading from new connection. If the read fails and
             * it was an old connection the web server probably closed it;
             * try making a new connection before giving up
             */
            request.setBeginProcessed(false);
            request.setInStream(new FCGIInputStream((FileInputStream) request.getSocket().getInputStream(),
                    8192, 0, request));
            request.getInStream().fill();
            if (request.isBeginProcessed()) {
                break;
            }
            request.getSocket().close();

            request.setSocket(null);
            if (isNewConnection) {
                return false;
            }
        }
        /*
        * Set up the objects for the new request
        */
        request.setParams(new Properties(startupProps));
        switch (request.getRole()) {
            case FCGIConstants.ROLE_RESPONDER:
                request.getParams().put("ROLE", "RESPONDER");
                break;
            case FCGIConstants.ROLE_AUTHORIZER:
                request.getParams().put("ROLE", "AUTHORIZER");
                break;
            case FCGIConstants.ROLE_FILTER:
                request.getParams().put("ROLE", "FILTER");
                break;
            default:
                return false;
        }
        request.getInStream().setReaderType(FCGIConstants.TYPE_PARAMS);
        /*
         * read the rest of request parameters
         */
        if (new FCGIMessage(request.getInStream()).readParams(request.getParams()) < 0) {
            return false;
        }
        request.getInStream().setReaderType(FCGIConstants.TYPE_STDIN);
        request.setOutStream(new FCGIOutputStream((FileOutputStream) request.getSocket().
        getOutputStream(), 8192,
        FCGIConstants.TYPE_STDOUT, request));
        request.setErrStream(new FCGIOutputStream((FileOutputStream) request.getSocket().
        getOutputStream(), 512,
        FCGIConstants.TYPE_STDERR, request));
        request.setNumWriters(2);
        return true;
    }
}
