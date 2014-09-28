/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.s2dao.extension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.jdbc.LazyDatabaseMetaDataWrapper;
import org.seasar.dbflute.jdbc.MetaDataConnectionProvider;
import org.seasar.dbflute.optional.RelationOptionalFactory;
import org.seasar.dbflute.resource.ManualThreadDataSourceHandler;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGeneratorFactory;
import org.seasar.dbflute.s2dao.metadata.TnBeanAnnotationReader;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnModifiedPropertySupport;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyTypeFactory;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyTypeFactoryBuilder;
import org.seasar.dbflute.s2dao.metadata.impl.TnBeanMetaDataFactoryImpl;
import org.seasar.dbflute.s2dao.metadata.impl.TnBeanMetaDataImpl;
import org.seasar.dbflute.s2dao.metadata.impl.TnDBMetaBeanAnnotationReader;
import org.seasar.dbflute.s2dao.metadata.impl.TnRelationPropertyTypeFactoryBuilderImpl;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The DBFlute extension of factory of bean meta data.
 * @author jflute
 */
public class TnBeanMetaDataFactoryExtension extends TnBeanMetaDataFactoryImpl {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for internal debug. (XLog should be used instead for execute-status log) */
    private static final Log _log = LogFactory.getLog(TnBeanMetaDataFactoryExtension.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The cached instance of relation optional factory. (NotNull) */
    protected final TnRelationRowOptionalHandler _relationRowOptionalHandler;

    /** The map of bean meta data for cache. (NotNull) */
    protected final Map<Class<?>, TnBeanMetaData> _metaMap = newConcurrentHashMap();

    /** Is internal debug enabled? */
    protected boolean _internalDebug;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param relationOptionalFactory The factory of relation optional. (NotNull)
     */
    public TnBeanMetaDataFactoryExtension(RelationOptionalFactory relationOptionalFactory) {
        _relationRowOptionalHandler = createRelationRowOptionalHandler(relationOptionalFactory);
    }

    // ===================================================================================
    //                                                                  Override for Cache
    //                                                                  ==================
    @Override
    public TnBeanMetaData createBeanMetaData(Class<?> beanClass) {
        final TnBeanMetaData cachedMeta = findCachedMeta(beanClass);
        if (cachedMeta != null) {
            return cachedMeta;
        } else {
            return super.createBeanMetaData(beanClass);
        }
    }

    @Override
    public TnBeanMetaData createBeanMetaData(Class<?> beanClass, int relationNestLevel) {
        final TnBeanMetaData cachedMeta = findCachedMeta(beanClass);
        if (cachedMeta != null) {
            return cachedMeta;
        } else {
            return super.createBeanMetaData(beanClass, relationNestLevel);
        }
    }

    @Override
    protected LazyDatabaseMetaDataWrapper createLazyDatabaseMetaDataWrapper(Class<?> beanClass) {
        final MetaDataConnectionProvider connectionProvider = createMetaDataConnectionProvider();
        final LazyDatabaseMetaDataWrapper metaDataWrapper = new LazyDatabaseMetaDataWrapper(connectionProvider);
        metaDataWrapper.restrictMetaData(); // because DBFlute completely does not use dynamic meta data
        return metaDataWrapper;
    }

    /**
     * Create the provider of connection for database meta data. <br />
     * The provider might provide connection from manual thread.
     * @return The instance of the connection provider. (NotNull)
     */
    protected MetaDataConnectionProvider createMetaDataConnectionProvider() {
        return new MetaDataConnectionProvider() {
            public Connection getConnection() throws SQLException {
                final ManualThreadDataSourceHandler handler = getManualThreadDataSourceHandler();
                if (handler != null) {
                    return handler.getConnection(_dataSource);
                }
                return _dataSource.getConnection();
            }
        };
    }

    /**
     * Get the data source handler of manual thread.
     * @return The instance of the data source handler. (NullAllowed: if null, no manual thread handling)
     */
    protected ManualThreadDataSourceHandler getManualThreadDataSourceHandler() {
        return ManualThreadDataSourceHandler.getDataSourceHandler();
    }

    @Override
    public TnBeanMetaData createBeanMetaData(DatabaseMetaData dbMetaData, Class<?> beanClass, int relationNestLevel) {
        final TnBeanMetaData cachedMeta = findOrCreateCachedMetaIfNeeds(dbMetaData, beanClass, relationNestLevel);
        if (cachedMeta != null) {
            return cachedMeta;
        } else {
            return super.createBeanMetaData(dbMetaData, beanClass, relationNestLevel);
        }
    }

    protected TnBeanMetaData findCachedMeta(Class<?> beanClass) {
        if (isDBFluteEntity(beanClass)) {
            final TnBeanMetaData cachedMeta = getMetaFromCache(beanClass);
            if (cachedMeta != null) {
                return cachedMeta;
            }
        }
        return null;
    }

    protected TnBeanMetaData findOrCreateCachedMetaIfNeeds(DatabaseMetaData dbMetaData, Class<?> beanClass,
            int relationNestLevel) {
        if (isDBFluteEntity(beanClass)) {
            final TnBeanMetaData cachedMeta = getMetaFromCache(beanClass);
            if (cachedMeta != null) {
                return cachedMeta;
            } else {
                return super.createBeanMetaData(dbMetaData, beanClass, 0);
            }
        }
        return null;
    }

    @Override
    protected TnBeanMetaDataImpl createBeanMetaDataImpl(Class<?> beanClass) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // for ConditionBean and insert() and update() and delete() and so on...
        // = = = = = = = = = =/
        final DBMeta dbmeta = provideDBMeta(beanClass);
        return new TnBeanMetaDataImpl(beanClass, dbmeta) {
            /** The internal list of identifier generator. Elements of this list should be added when initializing. */
            protected final List<TnIdentifierGenerator> _internalIdentifierGeneratorList = new ArrayList<TnIdentifierGenerator>();

            /** The internal map of identifier generator by property name. */
            protected final Map<String, TnIdentifierGenerator> _internalIdentifierGeneratorsByPropertyName = newConcurrentHashMap();

            // /= = = = = = =
            // for cache
            // = = = = =/
            @Override
            public void initialize() { // non thread safe so this is called immediately after creation 
                final Class<?> myBeanClass = getBeanClass();
                if (isDBFluteEntity(myBeanClass)) {
                    final TnBeanMetaData cachedMeta = getMetaFromCache(myBeanClass);
                    if (cachedMeta == null) {
                        if (isInternalDebugEnabled()) {
                            _log.debug("...Caching the bean: " + DfTypeUtil.toClassTitle(myBeanClass));
                        }
                        _metaMap.put(myBeanClass, this);
                    }
                }
                super.initialize();
            }

            // /= = = = = = =
            // for insert()
            // = = = = =/
            // The attributes 'identifierGenerators' and 'identifierGeneratorsByPropertyName'
            // of super class are unused. It prepares original attributes here.
            @Override
            protected void setupIdentifierGenerator(TnPropertyType propertyType) { // only called in the initialize() process
                final DfPropertyDesc pd = propertyType.getPropertyDesc();
                final String propertyName = propertyType.getPropertyName();
                final String idType = _beanAnnotationReader.getId(pd);
                final TnIdentifierGenerator generator = createInternalIdentifierGenerator(propertyType, idType);
                _internalIdentifierGeneratorList.add(generator);
                _internalIdentifierGeneratorsByPropertyName.put(propertyName, generator);
            }

            protected TnIdentifierGenerator createInternalIdentifierGenerator(TnPropertyType propertyType, String idType) {
                return TnIdentifierGeneratorFactory.createIdentifierGenerator(propertyType, idType);
            }

            @Override
            public TnIdentifierGenerator getIdentifierGenerator(int index) {
                return _internalIdentifierGeneratorList.get(index);
            }

            @Override
            public int getIdentifierGeneratorSize() {
                return _internalIdentifierGeneratorList.size();
            }

            @Override
            public TnIdentifierGenerator getIdentifierGenerator(String propertyName) {
                return _internalIdentifierGeneratorsByPropertyName.get(propertyName);
            }
        };
    }

