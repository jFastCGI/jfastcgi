/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package org.jfastcgi.fastcgi.spring;

import org.jfastcgi.fastcgi.ConnectionFactory;
import org.jfastcgi.fastcgi.impl.FastCGIHandler;
import org.jfastcgi.fastcgi.impl.ServletRequestAdapter;
import org.jfastcgi.fastcgi.impl.ServletResponseAdapter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sample configuration :
 * <p/>
 * <h3>web.xml</h3>
 * <p/>
 * <pre>
 * &lt;servlet&gt;
 *         &lt;servlet-name&gt;jfastcgi&lt;/servlet-name&gt;
 *         &lt;servlet-class&gt;org.springframework.web.servlet.DispatcherServlet&lt;/servlet-class&gt;
 *         &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *     &lt;/servlet&gt;
 *
 *     &lt;servlet-mapping&gt;
 *         &lt;servlet-name&gt;jfastcgi&lt;/servlet-name&gt;
 *         &lt;url-pattern&gt;*.php&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 * </pre>
 * <p/>
 * <h3>Spring xml configuration</h3>
 * <p/>
 * <pre>
 * &lt;bean class=&quot;org.springframework.web.servlet.handler.SimpleUrlHandlerMapping&quot;&gt;
 *         &lt;property name=&quot;mappings&quot;&gt;
 *             &lt;value&gt;
 *                 /&amp;#42;/&amp;#42;.php=fastCGIRequestHandler
 *             &lt;/value&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id=&quot;fastCGIRequestHandler&quot; class=&quot;net.jr.fastcgi.spring.RequestHandler&quot;&gt;
 *     &lt;property name=&quot;connectionFactory&quot; ref=&quot;connectionFactory&quot; /&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id=&quot;connectionFactory&quot; class=&quot;net.jr.fastcgi.impl.SingleConnectionFactory&quot;&gt;
 *     &lt;constructor-arg value=&quot;localhost:9763&quot;/&gt;
 *     &lt;/bean&gt;
 * </pre>
 *
 * @author julien
 */
public class RequestHandler implements HttpRequestHandler, ServletContextAware, InitializingBean, DisposableBean {

    private ServletContext servletContext;

    private ConnectionFactory connectionFactory = null;

    private FastCGIHandler fastCGIHandler = null;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(servletContext, "servletContext is null !");
        Assert.isTrue(getFastCGIHandler() != null, "connectionFactory or fastCgiHandler property should be set.");
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletRequestAdapter requestAdapter = new ServletRequestAdapter(servletContext, request);
        ServletResponseAdapter responseAdapter = new ServletResponseAdapter(response);
        getFastCGIHandler().service(requestAdapter, responseAdapter);
    }

    public FastCGIHandler getFastCGIHandler() {
        return fastCGIHandler;
    }

    public void setFastCGIHandler(FastCGIHandler fastCGIHandler) {
        this.fastCGIHandler = fastCGIHandler;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        if (getFastCGIHandler() == null) {
            setFastCGIHandler(new FastCGIHandler());
        }
        getFastCGIHandler().setConnectionFactory(connectionFactory);
    }

    public void destroy() throws Exception {
        if (fastCGIHandler != null) {
            fastCGIHandler.destroy();
        }
    }
}
