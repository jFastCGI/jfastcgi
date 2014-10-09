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
package org.jfastcgi.utils.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

/**
 * reads a stream indefinitely, and put a line of log in the specified logger
 * each time an EOF is seen.
 *
 * @author jrialland
 */
public class StreamLogger implements Runnable {

    private final BufferedReader reader;

    private final Logger logger;

    public StreamLogger(final InputStream is, final Logger logger) {
        this.logger = logger;
        reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                logger.trace(line);
            }
        }
        catch (final IOException e) {
            logger.error("while logging stream", e);
        }
    }

}