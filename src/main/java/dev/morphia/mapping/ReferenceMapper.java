package dev.morphia.mapping;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.DBRef;

import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.annotations.Reference;
import dev.morphia.mapping.cache.EntityCache;
import dev.morphia.mapping.experimental.CollectionReference;
import dev.morphia.mapping.experimental.MapReference;
import dev.morphia.mapping.experimental.MorphiaReference;
import dev.morphia.mapping.experimental.SingleReference;
import dev.morphia.mapping.lazy.proxy.ProxiedEntityReference;
import dev.morphia.mapping.lazy.proxy.ProxiedEntityReferenceList;
import dev.morphia.mapping.lazy.proxy.ProxiedEntityReferenceMap;
import dev.morphia.mapping.lazy.proxy.ProxyHelper;

/**
 * @morphia.internal
 * @deprecated
 */
@Deprecated
@SuppressWarnings({"unchecked", "rawtypes"})
class ReferenceMapper implements CustomMapper {
    public static final Logger LOG = LoggerFactory.getLogger(ReferenceMapper.class);

    @Override
    public void fromDBObject(final Datastore datastore, final DBObject dbObject, final MappedField mf, final Object entity,
                             final EntityCache cache, final Mapper mapper) {
        final Class fieldType = mf.getType();

        if (mf.getType().equals(MorphiaReference.class) && !mf.getTypeParameters().isEmpty()) {
            readMorphiaReferenceValues(mapper, datastore, mf, dbObject, entity);
        } else {
            final Reference refAnn = mf.getAnnotation(Reference.class);
            if (mf.isMap()) {
                readMap(datastore, mapper, entity, refAnn, cache, mf, dbObject);
            } else if (mf.isMultipleValues()) {
                readCollection(datastore, mapper, dbObject, mf, entity, refAnn, cache);
            } else {
                readSingle(datastore, mapper, entity, fieldType, refAnn, cache, mf, dbObject);
            }
        }

    }

    @Override
    public void toDBObject(final Object entity, final MappedField mf, final DBObject dbObject, final Map<Object, DBObject> involvedObjects,
                           final Mapper mapper) {
        final String name = mf.getNameToStore();

        final Object fieldValue = mf.getFieldValue(entity);

        if (fieldValue == null && !mapper.getOptions().isStoreNulls()) {
            return;
        }

        if (fieldValue instanceof MorphiaReference && !mf.getTypeParameters().isEmpty()) {
            writeMorphiaReferenceValues(dbObject, mf, fieldValue, name, mapper);
        } else {
            final Reference refAnn = mf.getAnnotation(Reference.class);
            if (mf.isMap()) {
                writeMap(mf, dbObject, name, fieldValue, refAnn, mapper);
            } else if (mf.isMultipleValues()) {
                writeCollection(mf, dbObject, name, fieldValue, refAnn, mapper);
            } else {
                writeSingle(dbObject, name, fieldValue, refAnn, mapper);
            }
        }

    }

    private void addValue(final List values, final Object o, final Mapper mapper, final boolean idOnly) {
        if (o == null && mapper.getOptions().isStoreNulls()) {
            values.add(null);
            return;
        }

        final Key key = o instanceof Key
                        ? (Key) o
                        : getKey(o, mapper);
        values.add(idOnly
                   ? mapper.keyToId(key)
                   : mapper.keyToDBRef(key));
    }

