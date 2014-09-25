package org.jfastcgi.client.juds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jfastcgi.client.ISocket;

import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocketClient;

public class UnixSocketWrapper implements ISocket {

	private final UnixDomainSocketClient s;

	public UnixSocketWrapper(final String path) throws IOException {
		this(new UnixDomainSocketClient(path, JUDS.SOCK_STREAM));
	}

	public UnixSocketWrapper(final UnixDomainSocketClient s) {
		this.s = s;
	}

	public InputStream getInputStream() throws IOException {
		return s.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return s.getOutputStream();
	}

	public void close() throws IOException {
		s.close();
	}

	public void setSoTimeout(final int timeout) {
		s.setTimeout(timeout);
	}

}
