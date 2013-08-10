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
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.net.Socket;

/**
 * A connection factory that handles multiple connections, using an underlying connection pool provided
 * by commons-pool. (http://commons.apache.org/pool/)
 *
 * @author jrialland
 */
public class PooledConnectionFactory implements ConnectionFactory {

    private ObjectPool pool;

    public PooledConnectionFactory(PoolableObjectFactory poolableObjectFactory) {
        this.pool = new GenericObjectPool(poolableObjectFactory);
    }

    /**
     * get e connection from the pool.
     */
    public Socket getConnection() {
        try {
            Socket s = (Socket) pool.borrowObject();
            return s;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * returns a connection to the pool.
     */
    public void releaseConnection(Socket socket) {
        try {
            pool.returnObject(socket);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectPool getPool() {
        return pool;
    }

    public void setPool(ObjectPool pool) {
        this.pool = pool;
    }
}
