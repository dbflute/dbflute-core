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
package org.seasar.dbflute.s2dao.metadata.impl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.dbflute.exception.handler.SQLExceptionHandler;
import org.seasar.dbflute.exception.handler.SQLExceptionResource;
import org.seasar.dbflute.jdbc.LazyDatabaseMetaDataWrapper;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.extension.TnBeanMetaDataFactoryExtension;
import org.seasar.dbflute.s2dao.metadata.TnBeanAnnotationReader;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.seasar.dbflute.s2dao.metadata.TnModifiedPropertySupport;
import org.seasar.dbflute.s2dao.metadata.TnPropertyTypeFactory;
import org.seasar.dbflute.s2dao.metadata.TnPropertyTypeFactoryBuilder;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyTypeFactory;

/**
 * The implementation as S2Dao of factory of bean meta data. <br />
 * This class has sub-class extended by DBFlute.
 * <pre>
 * {@link TnBeanMetaDataFactoryImpl} is close to S2Dao logic
 * {@link TnBeanMetaDataFactoryExtension} has DBFlute logic
 * </pre>
 * DBFlute depended on S2Dao before 0.9.0. <br />
 * It saves these structure to be easy to know what DBFlute extends it. <br />
 * However several S2Dao's logics are deleted as abstract methods
 * and this class already has several DBFlute logics...
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnBeanMetaDataFactoryImpl implements TnBeanMetaDataFactory {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The property name of modified property. (S2Dao's specification) */
    protected static final String MODIFIED_PROPERTY_PROPERTY_NAME = "modifiedPropertyNames";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource _dataSource;

    // ===================================================================================
    //                                                                            Creation
    //                                                                            ========
    // /= = = = = = = = = = = = = = = = = = = = = = = = = =
    // these methods are overridden at the extension class 
    // = = = = = = = = = =/
    // this is overridden but called in sub-class
    public TnBeanMetaData createBeanMetaData(Class<?> beanClass) {
        return createBeanMetaData(beanClass, 0);
    }

    // this is overridden but called in sub-class
    public TnBeanMetaData createBeanMetaData(Class<?> beanClass, int relationNestLevel) {
        if (beanClass == null) {
            throw new IllegalArgumentException("The argument 'beanClass' should not be null.");
        }
        final LazyDatabaseMetaDataWrapper metaDataWrapper = createLazyDatabaseMetaDataWrapper(beanClass);
        try {
            return createBeanMetaData(metaDataWrapper, beanClass, relationNestLevel);
        } finally {
            try {
                metaDataWrapper.closeActualReally();
            } catch (SQLException e) {
                final SQLExceptionResource resource = createSQLExceptionResource();
                resource.setNotice("Failed to close the database connection.");
                handleSQLException(e, resource);
            }
        }
    }

    protected abstract LazyDatabaseMetaDataWrapper createLazyDatabaseMetaDataWrapper(Class<?> beanClass);

    protected void handleSQLException(SQLException e, SQLExceptionResource resource) {
        createSQLExceptionHandler().handleSQLException(e, resource);
    }

    protected SQLExceptionHandler createSQLExceptionHandler() {
        return ResourceContext.createSQLExceptionHandler();
    }

    protected SQLExceptionResource createSQLExceptionResource() {
        return new SQLExceptionResource();
    }

    // this is overridden but called in sub-class
    public TnBeanMetaData createBeanMetaData(DatabaseMetaData dbMetaData, Class<?> beanClass, int relationNestLevel) {
        if (dbMetaData == null) {
            throw new IllegalArgumentException("The argument 'dbMetaData' should not be null.");
        }
        if (beanClass == null) {
            throw new IllegalArgumentException("The argument 'beanClass' should not be null.");
        }
        final TnBeanMetaDataImpl bmd = createBeanMetaDataImpl(beanClass);
        final TnBeanAnnotationReader beanAnnotationReader = createBeanAnnotationReader(beanClass);
        final String versionNoPropertyName = getVersionNoPropertyName(beanAnnotationReader);
        final String timestampPropertyName = getTimestampPropertyName(beanAnnotationReader);
        bmd.setBeanAnnotationReader(beanAnnotationReader);
        bmd.setVersionNoPropertyName(versionNoPropertyName);
        bmd.setTimestampPropertyName(timestampPropertyName);
        bmd.setPropertyTypeFactory(createPropertyTypeFactory(beanClass, beanAnnotationReader, dbMetaData));

        final boolean stopRelationCreation = isLimitRelationNestLevel(relationNestLevel);
        bmd.setRelationPropertyTypeFactory(createRelationPropertyTypeFactory(beanClass, bmd, beanAnnotationReader,
                dbMetaData, relationNestLevel, stopRelationCreation));

        bmd.setModifiedPropertySupport(createModifiedPropertySupport());
        bmd.initialize();
        return bmd;
    }

    protected abstract TnBeanMetaDataImpl createBeanMetaDataImpl(Class<?> beanClass);

    protected abstract TnModifiedPropertySupport createModifiedPropertySupport();

    // ===================================================================================
    //                                                                   Annotation Reader
    //                                                                   =================
    protected abstract TnBeanAnnotationReader createBeanAnnotationReader(Class<?> beanClass);

    // ===================================================================================
    //                                                                     Optimistic Lock
    //                                                                     ===============
    protected String getVersionNoPropertyName(TnBeanAnnotationReader beanAnnotationReader) {
        final String defaultName = "versionNo"; // VERSION_NO is special name
        final String name = beanAnnotationReader.getVersionNoPropertyName();
        return name != null ? name : defaultName;
    }

    protected String getTimestampPropertyName(TnBeanAnnotationReader beanAnnotationReader) {
        return beanAnnotationReader.getTimestampPropertyName(); // has no default name
    }

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    protected TnPropertyTypeFactory createPropertyTypeFactory(Class<?> beanClass,
            TnBeanAnnotationReader beanAnnotationReader, DatabaseMetaData dbMetaData) {
        return createPropertyTypeFactoryBuilder(dbMetaData).build(beanClass, beanAnnotationReader);
    }

    protected TnPropertyTypeFactoryBuilder createPropertyTypeFactoryBuilder(DatabaseMetaData dbMetaData) {
        return new TnPropertyTypeFactoryBuilderImpl(); // is already customized for DBFlute (no use dynamic meta data)
    }

    protected abstract TnRelationPropertyTypeFactory createRelationPropertyTypeFactory(Class<?> beanClass,
            TnBeanMetaDataImpl localBeanMetaData, TnBeanAnnotationReader beanAnnotationReader,
            DatabaseMetaData dbMetaData, int relationNestLevel, boolean stopRelationCreation);

    // ===================================================================================
    //                                                                 Relation Next Level
    //                                                                 ===================
    protected boolean isLimitRelationNestLevel(int relationNestLevel) {
        return relationNestLevel == getLimitRelationNestLevel();
    }

    protected abstract int getLimitRelationNestLevel(); // return 1 if S2Dao

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setDataSource(DataSource dataSource) {
        _dataSource = dataSource;
    }
}
