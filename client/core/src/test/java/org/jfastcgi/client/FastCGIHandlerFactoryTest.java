package org.jfastcgi.client;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class FastCGIHandlerFactoryTest {

    PoolFactory poolFactory;

    @Before
    public void setUp() throws Exception {
        poolFactory = mock(PoolFactory.class);
        PoolFactoryFactory.setStaticPoolFactory(poolFactory);
    }

    @Test
    public void testCreate_simpleClusteredAddresses() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "127.0.0.1:9003");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9003");
    }

    @Test
    public void testCreate_twoClusteredAddresses() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES,
                "127.0.0.1:9000;127.0.0.1:9001");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9000");
        verify(poolFactory).addAddress("127.0.0.1:9001");
    }

    @Test
    public void testCreate_clusteredAddressesMultiLine() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put(
                FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES,
                "   127.0.0.1:9000;127.0.0.1:9001\n\t127.0.0.1:9002;\n   127.0.0.1:9003;127.0.0.1:9004\n[::1]:9005");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9000");
        verify(poolFactory).addAddress("127.0.0.1:9001");
        verify(poolFactory).addAddress("127.0.0.1:9002");
        verify(poolFactory).addAddress("127.0.0.1:9003");
        verify(poolFactory).addAddress("127.0.0.1:9004");
        verify(poolFactory).addAddress("[::1]:9005");
    }

    @Test
    public void testCreate_clusteredAddressesJustMultiLine() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put(
                FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES,
                "   127.0.0.1:9000\n127.0.0.1:9001\n127.0.0.1:9002\n127.0.0.1:9003\n127.0.0.1:9004\n[::1]:9005");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9000");
        verify(poolFactory).addAddress("127.0.0.1:9001");
        verify(poolFactory).addAddress("127.0.0.1:9002");
        verify(poolFactory).addAddress("127.0.0.1:9003");
        verify(poolFactory).addAddress("127.0.0.1:9004");
        verify(poolFactory).addAddress("[::1]:9005");
    }

    @Test
    public void testCreate_parseKeepAlive() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "127.0.0.1:9003");
        final FastCGIHandler handlerWithoutKeepAlive = FastCGIHandlerFactory
                .create(data);
        verify(poolFactory).addAddress("127.0.0.1:9003");
        assertThat(handlerWithoutKeepAlive).isNotNull();
        assertThat(handlerWithoutKeepAlive.isKeepAlive()).isFalse();

        data.put(FastCGIHandlerFactory.PARAM_KEEP_ALIVE, "true");
        final FastCGIHandler handlerWithKeepAlive = FastCGIHandlerFactory
                .create(data);
        assertThat(handlerWithKeepAlive).isNotNull();
        assertThat(handlerWithKeepAlive.isKeepAlive()).isTrue();
    }
}