    private Key<?> getKey(final Object entity, final Mapper mapper) {
        try {
            if (entity instanceof ProxiedEntityReference) {
                final ProxiedEntityReference proxy = (ProxiedEntityReference) entity;
                return proxy.__getKey();
            }
            final MappedClass mappedClass = mapper.getMappedClass(entity);
            Object id = mappedClass.getIdField().get(entity);
            if (id == null) {
                throw new MappingException("@Id field cannot be null!");
            }
            return new Key(mappedClass.getClazz(), mappedClass.getCollectionName(), id);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
    }

    private void readCollection(final Datastore datastore, final Mapper mapper, final DBObject dbObject, final MappedField mf,
                                final Object entity,
                                final Reference refAnn,
                                final EntityCache cache) {
    }

    private void readMap(final Datastore datastore, final Mapper mapper, final Object entity, final Reference refAnn,
                         final EntityCache cache, final MappedField mf, final DBObject dbObject) {
    }

    private void readSingle(final Datastore datastore, final Mapper mapper, final Object entity, final Class fieldType,
                            final Reference annotation, final EntityCache cache, final MappedField mf, final DBObject dbObject) {
    }

    private void writeCollection(final MappedField mf, final DBObject dbObject, final String name, final Object fieldValue,
                                 final Reference refAnn, final Mapper mapper) {
        if (fieldValue != null) {
            final List values = new ArrayList();

            if (ProxyHelper.isProxy(fieldValue) && ProxyHelper.isUnFetched(fieldValue)) {
                final ProxiedEntityReferenceList p = (ProxiedEntityReferenceList) fieldValue;
                final List<Key<?>> getKeysAsList = p.__getKeysAsList();
                for (final Key<?> key : getKeysAsList) {
                    addValue(values, key, mapper, refAnn.idOnly());
                }
            } else {

                if (mf.getType().isArray()) {
                    for (final Object o : (Object[]) fieldValue) {
                        addValue(values, o, mapper, refAnn.idOnly());
                    }
                } else {
                    for (final Object o : (Iterable) fieldValue) {
                        addValue(values, o, mapper, refAnn.idOnly());
                    }
                }
            }
            if (!values.isEmpty() || mapper.getOptions().isStoreEmpties()) {
                dbObject.put(name, values);
            }
        }
    }

    private void writeMap(final MappedField mf, final DBObject dbObject, final String name, final Object fieldValue,
                          final Reference refAnn, final Mapper mapper) {
        final Map<Object, Object> map = (Map<Object, Object>) fieldValue;
        if (map != null) {
            final Map values = mapper.getOptions().getObjectFactory().createMap(mf);

            if (ProxyHelper.isProxy(map) && ProxyHelper.isUnFetched(map)) {
                final ProxiedEntityReferenceMap proxy = (ProxiedEntityReferenceMap) map;

                final Map<Object, Key<?>> refMap = proxy.__getReferenceMap();
                for (final Map.Entry<Object, Key<?>> entry : refMap.entrySet()) {
                    final Object key = entry.getKey();
                    values.put(key, refAnn.idOnly()
                                    ? mapper.keyToId(entry.getValue())
                                    : mapper.keyToDBRef(entry.getValue()));
                }
            } else {
                for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                    final String strKey = mapper.getConverters().encode(entry.getKey()).toString();
                    values.put(strKey, refAnn.idOnly()
                                       ? mapper.keyToId(getKey(entry.getValue(), mapper))
                                       : mapper.keyToDBRef(getKey(entry.getValue(), mapper)));
                }
            }
            if (!values.isEmpty() || mapper.getOptions().isStoreEmpties()) {
                dbObject.put(name, values);
            }
        }
    }

    private void writeSingle(final DBObject dbObject, final String name, final Object fieldValue, final Reference refAnn,
                             final Mapper mapper) {
        if (fieldValue == null) {
            if (mapper.getOptions().isStoreNulls()) {
                dbObject.put(name, null);
            }
        } else {
            Key<?> key = getKey(fieldValue, mapper);
            if (refAnn.idOnly()) {
                Object id = mapper.keyToId(key);
                if (id != null && mapper.isMapped(id.getClass())) {
                    id = mapper.toMongoObject(id, true);
                }

                dbObject.put(name, id);
            } else {
                dbObject.put(name, mapper.keyToDBRef(key));
            }
        }
    }

    Object resolveObject(final Datastore datastore, final Mapper mapper, final EntityCache cache, final MappedField mf,
                         final boolean idOnly, final Object ref) {
    	return null;
    }

    void readMorphiaReferenceValues(final Mapper mapper, final Datastore datastore, final MappedField mappedField,
                                           final DBObject dbObject, final Object entity) {
        final Class paramType = mappedField.getTypeParameters().get(0).getType();
        MorphiaReference<?> reference;
        if (Map.class.isAssignableFrom(paramType)) {
            reference = MapReference.decode(datastore, mapper, mappedField, dbObject);
        } else if (Collection.class.isAssignableFrom(paramType)) {
            reference = CollectionReference.decode(datastore, mapper, mappedField, paramType, dbObject);
        } else {
            reference = SingleReference.decode(datastore, mapper, mappedField, paramType, dbObject);
        }
        mappedField.setFieldValue(entity, reference);
    }

    void writeMorphiaReferenceValues(final DBObject dbObject, final MappedField mf, final Object fieldValue, final String name,
                                     final Mapper mapper) {
        final Class paramType = mf.getTypeParameters().get(0).getType();

        boolean notEmpty = true;
        final Object value = ((MorphiaReference) fieldValue).encode(mapper, fieldValue, mf);
        final boolean notNull = value != null;

        if (Map.class.isAssignableFrom(paramType)) {
            notEmpty = notNull && !((Map) value).isEmpty();
        } else if (Collection.class.isAssignableFrom(paramType)) {
            notEmpty = notNull && !((Collection) value).isEmpty();
        }

        if ((notNull || mapper.getOptions().isStoreNulls())
            && (notEmpty || mapper.getOptions().isStoreEmpties())) {
            dbObject.put(name, value);
        }
    }
}
