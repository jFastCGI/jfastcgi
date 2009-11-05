/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.jr.fastcgi.ConnectionFactory;

/**
 * This class helps instanciating FastCGIHandlers using a properties-based configuration.
 * 
 * 
 * @author jrialland
 *
 */
public class FastCGIHandlerFactory {

	private static Log LOG = LogFactory.getLog(FastCGIHandlerFactory.class);
	
	/**
	 * Address of the fastcgi provider service to use.
	 */
	public static String PARAM_SERVER_ADDRESS = "server-address";
	
	/**
	 * executable that should be launched as the servlet starts.
	 */
	public static String PARAM_START_EXECUTABLE = "start-executable";
	
	/**
	 * user-provided class that will provide tcp connections.
	 */
	public static String PARAM_CONNECTION_FACTORY = "connection-factory";
	
	/**
	 * comma-separated list of adresses when using seveal fastcgi endpoints.
	 */
	public static String PARAM_CLUSTER_ADRESSES = "cluster-adresses";

	public static String[] PARAM_NAMES = new String[]{
		PARAM_SERVER_ADDRESS,
		PARAM_START_EXECUTABLE,
		PARAM_CONNECTION_FACTORY,
		PARAM_CLUSTER_ADRESSES
	};
	
	public static FastCGIHandler create(Map<String, String> config)
	{
		FastCGIHandler handler = new FastCGIHandler();
		if(config.get(PARAM_SERVER_ADDRESS) != null)
		{
			LOG.info("configuring fastCGI handler using a single connection -based policy");
			handler.setConnectionFactory(new SingleConnectionFactory(config.get(PARAM_SERVER_ADDRESS)));
		}
		else if(config.get(PARAM_CONNECTION_FACTORY) != null)
		{
			String className = config.get(PARAM_CONNECTION_FACTORY).trim(); 
			LOG.info("configuring fastCGI handler using custom class '"+className+"'");
			handler.setConnectionFactory(buildConnectionFactoryForClass(className));
		}
		else if(config.get(PARAM_CLUSTER_ADRESSES) != null)
		{
			PoolFactory factory = new PoolFactory();
			LOG.info("configuring fastCGI handler using the following adresses : ");
			for(String addr : config.get(PARAM_CLUSTER_ADRESSES).split(";"))
			{
				LOG.info("  => "+addr);
				factory.addAddress(addr.trim());
			}
			handler.setConnectionFactory(new PooledConnectionFactory(factory));//sorry for the confusion, everything seems to be named 'factory'...
		}
		else throw new IllegalArgumentException("Cannot create fcgi handler : did you provide any configuration ?");
		
		return handler;
	}
	
	private static ConnectionFactory buildConnectionFactoryForClass(String className)
	{
		try
		{
			return (ConnectionFactory)Class.forName(className).newInstance();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
