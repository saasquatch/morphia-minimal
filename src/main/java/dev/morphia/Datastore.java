package dev.morphia;


import java.util.List;

import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Text;
import dev.morphia.annotations.Validation;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.CountOptions;
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
     * ensure capped collections for {@code Entity}(s)
     */
    void ensureCaps();

    /**
     * Process any {@link Validation} annotations for document validation.
     *
     * @mongodb.driver.manual core/document-validation/
     * @since 1.3
     */
    void enableDocumentValidation();

    /**
     * Ensures (creating if necessary) the index including the field(s) + directions on the given collection name; eg fields = "field1,
     * -field2" ({field1:1, field2:-1})
     *
     * @param clazz  the class from which to get the index definitions
     * @param fields the fields to index
     * @param <T>    the type to index
     * @see MongoCollection#createIndex(org.bson.conversions.Bson, com.mongodb.client.model.IndexOptions)
     * @deprecated This method uses the legacy approach for defining indexes.  Switch to using annotations on entity classes or the
     * methods in the Java driver itself.
     */
    @Deprecated
    <T> void ensureIndex(Class<T> clazz, String fields);

    /**
     * Ensures (creating if necessary) the index including the field(s) + directions on the given collection name; eg fields = "field1,
     * -field2" ({field1:1, field2:-1})
     *
     * @param clazz            the class from which to get the index definitions
     * @param name             the name of the index to create
     * @param fields           the fields to index
     * @param unique           true if the index should enforce uniqueness on the fields indexed
     * @param dropDupsOnCreate Support for this has been removed from the server.  This value is ignored.
     * @param <T>              the type to index
     * @see MongoCollection#createIndex(org.bson.conversions.Bson, com.mongodb.client.model.IndexOptions)
     * @deprecated This method uses the legacy approach for defining indexes.  Switch to using annotations on entity classes or the
     * methods in the Java driver itself.
     */
    @Deprecated
    <T> void ensureIndex(Class<T> clazz, String name, String fields, boolean unique, boolean dropDupsOnCreate);

    /**
     * Ensures (creating if necessary) the indexes found during class mapping
     *
     * @see Indexes
     * @see Indexed
     * @see Text
     */
    void ensureIndexes();

    /**
     * Ensures (creating if necessary) the indexes found during class mapping (using {@code @Indexed, @Indexes)} on the given collection
     * name, possibly in the background
     *
     * @param background if true, the index will be built in the background.  If false, background indexing is deferred to the annotation
     *                   definition
     * @see Indexes
     * @see Indexed
     * @see Text
     * @deprecated use {@link #ensureIndexes()} instead
     */
    @Deprecated
    void ensureIndexes(boolean background);

    /**
     * Ensures (creating if necessary) the indexes found during class mapping
     *
     * @param clazz the class from which to get the index definitions
     * @param <T>   the type to index
     * @see Indexes
     * @see Indexed
     * @see Text
     */
    <T> void ensureIndexes(Class<T> clazz);

    /**
     * Ensures (creating if necessary) the indexes found during class mapping
     *
     * @param clazz      the class from which to get the index definitions
     * @param background if true, the index will be built in the background.  If false, background indexing is deferred to the annotation
     *                   definition
     * @param <T>        the type to index
     * @see Indexes
     * @see Indexed
     * @see Text
     * @deprecated use {@link #ensureIndexes(Class)} instead
     */
    @Deprecated
    <T> void ensureIndexes(Class<T> clazz, boolean background);

    /**
     * Does a query to check if the keyOrEntity exists in mongodb
     *
     * @param keyOrEntity the value to check for
     * @return the key if the entity exists
     * @deprecated use {@link Query#first()} instead
     */
    @Deprecated
    Key<?> exists(Object keyOrEntity);

    /**
     * Find all instances by type
     *
     * @param clazz the class to use for mapping the results
     * @param <T>   the type to query
     * @return the query
     */
    <T> Query<T> find(Class<T> clazz);

    /**
     * <p> Find all instances by collectionName, and filter property. </p><p> This is the same as: {@code find(clazzOrEntity).filter
     * (property, value); } </p>
     *
     * @param clazz    the class to use for mapping the results
     * @param property the document property to query against
     * @param value    the value to check for
     * @param <T>      the type to query
     * @param <V>      the type to filter value
     * @return the query
     * @deprecated use {@link Query} instead
     */
    @Deprecated
    <T, V> Query<T> find(Class<T> clazz, String property, V value);

    /**
     * Find all instances by type in a different collection than what is mapped on the class given skipping some documents and returning a
     * fixed number of the remaining.
     *
     * @param clazz    the class to use for mapping the results
     * @param property the document property to query against
     * @param value    the value to check for
     * @param offset   the number of results to skip
     * @param size     the maximum number of results to return
     * @param <T>      the type to query
     * @param <V>      the type to filter value
     * @return the query
     * @deprecated use {@link Query} instead
     */
    @Deprecated
    <T, V> Query<T> find(Class<T> clazz, String property, V value, int offset, int size);

    /**
     * Deletes the given entities based on the query (first item only).
     *
     * @param query the query to use when finding entities to delete
     * @param <T>   the type to query
     * @return the deleted Entity
     */
    <T> T findAndDelete(Query<T> query);

    /**
     * Deletes the given entities based on the query (first item only).
     *
     * @param query   the query to use when finding entities to delete
     * @param options the options to apply to the delete
     * @param <T>     the type to query
     * @return the deleted Entity
     * @since 1.3
     */
    <T> T findAndDelete(Query<T> query, FindAndModifyOptions options);

    /**
     * Find the first Entity from the Query, and modify it.
     *
     * @param query      the query to use when finding entities to update
     * @param operations the updates to apply to the matched documents
     * @param options    the options to apply to the update
     * @param <T>        the type to query
     * @return The modified Entity (the result of the update)
     * @since 1.3
     */
    <T> T findAndModify(Query<T> query, UpdateOperations<T> operations, FindAndModifyOptions options);

    /**
     * Find the first Entity from the Query, and modify it.
     *
     * @param query      the query to use when finding entities to update
     * @param operations the updates to apply to the matched documents
     * @param <T>        the type to query
     * @return The modified Entity (the result of the update)
     */
    <T> T findAndModify(Query<T> query, UpdateOperations<T> operations);

    /**
     * Find the first Entity from the Query, and modify it.
     *
     * @param query      the query to find the Entity with; You are not allowed to offset/skip in the query.
     * @param operations the updates to apply to the matched documents
     * @param oldVersion indicated the old version of the Entity should be returned
     * @param <T>        the type to query
     * @return The Entity (the result of the update if oldVersion is false)
     * @deprecated use {@link #findAndModify(Query, UpdateOperations, FindAndModifyOptions)}
     */
    @Deprecated
    <T> T findAndModify(Query<T> query, UpdateOperations<T> operations, boolean oldVersion);

    /**
     * Find the first Entity from the Query, and modify it.
     *
     * @param query           the query to find the Entity with; You are not allowed to offset/skip in the query.
     * @param operations      the updates to apply to the matched documents
     * @param oldVersion      indicated the old version of the Entity should be returned
     * @param createIfMissing if the query returns no results, then a new object will be created (sets upsert=true)
     * @param <T>             the type of the entity
     * @return The Entity (the result of the update if oldVersion is false)
     * @deprecated use {@link #findAndModify(Query, UpdateOperations, FindAndModifyOptions)}
     */
    @Deprecated
    <T> T findAndModify(Query<T> query, UpdateOperations<T> operations, boolean oldVersion, boolean createIfMissing);

    /**
     * Find the given entities (by id); shorthand for {@code find("_id in", ids)}
     *
     * @param clazz the class to use for mapping
     * @param ids   the IDs to query
     * @param <T>   the type to fetch
     * @param <V>   the type of the ID
     * @return the query to find the entities
     * @deprecated use {@link Query} instead.
     * @morphia.inline
     */
    @Deprecated
    <T, V> Query<T> get(Class<T> clazz, Iterable<V> ids);

    /**
     * Find the given entity (by id); shorthand for {@code find("_id ", id)}
     *
     * @param clazz the class to use for mapping
     * @param id    the ID to query
     * @param <T>   the type to fetch
     * @param <V>   the type of the ID
     * @return the matched entity.  may be null.
     * @deprecated use {@link Query} instead
     */
    @Deprecated
    <T, V> T get(Class<T> clazz, V id);

    /**
     * Find the given entity (by collectionName/id); think of this as refresh
     *
     * @param entity The entity to search for
     * @param <T>    the type to fetch
     * @return the matched entity.  may be null.
     * @deprecated use {@link Query} instead
     * @morphia.inline
     */
    @Deprecated
    <T> T get(T entity);

    /**
     * Find the given entity (by collectionName/id);
     *
     * @param clazz the class to use for mapping
     * @param key   the key search with
     * @param <T>   the type to fetch
     * @deprecated use a {@link Query} instead
     * @return the matched entity.  may be null.
     */
    @Deprecated
    <T> T getByKey(Class<T> clazz, Key<T> key);


    /**
     * Find the given entities (by id), verifying they are of the correct type; shorthand for {@code find("_id in", ids)}
     *
     * @param clazz the class to use for mapping
     * @param keys  the keys to search with
     * @param <T>   the type to fetch
     * @return the matched entities.  may be null.
     * @deprecated use a {@link Query} instead
     */
    @Deprecated
    <T> List<T> getByKeys(Class<T> clazz, Iterable<Key<T>> keys);

    /**
     * Find the given entities (by id); shorthand for {@code find("_id in", ids)}
     *
     * @param keys the keys to search with
     * @param <T>  the type to fetch
     * @return the matched entities.  may be null.
     * @deprecated use a {@link Query} instead
     */
    @Deprecated
    <T> List<T> getByKeys(Iterable<Key<T>> keys);

    /**
     * Gets the count this kind ({@link DBCollection})
     *
     * @param entity The entity whose type to count
     * @param <T>    the type to count
     * @return the count
     * @deprecated use {@link Query#count()} instead
     */
    @Deprecated
    <T> long getCount(T entity);

    /**
     * Gets the count this kind ({@link DBCollection})
     *
     * @param clazz The clazz type to count
     * @param <T>   the type to count
     * @return the count
     * @deprecated use {@link Query#count()} instead
     * @morphia.inline
     */
    @Deprecated
    <T> long getCount(Class<T> clazz);


    /**
     * Gets the count of items returned by this query; same as {@code query.countAll()}
     *
     * @param query the query to filter the documents to count
     * @param <T>   the type to count
     * @return the count
     * @deprecated use {@link Query#count()} instead
     * @morphia.inline
     */
    @Deprecated
    <T> long getCount(Query<T> query);

    /**
     * Gets the count of items returned by this query; same as {@code query.countAll()}
     *
     * @param query   the query to filter the documents to count
     * @param <T>     the type to count
     * @param options the options to apply to the count
     * @return the count
     * @since 1.3
     * @deprecated use {@link Query#count(CountOptions)} instead
     */
    @Deprecated
    <T> long getCount(Query<T> query, CountOptions options);

    /**
     * @return the MongoDatabase used by this DataStore
     * @since 1.5
     * @morphia.internal
     */
    MongoDatabase getDatabase();

    /**
     * @return the default WriteConcern used by this Datastore
     * @deprecated {@link MongoClient#setWriteConcern(WriteConcern)}
     */
    @Deprecated
    WriteConcern getDefaultWriteConcern();

    /**
     * Sets the default WriteConcern for this Datastore
     *
     * @param wc the default WriteConcern to be used by this Datastore
     * @deprecated {@link MongoClient#setWriteConcern(WriteConcern)}
     */
    @Deprecated
    void setDefaultWriteConcern(WriteConcern wc);

    /**
     * Creates a (type-safe) reference to the entity; if stored this will become a {@link com.mongodb.DBRef}
     *
     * @param entity the entity whose key is to be returned
     * @param <T>    the type of the entity
     * @return the Key
     */
    <T> Key<T> getKey(T entity);

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
