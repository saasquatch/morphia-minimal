package dev.morphia.dao;


import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;


/**
 * Defines a basic interface for use in applications
 *
 * @param <T> The Java type serviced by this DAO
 * @param <K> The Key type used by the entity
 * @deprecated This interface poorly tracks Datastore's API.  Use Datastore directly or wrap in an application specific DAO
 */
@Deprecated
public interface DAO<T, K> {

    /**
     * Starts a query for this DAO entities type
     *
     * @return the query
     */
    Query<T> createQuery();

    /**
     * Starts a update-operations def for this DAO entities type
     *
     * @return a new empty UpdateOperations instance
     */
    UpdateOperations<T> createUpdateOperations();

    /**
     * @return the underlying datastore
     */
    Datastore getDatastore();

    /**
     * The type of entities for this DAO
     *
     * @return the entity class
     */
    Class<T> getEntityClass();
}
