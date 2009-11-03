package net.jr.utils.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;

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