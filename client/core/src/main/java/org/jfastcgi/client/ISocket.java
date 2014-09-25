package org.jfastcgi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ISocket {

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void close() throws IOException;

	void setSoTimeout(int timeout);
}
