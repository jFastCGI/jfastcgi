package org.jfastcgi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocketClient;

public class UnixSocketConnectionDescriptor extends ConnectionDescriptor {

	private final String path;

	private static class UnixSocketWrapper implements ISocket {

		private final UnixDomainSocketClient s;

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

	public UnixSocketConnectionDescriptor(final String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public ISocket makeSocket() throws IOException {
		return new UnixSocketWrapper(new UnixDomainSocketClient(path, JUDS.SOCK_STREAM));
	}
}
