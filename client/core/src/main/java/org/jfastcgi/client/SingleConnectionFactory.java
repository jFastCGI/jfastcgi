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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.jfastcgi.api.ConnectionFactory;

/**
 * A connection factory that always tries to connect to the same ip/port.
 * 
 * @author jrialland
 */
public class SingleConnectionFactory implements ConnectionFactory {

	private final ConnectionDescriptor desc;

	public SingleConnectionFactory(final InetSocketAddress sockAddr) {
		this(sockAddr.getAddress(), sockAddr.getPort());
	}

	public SingleConnectionFactory(final InetAddress host, final int port) {
		this("tcp://" + host + ":" + port);
	}

	public SingleConnectionFactory(final String descriptor) {
		desc = ConnectionDescriptor.makeConnDesc(descriptor);
	}

	public ISocket getConnection() {
		try {
			return desc.makeSocket();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseConnection(final ISocket socket) {
		try {
			socket.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
