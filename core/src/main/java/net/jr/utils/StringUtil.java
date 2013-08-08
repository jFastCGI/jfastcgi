/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * String-related utility methods.
 * 
 * @author jrialland
 * 
 */
public final class StringUtil {

	public static <T> String arrayToString(String separator, T... array) {
		return collectionToString(separator, Arrays.asList(array));
	}

	public static String collectionToString(String separator, Collection<?> coll) {
		StringBuffer buf = new StringBuffer();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {

			Object item = it.next();
			if (item == null) {
				buf.append("null");
			} else {
				buf.append(item.toString());
			}

			if (it.hasNext()) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}
}