    // ===================================================================================
    //                                                       Override for ModifiedProperty
    //                                                       =============================
    @Override
    protected TnModifiedPropertySupport createModifiedPropertySupport() {
        return new TnModifiedPropertySupport() {
            @SuppressWarnings("unchecked")
            public Set<String> getModifiedPropertyNames(Object bean) {
                if (bean instanceof Entity) { // all entities of DBFlute are here
                    return ((Entity) bean).modifiedProperties();
                } else { // basically no way on DBFlute (S2Dao's route)
                    final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(bean.getClass());
                    final String propertyName = MODIFIED_PROPERTY_PROPERTY_NAME;
                    if (!beanDesc.hasPropertyDesc(propertyName)) {
                        return Collections.EMPTY_SET;
                    } else {
                        final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(propertyName);
                        final Object value = propertyDesc.getValue(bean);
                        if (value != null) {
                            return (Set<String>) value;
                        } else {
                            return Collections.EMPTY_SET;
                        }
                    }
                }
            }
        };
    }

    // ===================================================================================
    //                                                                   Annotation Reader
    //                                                                   =================
    @Override
    protected TnBeanAnnotationReader createBeanAnnotationReader(Class<?> beanClass) {
        // the DBMeta annotation reader also has field annotation reader's functions
        // so fixedly creates and returns
        return new TnDBMetaBeanAnnotationReader(beanClass);
    }

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    protected TnRelationPropertyTypeFactory createRelationPropertyTypeFactory(Class<?> beanClass,
            TnBeanMetaDataImpl localBeanMetaData, TnBeanAnnotationReader beanAnnotationReader,
            DatabaseMetaData dbMetaData, int relationNestLevel, boolean stopRelationCreation) {
        // DBFlute needs local BeanMetaData for relation property type
        final TnRelationPropertyTypeFactoryBuilder builder = createRelationPropertyTypeFactoryBuilder();
        return builder.build(beanClass, localBeanMetaData, beanAnnotationReader, dbMetaData, relationNestLevel,
                stopRelationCreation, getRelationOptionalEntityType());
    }

