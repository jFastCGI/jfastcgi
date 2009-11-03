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

public class PoolFactory extends BasePoolableObjectFactory {

	private static class ConnDesc {
		public InetAddress getAddr() {
			return addr;
		}

		public void setAddr(InetAddress addr) {
			this.addr = addr;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
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

	private List<ConnDesc> addresses = new ArrayList<ConnDesc>();

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

	@Override
	public Object makeObject() throws Exception {
		int index = random.nextInt(addresses.size() - 1);
		ConnDesc desc = addresses.get(index);
		return new Socket(desc.getAddr(), desc.getPort());
	}

	public void setAddress(String address) {
		this.addresses.add(makeConndesc(address));
	}

	public void setAddresses(List<String> addresses) {
		for (String s : addresses) {
			this.addresses.add(makeConndesc(s));
		}
	}
}
