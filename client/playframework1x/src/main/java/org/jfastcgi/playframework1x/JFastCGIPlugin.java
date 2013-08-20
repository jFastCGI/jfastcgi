package org.jfastcgi.playframework1x;

import org.jfastcgi.client.FastCGIHandler;
import org.jfastcgi.client.FastCGIHandlerFactory;
import play.Play;
import play.PlayPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jFastCGI Plugin for PlayFramework 1.x (>= 1.2.6)
 */
public class JFastCGIPlugin extends PlayPlugin {
    /*

     jfastcgi.DEFAULT.server-address = 127.0.0.1:19000
     jfastcgi.DEFAULT.start-executable = "/usr/bin/php-fcgi -b 19000"
     ...
     jfastcgi.ALTERNATE.server-address = 127.0.0.1:19000
     jfastcgi.ALTERNATE.connection-factory = com.yourcompany.fastcgi.CustomConnectionFactory
     ...
     jfastcgi.ACLUSTERCONFIG.cluster-adresses = 127.0.0.1:19000,[::1]:20000,[::1]:21000
     ...
     jfastcgi.FILTERINGHEADERS.server-address = 127.0.0.1:19000
     jfastcgi.FILTERINGHEADERS.filtered-headers = Authorization;
     */
    private final Pattern configPattern = Pattern.compile(
            "^jfastcgi.(.*).(server-address|cluster-adresses|start-executable|connection-factory|filtered-headers|context-path|keep-alive)$"
    );

    private Properties _configuration = null;

    private Properties getConfiguration() {
        if (_configuration == null) {
            return Play.configuration;
        }
        else {
            return _configuration;
        }
    }


    Map<String, Map<String, String>> createConfigMap(Properties configuration) {
        Set<Object> keys = configuration.keySet();

        Map<String, Map<String, String>> configMap = new HashMap<String, Map<String, String>>();

        for (Object key : keys) {
            Matcher matcher = configPattern.matcher(key.toString());
            if (matcher.matches()) {
                String endpointName = matcher.group(1);
                String paramName = matcher.group(2);
                String value = configuration.getProperty((String) key);

                if (!configMap.containsKey(endpointName)) {
                    configMap.put(endpointName, new HashMap<String, String>());
                }

                configMap.get(endpointName).put(paramName, value);
            }
        }
        return configMap;
    }

    Map<String, FastCGIHandler> createHandlers(Map<String, Map<String, String>> configMap) {
        Map<String, FastCGIHandler> result = new HashMap<String, FastCGIHandler>();
        for (String endpointName : configMap.keySet()) {
            result.put(endpointName, FastCGIHandlerFactory.create(configMap.get(endpointName)));
        }
        return result;
    }

    @Override
    public void onApplicationStart() {
        Map<String, Map<String, String>> configMap = createConfigMap(getConfiguration());


        // iterate over all configuration objects
        // start connections

    }

    @Override
    public void onApplicationStop() {
        // iterate over all configuration objects
        // close connections
        // remove configuration objects from memory

    }


    public void setConfiguration(Properties properties) {
        this._configuration = properties;
    }

}
