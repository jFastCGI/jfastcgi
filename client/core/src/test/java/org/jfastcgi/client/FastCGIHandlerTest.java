package org.jfastcgi.client;

import org.jfastcgi.api.ConnectionFactory;
import org.jfastcgi.api.RequestAdapter;
import org.jfastcgi.api.ResponseAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FastCGIHandlerTest {

    private ByteArrayOutputStream outputStream;

    private ByteArrayInputStream inputStream;

    @Before
    public void setup() {
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream(new byte[]{});
    }


    protected ISocket makeISocket() throws IOException {
        ISocket iSocket = Mockito.mock(ISocket.class);
        Mockito.when(iSocket.getOutputStream()).thenReturn(outputStream);
        Mockito.when(iSocket.getInputStream()).thenReturn(inputStream);
        return iSocket;
    }

    protected ConnectionFactory makeConnectionFactory() throws IOException {
        final ISocket iSocket = makeISocket();
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        Mockito.when(connectionFactory.getConnection()).thenReturn(iSocket);
        return connectionFactory;
    }

    protected RequestAdapter makeRequest(final Map<String, String> headers) {
        RequestAdapter request = Mockito.mock(RequestAdapter.class);
        Mockito.when(request.getServletPath()).thenReturn("/fcgi");
        Mockito.when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));

        Mockito.when(request.getHeader(Mockito.anyString())).then(new Answer<String>() {
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object arg = invocationOnMock.getArguments()[0];
                String key = arg == null ? null : arg.toString();
                return headers.get(key);
            }
        });

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        return request;
    }

    /**
     * (Fixing https://github.com/jFastCGI/jfastcgi/issues/21)
     * <p>
     * Check that the HTTP_PROXY environment variable is not transmitted when the 'Proxy' header
     * is set in the request.
     *
     * @throws Exception
     */
    @Test
    public void testProxyHeader() throws Exception {

        FastCGIHandler fastCGIHandler = new FastCGIHandler();
        fastCGIHandler.setConnectionFactory(makeConnectionFactory());

        ResponseAdapter response = Mockito.mock(ResponseAdapter.class);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Proxy", "proxyValue");

        fastCGIHandler.service(makeRequest(headers), response);

        String s = new String(outputStream.toByteArray());
        Assert.assertTrue(s.contains("CGI_HTTP_PROXY")); //same fix as ruby, we change the name of the variable
    }
}
