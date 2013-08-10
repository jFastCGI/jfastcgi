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
package org.jfastcgi.client;

import org.jfastcgi.api.ConnectionFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A connection factory that always tries to connect to the same ip/port.
 *
 * @author jrialland
 */
public class SingleConnectionFactory implements ConnectionFactory {

    private InetAddress host;

    private int port;

    public SingleConnectionFactory(InetSocketAddress sockAddr) {
        this(sockAddr.getAddress(), sockAddr.getPort());
    }

    public SingleConnectionFactory(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    public SingleConnectionFactory(String descriptor) {
        Matcher m = Pattern.compile("([^:]+):([1-9][0-9]*)$").matcher(descriptor.trim());
        if (m.matches()) {
            try {
                this.host = InetAddress.getByName(m.group(1));
                this.port = Integer.parseInt(m.group(2));
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            throw new IllegalArgumentException("syntax error (required format is <host>:<port>) - " + descriptor);
        }
    }

    public Socket getConnection() {
        try {
            return new Socket(host, port);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseConnection(Socket socket) {
        try {
            socket.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
