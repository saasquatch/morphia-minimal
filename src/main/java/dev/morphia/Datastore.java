package dev.morphia;


import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;

import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;
import dev.morphia.query.QueryFactory;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.UpdateResults;


/**
 * Datastore interface to get/delete/save objects
 *
 * @author Scott Hernandez
 */
public interface Datastore {

    /**
     * Returns a new query bound to the collection (a specific {@link DBCollection})
     *
     * @param collection The collection to query
     * @param <T>        the type of the query
     * @return the query
     */
    <T> Query<T> createQuery(Class<T> collection);

    /**
     * The builder for all update operations
     *
     * @param clazz the type to update
     * @param <T>   the type to update
     * @return the new UpdateOperations instance
     */
    <T> UpdateOperations<T> createUpdateOperations(Class<T> clazz);

    /**
     * @return the MongoDatabase used by this DataStore
     * @since 1.5
     * @morphia.internal
     */
    MongoDatabase getDatabase();

    /**
     * Get the underlying MongoClient that allows connection to the MongoDB instance being used.
     *
     * @return the MongoClient being used by this datastore.
     * @deprecated no replacement is planned
     */
    @Deprecated
    MongoClient getMongo();

    /**
     * @return the current {@link QueryFactory}.
     * @see QueryFactory
     */
    QueryFactory getQueryFactory();

    /**
     * Replaces the current {@link QueryFactory} with the given value.
     *
     * @param queryFactory the QueryFactory to use
     * @see QueryFactory
     */
    void setQueryFactory(QueryFactory queryFactory);

    /**
     * Work as if you did an update with each field in the entity doing a $set; Only at the top level of the entity.
     *
     * @param entity the entity to merge back in to the database
     * @param <T>    the type of the entity
     * @return the key of the entity
     */
    <T> Key<T> merge(T entity);

    /**
     * Work as if you did an update with each field in the entity doing a $set; Only at the top level of the entity.
     *
     * @param entity the entity to merge back in to the database
     * @param <T>    the type of the entity
     * @param wc     the WriteConcern to use
     * @return the key of the entity
     */
    <T> Key<T> merge(T entity, WriteConcern wc);

    /**
     * Returns a new query based on the example object
     *
     * @param example the example entity to use when creating the query
     * @param <T>     the type of the entity
     * @return the query
     */
    <T> Query<T> queryByExample(T example);

    /**
     * Saves the entities (Objects) and updates the @Id field
     *
     * <i>The return type will change in 2.0</i>
     *
     * @param entities the entities to save
     * @param <T>      the type of the entity
     * @return the keys of the entities
     */
    <T> Iterable<Key<T>> save(Iterable<T> entities);

    /**
     * Saves the entities (Objects) and updates the @Id field, with the WriteConcern
     *
     * @param entities the entities to save
     * @param <T>      the type of the entity
     * @param wc       the WriteConcern to use
     * @return the keys of the entities
     * @deprecated use {@link #save(Iterable, InsertOptions)} instead
     */
    @Deprecated
    <T> Iterable<Key<T>> save(Iterable<T> entities, WriteConcern wc);

    /**
     * Saves the entities (Objects) and updates the @Id field, with the WriteConcern
     *
     * <i>The return type will change in 2.0</i>
     *
     * @param entities the entities to save
     * @param <T>      the type of the entity
     * @param options  the options to apply to the save operation
     * @return the keys of the entities
     */
    <T> Iterable<Key<T>> save(Iterable<T> entities, InsertOptions options);

    /**
     * Saves the entities (Objects) and updates the @Id field
     *
     * @param entities the entities to save
     * @param <T>      the type of the entity
     * @return the keys of the entities
     * @deprecated use {@link #save(Iterable, InsertOptions)} instead
     */
    @Deprecated
    <T> Iterable<Key<T>> save(T... entities);

    /**
     * Saves an entity (Object) and updates the @Id field
     *
     * <i>The return type will change in 2.0</i>
     *
     * @param entity the entity to save
     * @param <T>    the type of the entity
     * @return the keys of the entity
     */
    <T> Key<T> save(T entity);

    /**
     * Saves an entity (Object) and updates the @Id field, with the WriteConcern
     *
     * @param entity the entity to save
     * @param wc     the WriteConcern to use
     * @param <T>    the type of the entity
     * @return the keys of the entity
     * @deprecated use {@link #save(Object, InsertOptions)} instead
     */
    @Deprecated
    <T> Key<T> save(T entity, WriteConcern wc);

    /**
     * Saves an entity (Object) and updates the @Id field
     *
     * <i>The return type will change in 2.0</i>
     *
     * @param entity  the entity to save
     * @param options the options to apply to the save operation
     * @param <T>     the type of the entity
     * @return the keys of the entity
     */
    <T> Key<T> save(T entity, InsertOptions options);

    /**
     * Updates an entity with the operations; this is an atomic operation
     *
     * @param entity     the entity to update
     * @param operations the update operations to perform
     * @param <T>        the type of the entity
     * @return the update results
     * @see UpdateResults
     * @deprecated use {@link #update(Query, UpdateOperations)} instead
     */
    @Deprecated
    <T> UpdateResults update(T entity, UpdateOperations<T> operations);

