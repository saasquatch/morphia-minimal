package dev.morphia.mapping.cache;

/**
 * Default implementation of cache factory, returning the default entity cache.
 */
public class DefaultEntityCacheFactory implements EntityCacheFactory {

    /**
     * Creates a new DefaultEntityCache
     *
     * @return the cache
     */
    @Override
	public EntityCache createCache() {
        return new DefaultEntityCache();
    }
}
