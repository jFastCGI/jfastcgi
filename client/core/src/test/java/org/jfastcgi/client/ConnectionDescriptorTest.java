package org.jfastcgi.client;

import static org.fest.assertions.Assertions.assertThat;
import junit.framework.Assert;

import org.junit.Test;

public class ConnectionDescriptorTest {

    /**
     * Detect if test is running under the Travis CI environment.
     */
    private static boolean runsOnTravisCI() {
        String travis = System.getenv("TRAVIS");
        return travis != null && travis.equals(Boolean.TRUE.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_null() throws Exception {
        ConnectionDescriptor.makeConnDesc(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_onlyHostname() throws Exception {
        ConnectionDescriptor.makeConnDesc("somehost");
    }

    @Test
    public void testMakeConnDesc_unixSock() {
        final ConnectionDescriptor desc = ConnectionDescriptor
                .makeConnDesc("unix://var/run/application.sock");
        Assert.assertTrue(desc instanceof UnixSocketConnectionDescriptor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_unixSock_noFile() {
        ConnectionDescriptor.makeConnDesc("unix://");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_unixSock_invalidFile() {
        ConnectionDescriptor.makeConnDesc("unix://tes?t*");
    }

    @Test
    public void testMakeConnDesc_localhost_port1() throws Exception {
        final TcpConnectionDescriptor desc = (TcpConnectionDescriptor) ConnectionDescriptor
                .makeConnDesc("localhost:1");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(1);
    }

    @Test
    public void testMakeConnDesc_127001_9000() throws Exception {
        final TcpConnectionDescriptor desc = (TcpConnectionDescriptor) ConnectionDescriptor
                .makeConnDesc("127.0.0.1:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_fullAddress() throws Exception {
        final TcpConnectionDescriptor desc = (TcpConnectionDescriptor) ConnectionDescriptor
                .makeConnDesc("[0:0:0:0:0:0:0:1]:9000");
        assertThat(desc).isNotNull();
        if(!runsOnTravisCI()) {
            assertThat(desc.getAddr().isReachable(1)).isTrue();
        }
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_localhostShortcut() throws Exception {
        final TcpConnectionDescriptor desc = (TcpConnectionDescriptor) ConnectionDescriptor
                .makeConnDesc("[::1]:9000");
        assertThat(desc).isNotNull();
        if(!runsOnTravisCI()){
            assertThat(desc.getAddr().isReachable(1)).isTrue();
        }
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_arbitraryAddress() throws Exception {
        final TcpConnectionDescriptor desc = (TcpConnectionDescriptor) ConnectionDescriptor
                .makeConnDesc("[0000:0000:0000:0000:0000:0acf:0000:0001]:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().getHostAddress()).isEqualToIgnoringCase(
                "0:0:0:0:0:acf:0:1");
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_ipv6_invalidAddress() throws Exception {
        ConnectionDescriptor
                .makeConnDesc("[0000:0Z00:0000:0000:0000:0acf:0000:0001]:9000");
    }

}
