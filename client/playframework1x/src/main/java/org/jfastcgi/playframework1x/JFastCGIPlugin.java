package org.jfastcgi.playframework1x;

import play.Play;
import play.PlayPlugin;

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
    private final Pattern configPattern = Pattern.compile("^jfastcgi.(.*).(server-address|cluster-adresses|start-executable|connection-factory|filtered-headers|context-path)$");

    @Override
    public void onApplicationStart() {

        Set<Object> keys = Play.configuration.keySet();

        for (Object key : keys) {
            Matcher matcher = configPattern.matcher(key.toString());
            if(matcher.matches()){
                String endpointName = matcher.group(1);
                String paramName = matcher.group(2);
                String value = Play.configuration.getProperty((String) key);

                // store into configuration object
            }
        }

        // iterate over all configuration objects
        // start connections

    }

    @Override
    public void onApplicationStop() {
        // iterate over all configuration objects
        // close connections
        // remove configuration objects from memory

    }
}