    /**
     * Updates an entity with the operations; this is an atomic operation
     *
     * @param key        the key of entity to update
     * @param operations the update operations to perform
     * @param <T>        the type of the entity
     * @return the update results
     * @see UpdateResults
     * @deprecated use {@link #update(Query, UpdateOperations)} instead
     */
    @Deprecated
    <T> UpdateResults update(Key<T> key, UpdateOperations<T> operations);


    /**
     * Updates all entities found with the operations; this is an atomic operation per entity
     *
     * @param query      the query used to match the documents to update
     * @param operations the update operations to perform
     * @param <T>        the type of the entity
     * @return the results of the updates
     */
    <T> UpdateResults update(Query<T> query, UpdateOperations<T> operations);

    /**
     * Updates all entities found with the operations; this is an atomic operation per entity
     *
     * @param query      the query used to match the documents to update
     * @param operations the update operations to perform
     * @param options    the options to apply to the update
     * @param <T>        the type of the entity
     * @return the results of the updates
     * @since 1.3
     */
    <T> UpdateResults update(Query<T> query, UpdateOperations<T> operations, UpdateOptions options);

    /**
     * Updates all entities found with the operations, if nothing is found insert the update as an entity if "createIfMissing" is true;
     * this
     * is an atomic operation per entity
     *
     * @param query           the query used to match the documents to update
     * @param operations      the update operations to perform
     * @param createIfMissing if true, a document will be created if none can be found that match the query
     * @param <T>             the type of the entity
     * @return the results of the updates
     * @deprecated use {@link #update(Query, UpdateOperations, UpdateOptions)} with upsert set to the value of
     * createIfMissing
     */
    @Deprecated
    <T> UpdateResults update(Query<T> query, UpdateOperations<T> operations, boolean createIfMissing);

    /**
     * Updates all entities found with the operations, if nothing is found insert the update as an entity if "createIfMissing" is true;
     * this
     * is an atomic operation per entity
     *
     * @param query           the query used to match the documents to update
     * @param operations      the update operations to perform
     * @param createIfMissing if true, a document will be created if none can be found that match the query
     * @param wc              the WriteConcern to use
     * @param <T>             the type of the entity
     * @return the results of the updates
     * @deprecated use {@link AdvancedDatastore#update(Query, UpdateOperations, UpdateOptions)}
     * with upsert set to the value of createIfMissing
     */
    @Deprecated
    <T> UpdateResults update(Query<T> query, UpdateOperations<T> operations, boolean createIfMissing, WriteConcern wc);

    /**
     * Updates the first entity found with the operations; this is an atomic operation
     *
     * @param query      the query used to match the document to update
     * @param operations the update operations to perform
     * @param <T>        the type of the entity
     * @return the results of the update
     * @deprecated use {@link #update(Query, UpdateOperations, UpdateOptions)}
     */
    @Deprecated
    <T> UpdateResults updateFirst(Query<T> query, UpdateOperations<T> operations);

    /**
     * Updates the first entity found with the operations, if nothing is found insert the update as an entity if "createIfMissing" is true.
     *
     * @param query           the query used to match the documents to update
     * @param operations      the update operations to perform
     * @param createIfMissing if true, a document will be created if none can be found that match the query
     * @param <T>             the type of the entity
     * @return the results of the updates
     * @deprecated use {@link #update(Query, UpdateOperations, UpdateOptions)} with upsert set to the value of createIfMissing
     */
    @Deprecated
    <T> UpdateResults updateFirst(Query<T> query, UpdateOperations<T> operations, boolean createIfMissing);

    /**
     * Updates the first entity found with the operations, if nothing is found insert the update as an entity if "createIfMissing" is true.
     *
     * @param query           the query used to match the documents to update
     * @param operations      the update operations to perform
     * @param createIfMissing if true, a document will be created if none can be found that match the query
     * @param wc              the WriteConcern to use
     * @param <T>             the type of the entity
     * @return the results of the updates
     * @deprecated use {@link #update(Query, UpdateOperations, UpdateOptions)} with upsert set to the value of createIfMissing
     */
    @Deprecated
    <T> UpdateResults updateFirst(Query<T> query, UpdateOperations<T> operations, boolean createIfMissing, WriteConcern wc);

    /**
     * updates the first entity found using the entity as a template, if nothing is found insert the update as an entity if
     * "createIfMissing" is true.
     * <p>
     * If the entity is a versioned entity, an UnsupportedOperationException is thrown.
     *
     * @param query           the query used to match the documents to update
     * @param entity          the entity whose state will be used as an update template for any matching documents
     * @param createIfMissing if true, a document will be created if none can be found that match the query
     * @param <T>             the type of the entity
     * @return the results of the updates
     * @deprecated use {@link #update(Query, UpdateOperations, UpdateOptions)} with upsert set to the value of createIfMissing
     */
    @Deprecated
    <T> UpdateResults updateFirst(Query<T> query, T entity, boolean createIfMissing);

    /**
     * @return the Mapper used by this Datastore
     * @since 1.5
     * @morphia.internal
     */
    Mapper getMapper();
}
