package org.jfastcgi.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ConnectionDescriptor {

	private static final String ERR_MSG = "Invalid address (%) -- Try something like tcp://localhost:9000 or unix:///var/run/app.sock";
	
    private final static Pattern PATTERN_HOSTNAME_PORT = Pattern
            .compile("([^:]+):([1-9][0-9]*)$");

    private final static Pattern PATTERN_IPV6_PORT = Pattern
            .compile("\\[((?:(?:(?:[0-9a-fA-F]{0,4}):){2,7})(?:[0-9a-fA-F]{1,4}))\\]:([1-9][0-9]*)");

    public static ConnectionDescriptor makeConnDesc(String address) {
        if (address == null || address.trim().length() == 0) {
            throw new IllegalArgumentException(String.format(ERR_MSG, "The given address is empty"));
        }

        // address used to look like <host>:<port>. now that we switched to an
        // url-like syntax, we handle those legacy adresses like tcp:// ones
        address = address.replaceFirst("^tcp://", "");

        if (address.startsWith("unix://")) {
            final String path = address.substring("unix://".length()).trim();
            // check if the file name is valid
            if (path.isEmpty()) {
                throw new IllegalArgumentException(
                        "Unix socket file name is empty.");
            }
            final File f = new File(path);
            if (!f.getAbsolutePath().matches("^[^*&%\\s]+$")) {
                throw new IllegalArgumentException(String.format(ERR_MSG,"Not a regular file name : "
                        + f.getAbsolutePath()));
            }
            return new UnixSocketConnectionDescriptor(path);
        }

        final Matcher ipv4Matcher = PATTERN_HOSTNAME_PORT.matcher(address);
        final Matcher ipv6Matcher = PATTERN_IPV6_PORT.matcher(address);
        if (ipv4Matcher.matches()) {
            try {
                final InetAddress addr = InetAddress.getByName(ipv4Matcher
                        .group(1));
                final int port = Integer.parseInt(ipv4Matcher.group(2));
                return new TcpConnectionDescriptor(addr, port);
            }
            catch (final UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else if (ipv6Matcher.matches()) {
            try {
                final InetAddress addr = InetAddress.getByName(ipv6Matcher
                        .group(1));
                final int port = Integer.parseInt(ipv6Matcher.group(2));
                return new TcpConnectionDescriptor(addr, port);
            }
            catch (final UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            throw new IllegalArgumentException(String.format(ERR_MSG, "Not a valid address"));
        }
    }

    public abstract ISocket makeSocket() throws IOException;
}
