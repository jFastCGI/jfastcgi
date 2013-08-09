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
package org.jfastcgi.fastcgi.impl;

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

    /**
     * embeds ip + port into a single structure.
     *
     * @author jrialland
     */
    private static class ConnDesc {
        private InetAddress addr;
        private int port;

        public ConnDesc(InetAddress addr, int port) {
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
    private List<ConnDesc> addresses = new ArrayList<ConnDesc>();

    /**
     * build a ConnDesc from a "host:port" string.
     *
     * @param s
     * @return
     */
    private static ConnDesc makeConnDesc(String s) {
        Matcher m = Pattern.compile("([^:]+):([1-9][0-9]*)$").matcher(s);
        if (m.matches()) {
            try {
                InetAddress addr = InetAddress.getByName(m.group(1));
                int port = Integer.parseInt(m.group(2));
                return new ConnDesc(addr, port);
            }
            catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            throw new IllegalArgumentException(s);
        }
    }

    /**
     * builds a new socket using one of the descriptors.
     */
    @Override
    public Object makeObject() throws Exception {
        int index = random.nextInt(addresses.size() - 1);
        ConnDesc desc = addresses.get(index);
        return new Socket(desc.getAddr(), desc.getPort());
    }

    public void addAddress(String address) {
        this.addresses.add(makeConnDesc(address));
    }

    public void addAdresses(Iterable<String> addresses) {
        for (String s : addresses) {
            this.addresses.add(makeConnDesc(s));
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
