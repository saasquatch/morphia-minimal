package dev.morphia.dao;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.QueryImpl;
import dev.morphia.query.UpdateOperations;

/**
 * @param <T> the type of the entity
 * @param <K> the type of the key
 * @author Olafur Gauti Gudmundsson
 * @author Scott Hernandez
 * @deprecated This interface poorly tracks Datastore's API.  Use Datastore directly or wrap in an application specific DAO
 */
@Deprecated
public class BasicDAO<T, K> implements DAO<T, K> {
    //CHECKSTYLE:OFF
    /**
     * @deprecated use {@link #getEntityClass()}
     */
    @Deprecated
    protected Class<T> entityClazz;
    /**
     * @deprecated use {@link #getDatastore()}
     */
    @Deprecated
    protected dev.morphia.DatastoreImpl ds;
    //CHECKSTYLE:ON

    /**
     * Create a new BasicDAO
     *
     * @param entityClass the class of the POJO you want to persist using this DAO
     * @param mongoClient the representations of the connection to a MongoDB instance
     * @param morphia     a Morphia instance
     * @param dbName      the name of the database
     */
    public BasicDAO(final Class<T> entityClass, final Morphia morphia, final String dbName) {
        initDS(morphia, dbName);
        initType(entityClass);
    }

    /**
     * Create a new BasicDAO
     *
     * @param entityClass the class of the POJO you want to persist using this DAO
     * @param ds          the Datastore which gives access to the MongoDB instance for this DAO
     */
    public BasicDAO(final Class<T> entityClass, final Datastore ds) {
        this.ds = (dev.morphia.DatastoreImpl) ds;
        initType(entityClass);
    }

    /**
     * Only calls this from your derived class when you explicitly declare the generic types with concrete classes
     * <p/>
     * {@code class MyDao extends DAO<MyEntity, String>}
     *
     * @param mongoClient the representations of the connection to a MongoDB instance
     * @param morphia     a Morphia instance
     * @param dbName      the name of the database
     */
    @SuppressWarnings("unchecked")
    protected BasicDAO(final Morphia morphia, final String dbName) {
        initDS(morphia, dbName);
        initType(((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
    }

    @SuppressWarnings("unchecked")
    protected BasicDAO(final Datastore ds) {
        this.ds = (dev.morphia.DatastoreImpl) ds;
        initType(((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
    }

    @Override
    public Query<T> createQuery() {
    	return new QueryImpl<>(getEntityClass(), getDatastore());
    }

    @Override
    public UpdateOperations<T> createUpdateOperations() {
        return ds.createUpdateOperations(entityClazz);
    }

    /* (non-Javadoc)
     * @see dev.morphia.DAO#getDatastore()
     */
    @Override
    public Datastore getDatastore() {
        return ds;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClazz;
    }

    /**
     * @return the Datastore used by this DAO
     * @deprecated use {@link #getDatastore()}
     */
    @Deprecated
    public dev.morphia.DatastoreImpl getDs() {
        return ds;
    }

    /**
     * @return the entity class
     * @deprecated use {@link #getEntityClass()} instead
     */
    @Deprecated
    public Class<T> getEntityClazz() {
        return entityClazz;
    }

    protected void initDS(final Morphia mor, final String db) {
        ds = (dev.morphia.DatastoreImpl) mor.createDatastore(db);
    }

    protected void initType(final Class<T> type) {
        entityClazz = type;
        ds.getMapper().addMappedClass(type);
    }

    /**
     * Converts from a List<Key> to their id values
     */
    protected List<?> keysToIds(final List<Key<T>> keys) {
        final List<Object> ids = new ArrayList<>(keys.size() * 2);
        for (final Key<T> key : keys) {
            ids.add(key.getId());
        }
        return ids;
    }

}
