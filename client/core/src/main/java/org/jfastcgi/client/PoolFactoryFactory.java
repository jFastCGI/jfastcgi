package org.jfastcgi.client;

/**
 * A factory that creates &quot;PoolFactory&quot; objects.
 * <p/>
 * Has a setStaticPoolFactory() method to allow easy testing
 */
public class PoolFactoryFactory {

    /**
     * This static instance variable is used for testing
     */
    private static PoolFactory _poolFactory = null;

    /**
     * Returns a new PoolFactory as long as setStaticPoolFactory has not be
     * called with a non-null value.
     *
     * @return a new PoolFactory or the one provided by setStaticPoolFactory()
     */
    public static PoolFactory getOrCreatePoolFactory() {
        if (_poolFactory == null) {
            return new PoolFactory(); // do not set the _poolFactory variable,
            // we want new instances everytime.
        }
        else {
            return _poolFactory;
        }
    }

    public static void setStaticPoolFactory(final PoolFactory poolFactory) {
        _poolFactory = poolFactory;
    }
}
