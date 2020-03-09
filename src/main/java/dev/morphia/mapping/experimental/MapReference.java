package dev.morphia.mapping.experimental;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.DBObject;

import dev.morphia.Datastore;
import dev.morphia.mapping.MappedClass;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;

/**
 * @param <T>
 * @morphia.internal
 */
public class MapReference<T> extends MorphiaReference<Map<String, T>> {
    private Map<String, Object> ids;
    private Map<String, T> values;
    private Map<String, List<Object>> collections = new HashMap<String, List<Object>>();

    /**
     * @morphia.internal
     */
    MapReference(final Datastore datastore, final MappedClass mappedClass, final Map<String, Object> ids) {
        super(datastore, mappedClass);
        Map<String, Object> unwrapped = ids;
        if (ids != null) {
            for (final Entry<String, Object> entry : ids.entrySet()) {
                CollectionReference.collate(datastore, collections, entry.getValue());
            }
        }

        this.ids = unwrapped;
    }

    MapReference(final Map<String, T> values) {
        this.values = values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Map<String, T> get() {
        if (values == null && ids != null) {
            values = new LinkedHashMap<String, T>();
            mergeReads();
        }
        return values;
    }

    private void mergeReads() {
        for (final Entry<String, List<Object>> entry : collections.entrySet()) {
            readFromSingleCollection(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void readFromSingleCollection(final String collection, final List<Object> collectionIds) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean isResolved() {
        return values != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object encode(final Mapper mapper, final Object value, final MappedField field) {
        if (isResolved()) {
            Map<String, Object> ids = new LinkedHashMap<String, Object>();
            for (final Entry<String, T> entry : get().entrySet()) {
                ids.put(entry.getKey(), wrapId(mapper, field, entry.getValue()));
            }
            return ids;
        } else {
            return null;
        }
    }

    /**
     * Decodes a document in to entities
     *
     * @param datastore   the datastore
     * @param mapper      the mapper
     * @param mappedField the MappedField
     * @param dbObject    the DBObject to decode
     * @return the entities
     */
    public static MapReference decode(final Datastore datastore, final Mapper mapper, final MappedField mappedField,
                                      final DBObject dbObject) {
        final Class subType = mappedField.getTypeParameters().get(0).getSubClass();

        final Map<String, Object> ids = (Map<String, Object>) mappedField.getDbObjectValue(dbObject);
        MapReference reference = null;
        if (ids != null) {
            reference = new MapReference(datastore, mapper.getMappedClass(subType), ids);
        }

        return reference;
    }
}
