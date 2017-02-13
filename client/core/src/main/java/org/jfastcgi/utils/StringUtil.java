/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developers.
 * Released under BSD License, see LICENSE_JRIALLAND.txt
 */
/*
 Copyright (c) 2013-2016 - the jFastCGI project developers.

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
package org.jfastcgi.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * String-related utility methods.
 *
 * @author jrialland
 */
public final class StringUtil {

    public static <T> String arrayToString(final String separator,
            final T... array) {
        return collectionToString(separator, Arrays.asList(array));
    }

    public static String collectionToString(final String separator,
            final Collection<?> coll) {
        final StringBuffer buf = new StringBuffer();
        final Iterator<?> it = coll.iterator();
        while (it.hasNext()) {

            final Object item = it.next();
            if (item == null) {
                buf.append("null");
            }
            else {
                buf.append(item.toString());
            }

            if (it.hasNext()) {
                buf.append(separator);
            }
        }
        return buf.toString();
    }
}
