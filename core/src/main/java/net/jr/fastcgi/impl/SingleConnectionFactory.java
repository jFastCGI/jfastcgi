/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jr.fastcgi.ConnectionFactory;

/**
 * A connection factory that always tries to connect to the same ip/port.
 * 
 * @author jrialland
 *
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

	public SingleConnectionFactory(String descriptor)
	{
		Matcher m = Pattern.compile("([^:]+):([1-9][0-9]*)$").matcher(descriptor.trim());
		if (m.matches()) {
			try {
				this.host = InetAddress.getByName(m.group(1));
				this.port = Integer.parseInt(m.group(2));
			}
			catch(Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		else throw new IllegalArgumentException("syntax error (required format is <host>:<port>) - "+descriptor);
	}

	public Socket getConnection() {
		try {
			return new Socket(host, port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseConnection(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
