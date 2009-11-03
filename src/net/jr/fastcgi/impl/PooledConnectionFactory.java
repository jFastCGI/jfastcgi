package net.jr.fastcgi.impl;

import java.net.Socket;

import net.jr.fastcgi.ConnectionFactory;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class PooledConnectionFactory implements ConnectionFactory {

	private ObjectPool pool;
	
	public PooledConnectionFactory(ObjectPool pool)
	{
		this.pool = pool;
	}
	
	public PooledConnectionFactory()
	{
		this(new GenericObjectPool());
	}
	
	
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
}
