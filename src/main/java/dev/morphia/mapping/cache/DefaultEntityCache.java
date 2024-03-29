package dev.morphia.mapping.cache;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import dev.morphia.Key;

/**
 * This is the default EntityCache for Morphia
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultEntityCache implements EntityCache {

    private final Map entityMap = new ReferenceMap(ReferenceStrength.HARD, ReferenceStrength.WEAK);
    private final Map proxyMap = new ReferenceMap(ReferenceStrength.WEAK, ReferenceStrength.WEAK);
    private final Map<Key, Boolean> existenceMap = new HashMap<>();
    private final EntityCacheStatistics stats = new EntityCacheStatistics();

    @Override
    public Boolean exists(final Key<?> k) {
        if (entityMap.containsKey(k)) {
            stats.incHits();
            return true;
        }

        final Boolean b = existenceMap.get(k);
        if (b == null) {
            stats.incMisses();
        } else {
            stats.incHits();
        }
        return b;
    }

    @Override
    public void flush() {
        entityMap.clear();
        existenceMap.clear();
        proxyMap.clear();
        stats.reset();
    }

    @Override
    public <T> T getEntity(final Key<T> k) {
        final Object o = entityMap.get(k);
        if (o == null) {
            // System.out.println("miss entity " + k + ":" + this);
            stats.incMisses();
        } else {
            stats.incHits();
        }
        return (T) o;
    }

    @Override
    public <T> T getProxy(final Key<T> k) {
        final Object o = proxyMap.get(k);
        if (o == null) {
            stats.incMisses();
        } else {
            stats.incHits();
        }
        return (T) o;
    }

    @Override
    public void notifyExists(final Key<?> k, final boolean exists) {
        final Boolean put = existenceMap.put(k, exists);
        if (put == null || !put) {
            stats.incEntities();
        }
    }

    @Override
    public <T> void putEntity(final Key<T> k, final T t) {
        notifyExists(k, true); // already registers a write
        entityMap.put(k, t);
    }

    @Override
    public <T> void putProxy(final Key<T> k, final T t) {
        proxyMap.put(k, t);
        stats.incEntities();

    }

    @Override
    public EntityCacheStatistics stats() {
        return stats.copy();
    }

}
