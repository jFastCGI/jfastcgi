package org.jfastcgi.playframework1x;

import org.jfastcgi.client.FastCGIHandler;
import org.junit.Before;
import org.junit.Test;
import play.Play;

import java.util.Map;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for the JFastCGIPlugin for PlayFramework 1x
 */
public class JFastCGIPluginTest {
    Properties conf;
    @Before
    public void setUp() throws Exception {

        conf = new Properties();
        conf.setProperty("jfastcgi.DEFAULT.server-address","127.0.0.1:19000");
        conf.setProperty("jfastcgi.DEFAULT.start-executable","/usr/bin/php-fcgi -b 19000");
        conf.setProperty("jfastcgi.ALTERNATE.server-address","127.0.0.1:19000");
        conf.setProperty("jfastcgi.ALTERNATE.connection-factory","com.yourcompany.fastcgi.CustomConnectionFactory");
        conf.setProperty("jfastcgi.ACLUSTERCONFIG.cluster-adresses","127.0.0.1:19000;[::1]:20000;[::1]:21000");
        conf.setProperty("jfastcgi.FILTERINGHEADERS.server-address","127.0.0.1:19000");
        conf.setProperty("jfastcgi.FILTERINGHEADERS.filtered-headers","Authorization;");
        conf.setProperty("jfastcgi.FILTERINGHEADERS.keep-alive","false");
        conf.setProperty("jfastcgi.CONTEXTOVERRIDEEXAMPLE.server-address","127.0.0.1:19000");
        conf.setProperty("jfastcgi.CONTEXTOVERRIDEEXAMPLE.context-path","/someapp");
        conf.setProperty("jfastcgi.CONTEXTOVERRIDEEXAMPLE.keep-alive","true");
    }


    @Test
    public void testCreateConfigMap() throws Exception {
        JFastCGIPlugin plugin = new JFastCGIPlugin();
        plugin.setConfiguration(conf);

        Map<String, Map<String, String>> configMap = plugin.createConfigMap(conf);

        assertThat(configMap).isNotNull();
        assertThat(configMap).hasSize(5);
        assertThat(configMap.containsKey("DEFAULT"));
        assertThat(configMap.get("DEFAULT")).hasSize(2);
        assertThat(configMap.get("ACLUSTERCONFIG")).hasSize(1);
        assertThat(configMap.get("FILTERINGHEADERS")).hasSize(3);
        assertThat(configMap.get("CONTEXTOVERRIDEEXAMPLE")).hasSize(3);
    }

    @Test
    public void testCreateHandlers() throws Exception {
        JFastCGIPlugin plugin = new JFastCGIPlugin();
        plugin.setConfiguration(conf);

        Map<String, Map<String, String>> configMap = plugin.createConfigMap(conf);
        Map<String, FastCGIHandler> handlers = plugin.createHandlers(configMap);

        assertThat(handlers).isNotNull().hasSize(5);


    }
}
