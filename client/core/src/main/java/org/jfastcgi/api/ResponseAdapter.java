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
package org.jfastcgi.api;

import java.io.OutputStream;

/**
 * / part of the portlet / servlet response interface used by FastCGIHandler.
 * Allows to use the same code for both portlet and servlet environment.
 *
 * @author jrialland
 */
public interface ResponseAdapter {

    public void sendError(int errorCode);

    public void setStatus(int statusCode);

    public void sendRedirect(String targetUrl);

    public void addHeader(String key, String value);

    public OutputStream getOutputStream();
}
