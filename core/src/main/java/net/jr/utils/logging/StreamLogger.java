/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.utils.logging;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * reads a stream indefinitely, and put a line of log in the specified logger
 * each time an EOF is seen.
 *
 * @author jrialland
 */
public class StreamLogger implements Runnable {

    private BufferedReader reader;

    private Logger logger;

    public StreamLogger(InputStream is, Logger logger) {
        this.logger = logger;
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                logger.trace(line);
            }
        }
        catch (IOException e) {
            logger.error("while logging stream", e);
        }
    }

}