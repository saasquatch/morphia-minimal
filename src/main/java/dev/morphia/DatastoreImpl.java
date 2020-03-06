package dev.morphia;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import dev.morphia.annotations.NotSaved;
import dev.morphia.annotations.PostPersist;
import dev.morphia.annotations.Validation;
import dev.morphia.annotations.Version;
import dev.morphia.mapping.MappedClass;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MappingException;
import dev.morphia.mapping.cache.EntityCache;
import dev.morphia.mapping.lazy.proxy.ProxyHelper;
import dev.morphia.query.DefaultQueryFactory;
import dev.morphia.query.Query;
import dev.morphia.query.QueryFactory;
import dev.morphia.query.UpdateException;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.UpdateOpsImpl;
import dev.morphia.query.UpdateResults;

/**
 * A generic (type-safe) wrapper around mongodb collections
 *
 * @morphia.internal
 * @deprecated This is an internal implementation of a published API.  No public alternative planned.
 */
@Deprecated
public class DatastoreImpl implements AdvancedDatastore {
    private static final Logger LOG = LoggerFactory.getLogger(DatastoreImpl.class);

    private final Morphia morphia;
    private Mapper mapper;

    private volatile QueryFactory queryFactory = new DefaultQueryFactory();

    /**
     * Create a new DatastoreImpl
     *
     * @param morphia     the Morphia instance
     * @param mapper      an initialised Mapper
     * @deprecated This is not meant to be directly instantiated by end user code.  Use
     * {@link Morphia#createDatastore(MongoClient, Mapper, String)}
     */
    @Deprecated
    public DatastoreImpl(final Morphia morphia, final Mapper mapper) {
        this.morphia = morphia;
        this.mapper = mapper;
    }

    @Deprecated
    public DatastoreImpl(final Morphia morphia) {
    	this(morphia, morphia.getMapper());
    }

    /**
     * Creates a copy of this Datastore and all its configuration but with a new database
     *
     * @param database the new database to use for operations
     * @return the new Datastore instance
     * @deprecated use {@link Morphia#createDatastore(MongoClient, Mapper, String)}
     */
    @Deprecated
    public DatastoreImpl copy() {
        return new DatastoreImpl(morphia, mapper);
    }

    @Override
    public <T> Query<T> createQuery(final Class<T> collection) {
        return newQuery(collection);
    }

    @Override
    public <T> UpdateOperations<T> createUpdateOperations(final Class<T> clazz) {
        return new UpdateOpsImpl<>(clazz, getMapper());
    }

    void process(final MappedClass mc, final Validation validation) {
    }

    private <T> void updateForVersioning(final Query<T> query, final UpdateOperations<T> operations) {
        final MappedClass mc = mapper.getMappedClass(query.getEntityClass());

        if (!mc.getFieldsAnnotatedWith(Version.class).isEmpty()) {
            operations.inc(mc.getMappedVersionField().getNameToStore());
        }

    }

    private <T> MongoCollection<T> getMongoCollection(final Class<T> clazz) {
    	return null;
    }

    @SuppressWarnings("unchecked")
    private <T> MongoCollection<T> getMongoCollection(final String name, final Class<T> clazz) {
    	return null;
    }

    @Override
    public MongoDatabase getDatabase() {
        return null;
    }

    @Override
    public MongoClient getMongo() {
        return null;
    }

