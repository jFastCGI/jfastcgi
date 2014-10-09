package org.jfastcgi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpConnectionDescriptor extends ConnectionDescriptor {

    private final InetAddress addr;

    private final int port;

    private static class TcpSocketWrapper implements ISocket {

        private final Socket s;

        public TcpSocketWrapper(final Socket s) {
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
            try {
                s.setSoTimeout(timeout);
            }
            catch (final SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public TcpConnectionDescriptor(final InetAddress addr, final int port) {
        this.addr = addr;
        this.port = port;
    }

    @Override
    public ISocket makeSocket() throws IOException {
        return new TcpSocketWrapper(new Socket(addr, port));
    }

    public InetAddress getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }
}
