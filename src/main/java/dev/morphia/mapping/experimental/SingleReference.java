package dev.morphia.mapping.experimental;

import com.mongodb.DBObject;
import dev.morphia.Datastore;
import dev.morphia.mapping.MappedClass;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;

/**
 * @param <T>
 * @morphia.internal
 */
@SuppressWarnings("deprecation")
public class SingleReference<T> extends MorphiaReference<T> {
    private Object id;
    private T value;

    /**
     * @morphia.internal
     */
    SingleReference(final Datastore datastore, final MappedClass mappedClass, final Object id) {
        super(datastore, mappedClass);
        this.id = id;
    }

    SingleReference(final T value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
    	return null;
    }

    Query<?> buildQuery() {
        return null;
    }

    Object getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean isResolved() {
        return value != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object encode(final Mapper mapper, final Object value, final MappedField optionalExtraInfo) {
        if (isResolved()) {
            return wrapId(mapper, optionalExtraInfo, get());
        } else {
            return null;
        }

    }

    /**
     * Decodes a document in to an entity
     * @param datastore the datastore
     * @param mapper the mapper
     * @param mappedField the MappedField
     * @param paramType the type of the underlying entity
     * @param dbObject the DBObject to decode
     * @return the entity
     */
    public static MorphiaReference<?> decode(final Datastore datastore,
                                             final Mapper mapper,
                                             final MappedField mappedField,
                                             final Class paramType, final DBObject dbObject) {
        final MappedClass mappedClass = mapper.getMappedClass(paramType);
        Object id = dbObject.get(mappedField.getMappedFieldName());

        return new SingleReference(datastore, mappedClass, id);
    }

}