    @Override
    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    @Override
    public void setQueryFactory(final QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public <T> Key<T> merge(final T entity) {
        return merge(entity, getWriteConcern(entity));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Key<T> merge(final T entity, final WriteConcern wc) {
        return null;
    }

    @Override
    public <T> Query<T> queryByExample(final T ex) {
        return _queryByExample(ex);
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities) {
        Iterator<T> iterator = entities.iterator();
        return !iterator.hasNext()
               ? Collections.<Key<T>>emptyList()
               : save(entities, getWriteConcern(iterator.next()));
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities, final WriteConcern wc) {
        return save(entities, new InsertOptions().writeConcern(wc));
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities, final InsertOptions options) {
        final List<Key<T>> savedKeys = new ArrayList<>();
        for (final T ent : entities) {
            savedKeys.add(save(ent, options));
        }
        return savedKeys;

    }

    @Override
    @Deprecated
    public <T> Iterable<Key<T>> save(final T... entities) {
        return save(asList(entities), new InsertOptions());
    }

    @Override
    public <T> Key<T> save(final T entity) {
        return save(entity, new InsertOptions());
    }

    @Override
    @Deprecated
    public <T> Key<T> save(final T entity, final WriteConcern wc) {
        return save(entity, new InsertOptions()
                                .writeConcern(wc));
    }

    @Override
    public <T> Key<T> save(final T entity, final InsertOptions options) {
    	return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UpdateResults update(final T entity, final UpdateOperations<T> operations) {
        if (entity instanceof Query) {
            return update((Query<T>) entity, operations);
        }

        final MappedClass mc = mapper.getMappedClass(entity);
        Query<?> query = createQuery(mapper.getMappedClass(entity).getClazz())
                             .disableValidation()
                             .filter("_id", mapper.getId(entity));
        if (!mc.getFieldsAnnotatedWith(Version.class).isEmpty()) {
            final MappedField field = mc.getFieldsAnnotatedWith(Version.class).get(0);
            query.field(field.getNameToStore()).equal(field.getFieldValue(entity));
        }

        return update((Query<T>) query, operations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UpdateResults update(final Key<T> key, final UpdateOperations<T> operations) {
        Class<T> clazz = (Class<T>) key.getType();
        if (clazz == null) {
            clazz = (Class<T>) mapper.getClassFromCollection(key.getCollection());
        }
        return update(createQuery(clazz).disableValidation().filter("_id", key.getId()), operations, new UpdateOptions());
    }

    @Override
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations) {
        return update(query, operations, new UpdateOptions()
                                             .upsert(false)
                                             .multi(true)
                                             .writeConcern(getWriteConcern(query.getEntityClass())));
    }

    @Override
    @Deprecated
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing) {
        return update(query, operations, new UpdateOptions()
                                             .upsert(createIfMissing)
                                             .multi(true)
                                             .writeConcern(getWriteConcern(query.getEntityClass())));
    }

    @Override
    @Deprecated
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing,
                                    final WriteConcern wc) {
        return update(query, operations, new UpdateOptions()
                                             .upsert(createIfMissing)
                                             .multi(true)
                                             .writeConcern(wc));
    }

    @Override
    @Deprecated
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations) {
        return update(query, operations, new UpdateOptions());
    }

    @Override
    @Deprecated
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing) {
        return update(query, operations, new UpdateOptions()
                                             .upsert(createIfMissing));

    }

