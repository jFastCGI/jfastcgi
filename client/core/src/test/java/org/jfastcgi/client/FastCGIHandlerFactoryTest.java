package org.jfastcgi.client;


import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FastCGIHandlerFactoryTest {

    PoolFactory poolFactory;

    @Before
    public void setUp() throws Exception {
        poolFactory = mock(PoolFactory.class);
        FastCGIHandlerFactory.setPoolFactory(poolFactory);
    }


    @Test
    public void testCreate_simpleClusteredAddresses() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "127.0.0.1:9003");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9003");
    }

    @Test
    public void testCreate_twoClusteredAddresses() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "127.0.0.1:9000;127.0.0.1:9001");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9000");
        verify(poolFactory).addAddress("127.0.0.1:9001");
    }

    @Test
    public void testCreate_clusteredAddressesMultiLine() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "   127.0.0.1:9000;127.0.0.1:9001\n\t127.0.0.1:9002;\n   127.0.0.1:9003;127.0.0.1:9004\n[::1]:9005");
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
        Map<String, String> data = new HashMap<String, String>();
        data.put(FastCGIHandlerFactory.PARAM_CLUSTER_ADRESSES, "   127.0.0.1:9000\n127.0.0.1:9001\n127.0.0.1:9002\n127.0.0.1:9003\n127.0.0.1:9004\n[::1]:9005");
        FastCGIHandlerFactory.create(data);
        verify(poolFactory).addAddress("127.0.0.1:9000");
        verify(poolFactory).addAddress("127.0.0.1:9001");
        verify(poolFactory).addAddress("127.0.0.1:9002");
        verify(poolFactory).addAddress("127.0.0.1:9003");
        verify(poolFactory).addAddress("127.0.0.1:9004");
        verify(poolFactory).addAddress("[::1]:9005");
    }


}
