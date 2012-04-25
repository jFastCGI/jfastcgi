/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * Implements PoolableObjectFactory so it creates tcp connections on demand using pool configuration.
 * 
 * @author jrialland
 *
 */
public class PoolFactory extends BasePoolableObjectFactory {

	/**
	 * embeds ip + port into a single structure.
	 * 
	 * @author jrialland
	 *
	 */
	private static class ConnDesc {
		public InetAddress getAddr() {
			return addr;
		}

		public int getPort() {
			return port;
		}

		private InetAddress addr;

		private int port;

		public ConnDesc(InetAddress addr, int port) {
			this.addr = addr;
			this.port = port;
		}

		@Override
		public String toString() {
			return addr.getHostName() + ":" + port;
		}
	}

	private Random random = new Random();

	/**
	 * List of configured host/port pairs.
	 * 
	 */
	private List<ConnDesc> addresses = new ArrayList<ConnDesc>();

	/**
	 * build a ConnDesc from a "host:port" string.
	 * @param s
	 * @return
	 */
	private static ConnDesc makeConndesc(String s) {
		Matcher m = Pattern.compile("([^:]+):([1-9][0-9]*)$").matcher(s);
		if (m.matches()) {
			try {
				InetAddress addr = InetAddress.getByName(m.group(1));
				int port = Integer.parseInt(m.group(2));
				return new ConnDesc(addr, port);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}
		} else
			throw new IllegalArgumentException(s);
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

	public void addAddress(String address)
	{
		this.addresses.add(makeConndesc(address));
	}
	
	public void addAdresses(Iterable<String> addresses)
	{
		for (String s : addresses) {
			this.addresses.add(makeConndesc(s));
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
