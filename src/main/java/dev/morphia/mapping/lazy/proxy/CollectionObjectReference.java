package dev.morphia.mapping.lazy.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.Key;

/**
 * A list of proxied elements
 *
 * @param <T> the type of the proxied items
 */
public class CollectionObjectReference<T> extends AbstractReference implements ProxiedEntityReferenceList {

    private static final long serialVersionUID = 1L;
    private final List<Key<?>> listOfKeys;

    /**
     * Creates a CollectionObjectReference
     *
     * @param type              the collection
     * @param referenceObjClass the Class of the referenced objects
     * @param ignoreMissing     ignore missing referenced documents
     * @param datastore         the Datastore to use when fetching this reference
     */
    public CollectionObjectReference(final Collection<T> type, final Class<T> referenceObjClass, final boolean ignoreMissing,
                                     final Datastore datastore) {

        super(datastore, referenceObjClass, ignoreMissing);

        object = type;
        listOfKeys = new ArrayList<>();
    }

    @Override
    //CHECKSTYLE:OFF
    public void __add(final Key key) {
        //CHECKSTYLE:ON
        listOfKeys.add(key);
    }

    @Override
    //CHECKSTYLE:OFF
    public void __addAll(final Collection<? extends Key<?>> keys) {
        //CHECKSTYLE:ON
        listOfKeys.addAll(keys);
    }

    //CHECKSTYLE:OFF
    @Override
    public List<Key<?>> __getKeysAsList() {
        return Collections.unmodifiableList(listOfKeys);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void beforeWriteObject() {
        if (__isFetched()) {
            syncKeys();
            ((Collection<T>) object).clear();
        }
    }
    //CHECKSTYLE:ON

    @Override
    protected synchronized Object fetch() {
        return null;
    }

    private void syncKeys() {
    }
}
