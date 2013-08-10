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
package org.jfastcgi.servlet;

import org.jfastcgi.client.FastCGIHandler;
import org.jfastcgi.client.FastCGIHandlerFactory;
import org.jfastcgi.servlet.impl.ServletRequestAdapter;
import org.jfastcgi.servlet.impl.ServletResponseAdapter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jrialland
 */
public class FastCGIServlet extends HttpServlet {

    private static final long serialVersionUID = -8597795652806478718L;

    private FastCGIHandler handler = new FastCGIHandler();

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        Map<String, String> config = new TreeMap<String, String>();
        for (String paramName : FastCGIHandlerFactory.PARAM_NAMES) {
            String value = servletConfig.getInitParameter(paramName);
            if (value != null) {
                config.put(paramName, getInitParameter(paramName));
            }
        }
        handler = FastCGIHandlerFactory.create(config);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handler.service(new ServletRequestAdapter(getServletContext(), request), new ServletResponseAdapter(response));
    }

    @Override
    public void destroy() {
        super.destroy();
        handler.destroy();
    }

    public FastCGIHandler getHandler() {
        return handler;
    }

    public void setHandler(FastCGIHandler handler) {
        this.handler = handler;
    }
}
