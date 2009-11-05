/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jr.fastcgi.ConnectionFactory;
import net.jr.fastcgi.impl.FastCGIHandler;
import net.jr.fastcgi.impl.ServletRequestAdapter;
import net.jr.fastcgi.impl.ServletResponseAdapter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;

/**
 * Sample configuration :
 * 
 * <h3>web.xml</h3>
 * <pre><![CDATA[    <servlet>
        <servlet-name>jfastcgi</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>jfastcgi</servlet-name>
        <url-pattern>*.php</url-pattern>
    </servlet-mapping>

]]></pre>
 * 
 * <h3>Spring xml configuration</h3>
 * <pre><![CDATA[<bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
        <property name="mappings">
            <value>
                /&#42;/&#42;.php=fastCGIRequestHandler
            </value>
        </property>
    </bean>
    
    <bean id="fastCGIRequestHandler" class="net.jr.fastcgi.spring.RequestHandler">
    	<property name="connectionFactory" ref="connectionFactory" />
    </bean>
    
    <bean id="connectionFactory" class="net.jr.fastcgi.impl.SingleConnectionFactory">
    	 <constructor-arg value="localhost:9763"/>
    </bean>
]]></pre>
 * @author julien
 *
 */
public class RequestHandler implements HttpRequestHandler, InitializingBean {

	private ConnectionFactory connectionFactory = null;
	
	private FastCGIHandler fastCGIHandler = null;
	
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ServletRequestAdapter requestAdapter = new ServletRequestAdapter(
				request);
		ServletResponseAdapter responseAdapter = new ServletResponseAdapter(
				response);
		getFastCGIHandler().service(requestAdapter, responseAdapter);
	}

	public void afterPropertiesSet() throws Exception {
		Assert.isTrue(getFastCGIHandler() != null);
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
		if(getFastCGIHandler() == null)
		{
			setFastCGIHandler(new FastCGIHandler());
		}
		getFastCGIHandler().setConnectionFactory(connectionFactory);
	}

}
