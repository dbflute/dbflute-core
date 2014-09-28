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
package org.seasar.dbflute;

import java.util.Properties;

import org.seasar.dbflute.properties.DfAdditionalForeignKeyProperties;
import org.seasar.dbflute.properties.DfAdditionalPrimaryKeyProperties;
import org.seasar.dbflute.properties.DfAdditionalTableProperties;
import org.seasar.dbflute.properties.DfAdditionalUniqueKeyProperties;
import org.seasar.dbflute.properties.DfAllClassCopyrightProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfBehaviorFilterProperties;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.DfCommonColumnProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfDependencyInjectionProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfFlexDtoProperties;
import org.seasar.dbflute.properties.DfFreeGenProperties;
import org.seasar.dbflute.properties.DfHibernateProperties;
import org.seasar.dbflute.properties.DfIncludeQueryProperties;
import org.seasar.dbflute.properties.DfInfraProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfMultipleFKPropertyProperties;
import org.seasar.dbflute.properties.DfOptimisticLockProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.properties.DfRefreshProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.properties.DfS2jdbcProperties;
import org.seasar.dbflute.properties.DfSequenceIdentityProperties;
import org.seasar.dbflute.properties.DfSimpleDtoProperties;
import org.seasar.dbflute.properties.DfSqlLogRegistryProperties;
import org.seasar.dbflute.properties.DfTypeMappingProperties;
import org.seasar.dbflute.properties.handler.DfPropertiesHandler;

/**
 * The properties to build. <br />
 * Actually this class is provider of various DBFlute properties.
 * @author jflute
 */
public final class DfBuildProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Singleton-instance. */
    private static final DfBuildProperties _instance = new DfBuildProperties();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** Build properties. */
    private Properties _buildProperties;

    /** The version of DBFlute. */
    private Integer _version;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor. (Private for Singleton)
     */
    private DfBuildProperties() {
    }

    /**
     * Get singleton-instance.
     * @return Singleton-instance. (NotNull)
     */
    public synchronized static DfBuildProperties getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Set build-properties.
     * <pre>
     * This method should be invoked at first initialization.
     * Don't invoke with build-properties null.
     * </pre>
     * @param buildProperties Build-properties. (NotNull)
     */
    final public void setProperties(Properties buildProperties) {
        _buildProperties = buildProperties;
    }

    /**
     * Get build-properties.
     * @return Build-properties. (NotNull)
     */
    final public Properties getProperties() {
        return _buildProperties;
    }

    // ===================================================================================
    //                                                                    Property Handler
    //                                                                    ================
    public DfPropertiesHandler getHandler() {
        return DfPropertiesHandler.getInstance();
    }

    // ===================================================================================
    //                                                                     Property Object
    //                                                                     ===============
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public DfBasicProperties getBasicProperties() {
        return getHandler().getBasicProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                Additional Foreign Key
    //                                ----------------------
    public DfAdditionalForeignKeyProperties getAdditionalForeignKeyProperties() {
        return getHandler().getAdditionalForeignKeyProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                Additional Primary Key
    //                                ----------------------
    public DfAdditionalPrimaryKeyProperties getAdditionalPrimaryKeyProperties() {
        return getHandler().getAdditionalPrimaryKeyProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                      Additional Table
    //                                      ----------------
    public DfAdditionalTableProperties getAdditionalTableProperties() {
        return getHandler().getAdditionalTableProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                 Additional Unique Key
    //                                 ---------------------
    public DfAdditionalUniqueKeyProperties getAdditionalUniqueKeyProperties() {
        return getHandler().getAdditionalUniqueKeyProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                   All Class Copyright
    //                                   -------------------
    public DfAllClassCopyrightProperties getAllClassCopyrightProperties() {
        return getHandler().getAllClassCopyrightProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                       Behavior Filter
    //                                       ---------------
    public DfBehaviorFilterProperties getBehaviorFilterProperties() {
        return getHandler().getBehaviorFilterProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                        Classification
    //                                        --------------
    public DfClassificationProperties getClassificationProperties() {
        return getHandler().getClassificationProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                         Common Column
    //                                         -------------
    public DfCommonColumnProperties getCommonColumnProperties() {
        return getHandler().getCommonColumnProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                              Database
    //                                              --------
    public DfDatabaseProperties getDatabaseProperties() {
        return getHandler().getDatabaseProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                  Dependency Injection
    //                                  --------------------
    public DfDependencyInjectionProperties getDependencyInjectionProperties() {
        return getHandler().getDependencyInjectionProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                              Document
    //                                              --------
    public DfDocumentProperties getDocumentProperties() {
        return getHandler().getDocumentProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                              Flex DTO
    //                                              --------
    public DfFlexDtoProperties getFlexDtoProperties() {
        return getHandler().getFlexDtoProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                               FreeGen
    //                                               -------
    public DfFreeGenProperties getFreeGenProperties() {
        return getHandler().getFreeGenProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                             Hibernate
    //                                             ---------
    public DfHibernateProperties getHibernateProperties() {
        return getHandler().getHibernateProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                         Include Query
    //                                         -------------
    public DfIncludeQueryProperties getIncludeQueryProperties() {
        return getHandler().getIncludeQueryProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                                 Infra
    //                                                 -----
    public DfInfraProperties getInfraProperties() {
        return getHandler().getInfraProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                     Little Adjustment
    //                                     -----------------
    public DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getHandler().getLittleAdjustmentProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                  Multiple FK Property
    //                                  --------------------
    public DfMultipleFKPropertyProperties getMultipleFKPropertyProperties() {
        return getHandler().getMultipleFKPropertyProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                       Optimistic Lock
    //                                       ---------------
    public DfOptimisticLockProperties getOptimisticLockProperties() {
        return getHandler().getOptimisticLockProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                            OutsideSql
    //                                            ----------
    public DfOutsideSqlProperties getOutsideSqlProperties() {
        return getHandler().getOutsideSqlProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                               Refresh
    //                                               -------
    public DfRefreshProperties getRefreshProperties() {
        return getHandler().getRefreshProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    public DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getHandler().getReplaceSchemaProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                                S2JDBC
    //                                                ------
    public DfS2jdbcProperties getS2jdbcProperties() {
        return getHandler().getS2JdbcProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                 Sequence and Identity
    //                                 ---------------------
    public DfSequenceIdentityProperties getSequenceIdentityProperties() {
        return getHandler().getSequenceIdentityProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                            Simple DTO
    //                                            ----------
    public DfSimpleDtoProperties getSimpleDtoProperties() {
        return getHandler().getSimpleDtoProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                      SQL Log Registry
    //                                      ----------------
    public DfSqlLogRegistryProperties getSqlLogRegistryProperties() {
        return getHandler().getSqlLogRegistryProperties(getProperties());
    }

    // -----------------------------------------------------
    //                                          Type Mapping
    //                                          ------------
    public DfTypeMappingProperties getTypeMappingProperties() {
        return getHandler().getTypeMappingProperties(getProperties());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Integer getVersion() { // means DBFlute's version
        return _version;
    }

    public void setVersion(Integer version) {
        this._version = version;
    }
}