    @Override
    @Deprecated
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing,
                                         final WriteConcern wc) {
        return update(query, operations, new UpdateOptions()
                                             .upsert(createIfMissing)
                                             .writeConcern(wc));
    }

    @Override
    @Deprecated
    public <T> UpdateResults updateFirst(final Query<T> query, final T entity, final boolean createIfMissing) {
        return null;
    }

    @Override
    public <T> Query<T> createQuery(final String collection, final Class<T> type) {
        return newQuery(type);
    }

    @Override
    public <T> Query<T> createQuery(final Class<T> clazz, final DBObject q) {
        return newQuery(clazz, q);
    }

    @Override
    public <T> Query<T> createQuery(final String collection, final Class<T> type, final DBObject q) {
        return newQuery(type, q);
    }

    @Override
    public <T, V> DBRef createRef(final Class<T> clazz, final V id) {
        if (id == null) {
            throw new MappingException("Could not get id for " + clazz.getName());
        }
        return new DBRef("", id);
    }

    @Override
    public <T> DBRef createRef(final T entity) {
        final T wrapped = ProxyHelper.unwrap(entity);
        final Object id = mapper.getId(wrapped);
        if (id == null) {
            throw new MappingException("Could not get id for " + wrapped.getClass().getName());
        }
        return createRef(wrapped.getClass(), id);
    }

    @Override
    public <T> UpdateOperations<T> createUpdateOperations(final Class<T> type, final DBObject ops) {
        final UpdateOpsImpl<T> upOps = (UpdateOpsImpl<T>) createUpdateOperations(type);
        upOps.setOps(ops);
        return upOps;
    }

    @Override
    @Deprecated
    public <T> void ensureIndex(final String collection, final Class<T> type, final String fields) {
        ensureIndex(collection, type, null, fields, false, false);
    }

    @Override
    @Deprecated
    public <T> void ensureIndex(final String collection, final Class<T> clazz, final String name, final String fields, final boolean unique,
                                final boolean dropDupsOnCreate) {
        if (dropDupsOnCreate) {
            LOG.warn("Support for dropDups has been removed from the server.  Please remove this setting.");
        }
    }

    @Override
    public <T> void ensureIndexes(final String collection, final Class<T> clazz) {
        ensureIndexes(collection, clazz, false);
    }

    @Override
    public <T> void ensureIndexes(final String collection, final Class<T> clazz, final boolean background) {
    }

    @Override
    public <T> Query<T> find(final String collection, final Class<T> clazz) {
        return createQuery(collection, clazz);
    }

    @Override
    public <T, V> Query<T> find(final String collection, final Class<T> clazz, final String property, final V value, final int offset,
                                final int size) {
        return find(collection, clazz, property, value, offset, size, true);
    }

    @Override
    public <T> T get(final Class<T> clazz, final DBRef ref) {
    	return null;
    }

    @Override
    public <T, V> T get(final String collection, final Class<T> clazz, final V id) {
        final List<T> results = find(collection, clazz, "_id", id, 0, 1).asList();
        if (results == null || results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public long getCount(final String collection) {
    	return 0;
    }

    @Override
    public <T> Key<T> insert(final String collection, final T entity) {
    	return null;
    }

    @Override
    public <T> Key<T> insert(final String collection, final T entity, final InsertOptions options) {
    	return null;
    }

    @Override
    public <T> Key<T> insert(final T entity) {
        return insert(entity, getWriteConcern(entity));
    }

    @Override
    public <T> Key<T> insert(final T entity, final WriteConcern wc) {
        return insert(entity, new InsertOptions().writeConcern(wc));
    }

    @Override
    public <T> Key<T> insert(final T entity, final InsertOptions options) {
    	return null;
    }

    @Override
    @Deprecated
    public <T> Iterable<Key<T>> insert(final T... entities) {
        return insert(asList(entities));
    }

    @Override
    public <T> Iterable<Key<T>> insert(final Iterable<T> entities, final WriteConcern wc) {
        return insert(entities, new InsertOptions().writeConcern(wc));
    }

    @Override
    public <T> Iterable<Key<T>> insert(final Iterable<T> entities, final InsertOptions options) {
    	return null;
    }

    @Override
    public <T> Iterable<Key<T>> insert(final String collection, final Iterable<T> entities) {
        return insert(collection, entities, new InsertOptions());
    }

    @Override
    public <T> Iterable<Key<T>> insert(final String collection, final Iterable<T> entities, final WriteConcern wc) {
    	return null;
    }

    @Override
    public <T> Iterable<Key<T>> insert(final String collection, final Iterable<T> entities, final InsertOptions options) {
    	return null;
    }

    @Override
    public <T> Query<T> queryByExample(final String collection, final T ex) {
        return queryByExample(ex);
    }

    @Override
    public <T> Key<T> save(final String collection, final T entity) {
        final T unwrapped = ProxyHelper.unwrap(entity);
        return save(collection, entity, getWriteConcern(unwrapped));
    }

    @Override
    public <T> Key<T> save(final String collection, final T entity, final WriteConcern wc) {
    	return null;
    }

    @Override
    public <T> Key<T> save(final String collection, final T entity, final InsertOptions options) {
    	return null;
    }

    /**
     * Find all instances by type in a different collection than what is mapped on the class given skipping some documents and returning a
     * fixed number of the remaining.
     *
     * @param collection The collection use when querying
     * @param clazz      the class to use for mapping the results
     * @param property   the document property to query against
     * @param value      the value to check for
     * @param offset     the number of results to skip
     * @param size       the maximum number of results to return
     * @param validate   if true, validate the query
     * @param <T>        the type to query
     * @param <V>        the type to filter value
     * @return the query
     */
    public <T, V> Query<T> find(final String collection, final Class<T> clazz, final String property, final V value, final int offset,
                                final int size, final boolean validate) {
        final Query<T> query = find(collection, clazz);
        if (!validate) {
            query.disableValidation();
        }
        query.offset(offset);
        query.limit(size);
        return query.filter(property, value).enableValidation();
    }

    /**
     * @return the Mapper used by this Datastore
     */
    @Override
    public Mapper getMapper() {
        return mapper;
    }

    /**
     * Sets the Mapper this Datastore uses
     *
     * @param mapper the new Mapper
     */
    public void setMapper(final Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Inserts entities in to the database
     *
     * @param entities the entities to insert
     * @param <T>      the type of the entities
     * @return the keys of entities
     */
    @Override
    public <T> Iterable<Key<T>> insert(final Iterable<T> entities) {
        return insert(entities, new InsertOptions());
    }

    /**
     * Inserts an entity in to the database
     *
     * @param collection the collection to query against
     * @param entity     the entity to insert
     * @param wc         the WriteConcern to use when deleting
     * @param <T>        the type of the entities
     * @return the key of entity
     */
    public <T> Key<T> insert(final String collection, final T entity, final WriteConcern wc) {
    	return null;
    }

    @Deprecated
    protected Object getId(final Object entity) {
        return mapper.getId(entity);
    }

    private MongoCollection enforceWriteConcern(final MongoCollection collection, final Class klass) {
        WriteConcern applied = getWriteConcern(klass);
        return applied != null
               ? collection.withWriteConcern(applied)
               : collection;
    }

    <T> FindAndModifyOptions enforceWriteConcern(final FindAndModifyOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                       .copy()
                       .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> InsertOptions enforceWriteConcern(final InsertOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                       .copy()
                       .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> UpdateOptions enforceWriteConcern(final UpdateOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                       .copy()
                       .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> DeleteOptions enforceWriteConcern(final DeleteOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                       .copy()
                       .writeConcern(getWriteConcern(klass));
        }
        return options;
    }



/*
    @SuppressWarnings("unchecked")
    private <T> Key<T> save(final MongoCollection collection, final T entity, final InsertOneOptions options) {
        final MappedClass mc = validateSave(entity);

        // involvedObjects is used not only as a cache but also as a list of what needs to be called for life-cycle methods at the end.
        final LinkedHashMap<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        final Document document = new Document(entityToDBObj(entity, involvedObjects).toMap());

        // try to do an update if there is a @Version field
        final Object idValue = document.get(Mapper.ID_KEY);
        UpdateResult wr = tryVersionedUpdate(collection, entity, document, idValue, options, mc);

        if (wr == null) {
            if (document.get(ID_FIELD_NAME) == null) {
                 collection.insertOne(singletonList(document), options);
            } else {
                collection.updateOne(new Document(ID_FIELD_NAME, document.get(ID_FIELD_NAME)), document,
                    new com.mongodb.client.model.UpdateOptions()
                        .bypassDocumentValidation(options.getBypassDocumentValidation())
                        .upsert(true));
            }
        }

        return postSaveOperations(singletonList(entity), involvedObjects, collection.getNamespace().getCollectionName()).get(0);
    }
*/

    private <T> MappedClass validateSave(final T entity) {
        if (entity == null) {
            throw new UpdateException("Can not persist a null entity");
        }

        final MappedClass mc = mapper.getMappedClass(entity);
        if (mc.getAnnotation(NotSaved.class) != null) {
            throw new MappingException(format("Entity type: %s is marked as NotSaved which means you should not try to save it!",
                mc.getClazz().getName()));
        }
        return mc;
    }

    private EntityCache createCache() {
        return mapper.createEntityCache();
    }

    private DBObject entityToDBObj(final Object entity, final Map<Object, DBObject> involvedObjects) {
        return mapper.toDBObject(ProxyHelper.unwrap(entity), involvedObjects);
    }

    /**
     * Creates and returns a {@link Query} using the underlying {@link QueryFactory}.
     *
     * @see QueryFactory#createQuery(Datastore, DBCollection, Class, DBObject)
     */
    private <T> Query<T> newQuery(final Class<T> type, final DBObject query) {
        return getQueryFactory().createQuery(this, type, query);
    }

    /**
     * Creates and returns a {@link Query} using the underlying {@link QueryFactory}.
     *
     * @see QueryFactory#createQuery(Datastore, DBCollection, Class)
     */
    private <T> Query<T> newQuery(final Class<T> type) {
        return getQueryFactory().createQuery(this, type);
    }

    private long nextValue(final Long oldVersion) {
        return oldVersion == null ? 1 : oldVersion + 1;
    }

    private <T> List<Key<T>> postSaveOperations(final Iterable<T> entities,
                                                final Map<Object, DBObject> involvedObjects,
                                                final String collectionName) {
        return postSaveOperations(entities, involvedObjects, true, collectionName);
    }

    @SuppressWarnings("unchecked")
    private <T> List<Key<T>> postSaveOperations(final Iterable<T> entities, final Map<Object, DBObject> involvedObjects,
                                                final boolean fetchKeys, final String collectionName) {
        List<Key<T>> keys = new ArrayList<>();
        for (final T entity : entities) {
            final DBObject dbObj = involvedObjects.remove(entity);

            if (fetchKeys) {
                if (dbObj.get("_id") == null) {
                    throw new MappingException(format("Missing _id after save on %s", entity.getClass().getName()));
                }
                mapper.updateKeyAndVersionInfo(this, dbObj, createCache(), entity);
                keys.add(new Key<T>((Class<? extends T>) entity.getClass(), collectionName, mapper.getId(entity)));
            }
            mapper.getMappedClass(entity).callLifecycleMethods(PostPersist.class, entity, dbObj, mapper);
        }

        for (Entry<Object, DBObject> entry : involvedObjects.entrySet()) {
            final Object key = entry.getKey();
            mapper.getMappedClass(key).callLifecycleMethods(PostPersist.class, key, entry.getValue(), mapper);

        }
        return keys;
    }

    @SuppressWarnings("unchecked")
    private <T> Query<T> _queryByExample(final T example) {
        // TODO: think about remove className from baseQuery param below.
        final Class<T> type = (Class<T>) example.getClass();
        final DBObject query = entityToDBObj(example, new HashMap<Object, DBObject>());
        return newQuery(type, query);
    }

    private <T> DBObject toDbObject(final T ent, final Map<Object, DBObject> involvedObjects) {
        final MappedClass mc = mapper.getMappedClass(ent);
        if (mc.getAnnotation(NotSaved.class) != null) {
            throw new MappingException(format("Entity type: %s is marked as NotSaved which means you should not try to save it!",
                mc.getClazz().getName()));
        }
        DBObject dbObject = entityToDBObj(ent, involvedObjects);
        List<MappedField> versionFields = mc.getFieldsAnnotatedWith(Version.class);
        for (MappedField mappedField : versionFields) {
            String name = mappedField.getNameToStore();
            if (dbObject.get(name) == null) {
                dbObject.put(name, 1);
                mappedField.setFieldValue(ent, 1L);
            }
        }
        return dbObject;
    }

    @Override
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations, final UpdateOptions options) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> UpdateResults update(final Query<T> query, final DBObject update, final UpdateOptions options) {
    	return null;
    }

    /**
     * Gets the write concern for entity or returns the default write concern for this datastore
     *
     * @param clazzOrEntity the class or entity to use when looking up the WriteConcern
     */
    private WriteConcern getWriteConcern(final Object clazzOrEntity) {
        return null;
    }
}
