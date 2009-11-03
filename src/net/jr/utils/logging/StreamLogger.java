/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.utils.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;

/**
 * reads a stream indefinitely, and put a line of log in the specificied logger each time an EOF is seen.
 * 
 * @author jrialland
 *
 */
public class StreamLogger implements Runnable {

	private BufferedReader reader;

	private Log log;

	public StreamLogger(InputStream is, Log log) {
		reader = new BufferedReader(new InputStreamReader(is));
	}

	public void run() {
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				log.trace(line);
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

}