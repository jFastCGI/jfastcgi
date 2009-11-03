package net.jr.fastcgi.impl;

import java.net.Socket;

import net.jr.fastcgi.ConnectionFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * A connection factory that handles multiple connections, using an underlying connection pool provided
 * by commons-pool. (http://commons.apache.org/pool/)
 * 
 * @author jrialland
 *
 */
public class PooledConnectionFactory implements ConnectionFactory {

	private ObjectPool pool;
	
	public PooledConnectionFactory(PoolableObjectFactory poolableObjectFactory)
	{
		this.pool = new GenericObjectPool(poolableObjectFactory);
	}
	
	/**
	 * get e connection from the pool.
	 */
	public Socket getConnection() {
		try
		{
			Socket s = (Socket)pool.borrowObject();
			return s;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * returns a connection to the pool.
	 * 
	 */
	public void releaseConnection(Socket socket) {
		try
		{
			pool.returnObject(socket);
		}
		catch(Exception e)
		{
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
