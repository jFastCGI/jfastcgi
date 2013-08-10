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

import org.apache.commons.pool.BasePoolableObjectFactory;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements PoolableObjectFactory so it creates tcp connections on demand using pool configuration.
 *
 * @author jrialland
 */
public class PoolFactory extends BasePoolableObjectFactory {
    private final static Pattern PATTERN_HOSTNAME_PORT = Pattern.compile("([^:]+):([1-9][0-9]*)$");
    private final static Pattern PATTERN_IPV6_PORT = Pattern.compile("\\[((?:(?:(?:[0-9a-fA-F]{0,4}):){2,7})(?:[0-9a-fA-F]{1,4}))\\]:([1-9][0-9]*)");

    /**
     * embeds ip + port into a single structure.
     *
     * @author jrialland
     */
    static class ConnectionDescriptor {
        private final InetAddress addr;
        private final int port;

        public ConnectionDescriptor(InetAddress addr, int port) {
            this.addr = addr;
            this.port = port;
        }

        public InetAddress getAddr() {
            return addr;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return addr.getHostName() + ":" + port;
        }
    }

    private Random random = new Random();

    /**
     * List of configured host/port pairs.
     */
    private List<ConnectionDescriptor> addresses = new ArrayList<ConnectionDescriptor>();

    /**
     * build a ConnDesc from a &quot;host:port&quot; string.
     * <p/>
     * If you want to specify ipv6 addresses, use the format &quot;[ipv6]:port&quot;, e.g. &quot;[::1]:9000&quot;
     *
     * @param address the input address
     * @return a parsed ConnectionDescription
     */
    static ConnectionDescriptor makeConnDesc(String address) {
        if (address == null) {
            throw new IllegalArgumentException("null for Connection Description given. Try something like localhost:9000 ");
        }

        Matcher ipv4Matcher = PATTERN_HOSTNAME_PORT.matcher(address);
        Matcher ipv6Matcher = PATTERN_IPV6_PORT.matcher(address);
        if (ipv4Matcher.matches()) {
            try {
                InetAddress addr = InetAddress.getByName(ipv4Matcher.group(1));
                int port = Integer.parseInt(ipv4Matcher.group(2));
                return new ConnectionDescriptor(addr, port);
            }
            catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else if (ipv6Matcher.matches()) {
            try {
                InetAddress addr = InetAddress.getByName(ipv6Matcher.group(1));
                int port = Integer.parseInt(ipv6Matcher.group(2));
                return new ConnectionDescriptor(addr, port);
            }
            catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            throw new IllegalArgumentException(address);
        }
    }

    /**
     * builds a new socket using one of the descriptors.
     */
    @Override
    public Object makeObject() throws Exception {
        int index = random.nextInt(addresses.size() - 1);
        ConnectionDescriptor desc = addresses.get(index);
        return new Socket(desc.getAddr(), desc.getPort());
    }

    public void addAddress(String address) {
        this.addresses.add(makeConnDesc(address));
    }

    public void addAdresses(Iterable<String> addresses) {
        for (String address : addresses) {
            this.addresses.add(makeConnDesc(address));
        }
    }

    /**
     * may simplify future spring integration...
     *
     * @param address
     */
    public void setAddress(String address) {
        addAddress(address);
    }

    /**
     * may simplify future spring integration...
     *
     * @param addresses
     */
    public void setAddresses(Iterable<String> addresses) {
        addAdresses(addresses);
    }
}
