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
package org.jfastcgi.fastcgi;

import java.net.Socket;

/**
 * interface that any service that can create / destroy connections to a fastcgi provider should implement.
 *
 * @author jrialland
 */
public interface ConnectionFactory {

    /**
     * Called when a connection is needed.
     *
     * @return
     */
    public Socket getConnection();

    /**
     * Called  when a connection is released (not needed anymore)
     * <p/>
     * Note : it doesn't mean that the socket should be closed at all, but notifies that this connection is no more
     * needed for a particular request.
     * For example, a pooling system could use this method to mark connection as "useable" for another request.
     *
     * @param socket
     */
    public void releaseConnection(Socket socket);
}
