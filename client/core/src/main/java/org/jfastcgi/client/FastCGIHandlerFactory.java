/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developers.
 * Released under BSD License, see LICENSE_JRIALLAND.txt
 */
/*
 Copyright (c) 2013 - the jFastCGI project developers.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.jfastcgi.client;

import org.jfastcgi.api.ConnectionFactory;
import org.jfastcgi.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class helps instanciating FastCGIHandlers using a properties-based configuration.
 *
 * @author jrialland
 */
public class FastCGIHandlerFactory {

    /**
     * Address of the fastcgi provider service to use.
     */
    public final static String PARAM_SERVER_ADDRESS = "server-address";

    /**
     * executable that should be launched as the servlet starts.
     */
    public final static String PARAM_START_EXECUTABLE = "start-executable";

    /**
     * user-provided class that will provide tcp connections.
     */
    public final static String PARAM_CONNECTION_FACTORY = "connection-factory";

    /**
     * comma-separated list of adresses when using seveal fastcgi endpoints.
     */
    public final static String PARAM_CLUSTER_ADRESSES = "cluster-adresses";

    /**
     * comma-separated list of http headers that will be filtered (i.e not transmitted to the fastcgi responder)
     * The list is case insensitive.
     */
    public final static String PARAM_FILTERED_HEADERS = "filtered-headers";

    public final static String[] PARAM_NAMES = new String[]{
            PARAM_SERVER_ADDRESS,
            PARAM_START_EXECUTABLE,
            PARAM_CONNECTION_FACTORY,
            PARAM_CLUSTER_ADRESSES,
            PARAM_FILTERED_HEADERS
    };

    private final static Logger LOGGER = LoggerFactory.getLogger(FastCGIHandlerFactory.class);

    public static FastCGIHandler create(Map<String, String> config) {
        FastCGIHandler handler = new FastCGIHandler();
        if (config.get(PARAM_SERVER_ADDRESS) != null) {
            getLog().info("configuring fastcgi servlet using default single connection handler");
            handler.setConnectionFactory(new SingleConnectionFactory(config.get(PARAM_SERVER_ADDRESS)));
        }
        else if (config.get(PARAM_CONNECTION_FACTORY) != null) {
            String className = config.get(PARAM_CONNECTION_FACTORY).trim();
            getLog().info("configuring fastCGI handler using custom class '" + className + "'");
            handler.setConnectionFactory(buildConnectionFactoryForClass(className));
        }
        else if (config.get(PARAM_CLUSTER_ADRESSES) != null) {
            PoolFactory factory = new PoolFactory();
            getLog().info("configuring fastCGI handler using the following adresses : ");
            for (String addr : config.get(PARAM_CLUSTER_ADRESSES).split(";")) {
                getLog().info("  => " + addr);
                factory.addAddress(addr.trim());
            }
            handler.setConnectionFactory(new PooledConnectionFactory(factory));//sorry for the confusion, everything seems to be named 'factory'...
        }
        else {
            throw new IllegalArgumentException("Cannot create fcgi handler : did you provide any configuration ?");
        }

        //handle filtered-headers param
        if (config.get(PARAM_FILTERED_HEADERS) != null) {
            String[] filteredHeaders = config.get(PARAM_FILTERED_HEADERS).split(";");
            for (int i = 0; i < filteredHeaders.length; i++) {
                filteredHeaders[i] = filteredHeaders[i].trim();
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("The following http headers will not be transmitted : [" + StringUtil.arrayToString(", ", filteredHeaders) + "]");
            }
            handler.setFilteredHeaders(filteredHeaders);
        }

        return handler;
    }

    private static ConnectionFactory buildConnectionFactoryForClass(String className) {
        try {
            return (ConnectionFactory) Class.forName(className).newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Logger getLog() {
        return LOGGER;
    }
}
