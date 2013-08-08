/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi;

import java.net.Socket;

/**
 * interface that any service that can create / destroy connections to a fastcgi provider should implement.
 * 
 * @author jrialland
 *
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
	 * 
	 * Note : it doesn't mean that the socket should be closed at all, but notifies that this connection is no more
	 * needed for a particular request.
	 * For example, a pooling system could use this method to mark connection as "useable" for another request.
	 * 
	 * @param socket
	 */
	public void releaseConnection(Socket socket);
}
