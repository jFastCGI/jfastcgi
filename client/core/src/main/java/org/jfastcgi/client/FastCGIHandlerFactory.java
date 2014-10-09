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

import java.io.IOException;
import java.util.Map;

import org.jfastcgi.api.ConnectionFactory;
import org.jfastcgi.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps instanciating FastCGIHandlers using a properties-based
 * configuration.
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
     * should connections be kept alive? true | false
     */
    public final static String PARAM_KEEP_ALIVE = "keep-alive";

    /**
     * comma-separated list of http headers that will be filtered (i.e not
     * transmitted to the fastcgi responder) The list is case insensitive.
     */
    public final static String PARAM_FILTERED_HEADERS = "filtered-headers";

    public final static String[] PARAM_NAMES = new String[] {
        PARAM_SERVER_ADDRESS, PARAM_START_EXECUTABLE,
        PARAM_CONNECTION_FACTORY, PARAM_CLUSTER_ADRESSES,
        PARAM_FILTERED_HEADERS, PARAM_KEEP_ALIVE };

    private final static Logger LOGGER = LoggerFactory
            .getLogger(FastCGIHandlerFactory.class);

    public static FastCGIHandler create(final Map<String, String> config) {
        final FastCGIHandler handler = new FastCGIHandler();
        if (config.get(PARAM_SERVER_ADDRESS) != null) {
            getLog().info(
                    "configuring fastcgi servlet using default single connection handler");
            handler.setConnectionFactory(new SingleConnectionFactory(config
                    .get(PARAM_SERVER_ADDRESS)));
        }
        else if (config.get(PARAM_CONNECTION_FACTORY) != null) {
            final String className = config.get(PARAM_CONNECTION_FACTORY)
                    .trim();
            getLog().info(
                    "configuring fastCGI handler using custom class '"
                            + className + "'");
            handler.setConnectionFactory(buildConnectionFactoryForClass(className));
        }
        else if (config.get(PARAM_CLUSTER_ADRESSES) != null) {
            getLog().info(
                    "configuring fastCGI handler using the following adresses : ");
            final PoolFactory poolFactory = PoolFactoryFactory
                    .getOrCreatePoolFactory();
            for (final String addr : config.get(PARAM_CLUSTER_ADRESSES)
                    .replaceAll("[\n\t]", ";").replaceAll(" ", "").split(";")) {
                if (!addr.isEmpty()) {
                    getLog().info("  => " + addr);
                    poolFactory.addAddress(addr.trim());
                }
            }
            handler.setConnectionFactory(new PooledConnectionFactory(
                    poolFactory));// sorry for the confusion, everything seems
            // to be named 'factory'...
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot create fcgi handler : did you provide any configuration ?");
        }

        // handle filtered-headers param
        if (config.get(PARAM_FILTERED_HEADERS) != null) {
            final String[] filteredHeaders = config.get(PARAM_FILTERED_HEADERS)
                    .split(";");
            for (int i = 0; i < filteredHeaders.length; i++) {
                filteredHeaders[i] = filteredHeaders[i].trim();
            }

            if (getLog().isInfoEnabled()) {
                getLog().info(
                        "The following http headers will not be transmitted : ["
                                + StringUtil.arrayToString(", ",
                                        filteredHeaders) + "]");
            }
            handler.setFilteredHeaders(filteredHeaders);
        }

        if (config.get(PARAM_KEEP_ALIVE) != null) {
            final String value = config.get(PARAM_KEEP_ALIVE);
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
                handler.setKeepAlive(true);
            }
        }

        if (config.get(PARAM_START_EXECUTABLE) != null) {
            final String cmd = config.get(PARAM_START_EXECUTABLE);
            try {
                handler.startProcess(cmd);
            }
            catch (final IOException e) {
                getLog().error("while starting external process", e);
                throw new RuntimeException(e);
            }
        }

        return handler;
    }

    private static ConnectionFactory buildConnectionFactoryForClass(
            final String className) {
        try {
            return (ConnectionFactory) Class.forName(className).newInstance();
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Logger getLog() {
        return LOGGER;
    }

}
