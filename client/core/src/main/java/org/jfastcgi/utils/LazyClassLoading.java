package org.jfastcgi.utils;

import java.lang.reflect.Constructor;

public class LazyClassLoading {

    /**
     * instanciates a new object given it's class name and constructor
     * arguments.
     *
     * It allows to defer class lookup, which is useful when the program may
     * rely on jars that are sometimes missing.
     *
     * @param className
     * @param args
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(final String className,
            final Object... args) throws ClassNotFoundException {
        Exception lastException = null;
        final Class<T> clazz = (Class<T>) Class.forName(className);
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == args.length) {
                try {
                    return (T) constructor.newInstance(args);
                }
                catch (final Exception e) {
                    lastException = e;
                }
            }
        }
        if (lastException != null) {
            throw new RuntimeException(lastException);
        }
        else {
            throw new IllegalArgumentException(
                    "Could not find any matching constructor for class "+className);
        }
    }
}
