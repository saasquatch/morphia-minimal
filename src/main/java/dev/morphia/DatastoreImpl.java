package dev.morphia;

import com.mongodb.WriteConcern;

import dev.morphia.annotations.Validation;
import dev.morphia.mapping.MappedClass;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.DefaultQueryFactory;
import dev.morphia.query.Query;
import dev.morphia.query.QueryFactory;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.UpdateOpsImpl;

/**
 * A generic (type-safe) wrapper around mongodb collections
 *
 * @morphia.internal
 */
public class DatastoreImpl implements Datastore {
//    private static final Logger LOG = LoggerFactory.getLogger(DatastoreImpl.class);

    private final Morphia morphia;
    private Mapper mapper;

    private volatile QueryFactory queryFactory = new DefaultQueryFactory();

    /**
     * Create a new DatastoreImpl
     *
     * @param morphia     the Morphia instance
     * @param mapper      an initialised Mapper
     */
    @Deprecated
	public DatastoreImpl(final Morphia morphia, final Mapper mapper) {
        this.morphia = morphia;
        this.mapper = mapper;
    }

    public DatastoreImpl(final Morphia morphia) {
    	this(morphia, morphia.getMapper());
    }

    /**
     * Creates a copy of this Datastore and all its configuration but with a new database
     *
     * @param database the new database to use for operations
     * @return the new Datastore instance
     * @deprecated use {@link Morphia#createDatastore(Mapper, String)}
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

    @Override
    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    @Override
    public void setQueryFactory(final QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
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

    @Deprecated
    protected Object getId(final Object entity) {
        return mapper.getId(entity);
    }

    /**
     * Creates and returns a {@link Query} using the underlying {@link QueryFactory}.
     *
     * @see QueryFactory#createQuery(Datastore, DBCollection, Class)
     */
    private <T> Query<T> newQuery(final Class<T> type) {
        return getQueryFactory().createQuery(this, type);
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