    protected TnRelationPropertyTypeFactoryBuilder createRelationPropertyTypeFactoryBuilder() {
        return new TnRelationPropertyTypeFactoryBuilderImpl(this); // is already customized for DBFlute
    }

    protected Class<?> getRelationOptionalEntityType() {
        return _relationRowOptionalHandler.getOptionalEntityType();
    }

    // ===================================================================================
    //                                                                 Relation Next Level
    //                                                                 ===================
    /**
     * Get the limit nest level of relation.
     * @return The limit nest level of relation.
     */
    @Override
    protected int getLimitRelationNestLevel() {
        // for Compatible to old version DBFlute
        // and this is actually unused on ConditionBean for now
        // CB covers an infinity nest level scope by its own original way
        // this method is used only when you use runtime classes as plain S2Dao
        return 2;
    }

    // ===================================================================================
    //                                                                   Optional Handling
    //                                                                   =================
    public TnRelationRowOptionalHandler getRelationRowOptionalHandler() {
        return _relationRowOptionalHandler;
    }

    protected TnRelationRowOptionalHandler createRelationRowOptionalHandler(RelationOptionalFactory factory) {
        return new TnRelationRowOptionalHandler(factory);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isDBFluteEntity(Class<?> beanClass) {
        return Entity.class.isAssignableFrom(beanClass);
    }

    protected DBMeta provideDBMeta(Class<?> entityType) {
        return ResourceContext.provideDBMeta(entityType);
    }

    protected TnBeanMetaData getMetaFromCache(Class<?> beanClass) {
        return _metaMap.get(beanClass);
    }

    // ===================================================================================
    //                                                                      Internal Debug
    //                                                                      ==============
    private boolean isInternalDebugEnabled() { // because log instance is private
        return ResourceContext.isInternalDebug() && _log.isDebugEnabled();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return new ConcurrentHashMap<KEY, VALUE>();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setInternalDebug(boolean internalDebug) {
        _internalDebug = internalDebug;
    }
}