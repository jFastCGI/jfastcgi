package org.jfastcgi.fastcgi.impl;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PoolFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_null() throws Exception {
        PoolFactory.makeConnDesc(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_onlyHostname() throws Exception {
        PoolFactory.makeConnDesc("somehost");
    }

    @Test
    public void testMakeConnDesc_localhost_port1() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("localhost:1");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(1);
    }

    @Test
    public void testMakeConnDesc_127001_9000() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("127.0.0.1:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_fullAddress() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("[0:0:0:0:0:0:0:1]:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_localhostShortcut() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("[::1]:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().isReachable(1)).isTrue();
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test
    public void testMakeConnDesc_ipv6_arbitraryAddress() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("[0000:0000:0000:0000:0000:0acf:0000:0001]:9000");
        assertThat(desc).isNotNull();
        assertThat(desc.getAddr().getHostAddress()).isEqualToIgnoringCase("0:0:0:0:0:acf:0:1");
        assertThat(desc.getPort()).isEqualTo(9000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeConnDesc_ipv6_invalidAddress() throws Exception {
        PoolFactory.ConnectionDescriptor desc = PoolFactory.makeConnDesc("[0000:0Z00:0000:0000:0000:0acf:0000:0001]:9000");
    }

}
