package org.jfastcgi.client;

import java.io.IOException;

import org.jfastcgi.utils.LazyClassLoading;

public class UnixSocketConnectionDescriptor extends ConnectionDescriptor {

	private final String path;

	public UnixSocketConnectionDescriptor(final String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public ISocket makeSocket() throws IOException {
		try {
			return LazyClassLoading.newInstance("org.jfastcgi.client.juds.UnixSocketWrapper", path);
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}
}
