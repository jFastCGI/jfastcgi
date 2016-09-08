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
package org.jfastcgi.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Implements PoolableObjectFactory so it creates tcp connections on demand
 * using pool configuration.
 *
 * @author jrialland
 */
public class PoolFactory extends BasePooledObjectFactory<ISocket> {

    private final Random random = new Random();

    /**
     * List of configured host/port pairs.
     */
    private final List<ConnectionDescriptor> addresses = new ArrayList<ConnectionDescriptor>();

    /**
     * build a ConnDesc from a &quot;host:port&quot; string.
     * <p/>
     * If you want to specify ipv6 addresses, use the format
     * &quot;[ipv6]:port&quot;, e.g. &quot;[::1]:9000&quot;
     *
     * @param address
     *            the input address
     * @return a parsed ConnectionDescription
     */

    public void addAddress(final String address) {
        addresses.add(ConnectionDescriptor.makeConnDesc(address));
    }

    public void addAdresses(final Iterable<String> addresses) {
        for (final String address : addresses) {
            this.addresses.add(ConnectionDescriptor.makeConnDesc(address));
        }
    }

    /**
     * may simplify future spring integration...
     *
     * @param address
     */
    public void setAddress(final String address) {
        addAddress(address);
    }

    /**
     * may simplify future spring integration...
     *
     * @param addresses
     */
    public void setAddresses(final Iterable<String> addresses) {
        addAdresses(addresses);
    }

    @Override
    public ISocket create() throws Exception {
        final int index = random.nextInt(addresses.size() - 1);
        final ConnectionDescriptor desc = addresses.get(index);
        return desc.makeSocket();
    }

    @Override
    public PooledObject<ISocket> wrap(final ISocket obj) {
        return new DefaultPooledObject<ISocket>(obj);
    }
}
