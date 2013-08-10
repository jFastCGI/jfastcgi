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
package org.jfastcgi.portlet;

import org.jfastcgi.client.FastCGIHandler;
import org.jfastcgi.client.FastCGIHandlerFactory;
import org.jfastcgi.portlet.impl.PortletRequestAdapter;
import org.jfastcgi.portlet.impl.PortletResponseAdapter;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Porlet version is quite experimental, but functional.
 *
 * @author jrialland
 */
public class FastCGIPortlet extends GenericPortlet {

    private FastCGIHandler handler = new FastCGIHandler();

    @Override
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        Map<String, String> config = new TreeMap<String, String>();
        for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
            String value = portletConfig.getInitParameter(paramName);
            if (value != null) {
                config.put(paramName, getInitParameter(paramName));
            }
        }
        handler = FastCGIHandlerFactory.create(config);
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        handler.service(new PortletRequestAdapter(request),
                new PortletResponseAdapter(response));
    }

    @Override
    public void destroy() {
        super.destroy();
        handler.destroy();
    }
}