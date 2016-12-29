/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.properties.handler;

import java.util.Properties;

import org.dbflute.properties.DfAdditionalForeignKeyProperties;
import org.dbflute.properties.DfAdditionalPrimaryKeyProperties;
import org.dbflute.properties.DfAdditionalTableProperties;
import org.dbflute.properties.DfAdditionalUniqueKeyProperties;
import org.dbflute.properties.DfAllClassCopyrightProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfBehaviorFilterProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.DfCommonColumnProperties;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfDependencyInjectionProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfESFluteProperties;
import org.dbflute.properties.DfFlexDtoProperties;
import org.dbflute.properties.DfFreeGenProperties;
import org.dbflute.properties.DfHibernateProperties;
import org.dbflute.properties.DfIncludeQueryProperties;
import org.dbflute.properties.DfInfraProperties;
import org.dbflute.properties.DfLastaFluteProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.DfMultipleFKPropertyProperties;
import org.dbflute.properties.DfOptimisticLockProperties;
import org.dbflute.properties.DfOutsideSqlProperties;
import org.dbflute.properties.DfRefreshProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.properties.DfSchemaPolicyProperties;
import org.dbflute.properties.DfSequenceIdentityProperties;
import org.dbflute.properties.DfSimpleDtoProperties;
import org.dbflute.properties.DfTypeMappingProperties;

/**
 * @author jflute
 */
public final class DfPropertiesHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static DfPropertiesHandler _insntace = new DfPropertiesHandler();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropertiesHandler() {
    }

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    public static DfPropertiesHandler getInstance() {
        return _insntace;
    }

    public void reload() { // for Test
        _insntace = new DfPropertiesHandler();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========

    // -----------------------------------------------------
    //                                Additional Foreign Key
    //                                ----------------------
    protected DfAdditionalForeignKeyProperties _additionalForeignKeyProperties;

    public DfAdditionalForeignKeyProperties getAdditionalForeignKeyProperties(Properties prop) {
        if (_additionalForeignKeyProperties == null) {
            _additionalForeignKeyProperties = new DfAdditionalForeignKeyProperties(prop);
        }
        return _additionalForeignKeyProperties;
    }

    // -----------------------------------------------------
    //                                Additional Primary Key
    //                                ----------------------
    protected DfAdditionalPrimaryKeyProperties _additionalPrimaryKeyProperties;

    public DfAdditionalPrimaryKeyProperties getAdditionalPrimaryKeyProperties(Properties prop) {
        if (_additionalPrimaryKeyProperties == null) {
            _additionalPrimaryKeyProperties = new DfAdditionalPrimaryKeyProperties(prop);
        }
        return _additionalPrimaryKeyProperties;
    }

    // -----------------------------------------------------
    //                                      Additional Table
    //                                      ----------------
    protected DfAdditionalTableProperties _additionalTableProperties;

    public DfAdditionalTableProperties getAdditionalTableProperties(Properties prop) {
        if (_additionalTableProperties == null) {
            _additionalTableProperties = new DfAdditionalTableProperties(prop);
        }
        return _additionalTableProperties;
    }

    // -----------------------------------------------------
    //                                 Additional Unique Key
    //                                 ---------------------
    protected DfAdditionalUniqueKeyProperties _additionalUniqueKeyProperties;

    public DfAdditionalUniqueKeyProperties getAdditionalUniqueKeyProperties(Properties prop) {
        if (_additionalUniqueKeyProperties == null) {
            _additionalUniqueKeyProperties = new DfAdditionalUniqueKeyProperties(prop);
        }
        return _additionalUniqueKeyProperties;
    }

    // -----------------------------------------------------
    //                                   All Class Copyright
    //                                   -------------------
    protected DfAllClassCopyrightProperties _allClassCopyrightProperties;

    public DfAllClassCopyrightProperties getAllClassCopyrightProperties(Properties prop) {
        if (_allClassCopyrightProperties == null) {
            _allClassCopyrightProperties = new DfAllClassCopyrightProperties(prop);
        }
        return _allClassCopyrightProperties;
    }

    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected DfBasicProperties _basicProperties;

    public DfBasicProperties getBasicProperties(Properties prop) {
        if (_basicProperties == null) {
            _basicProperties = new DfBasicProperties(prop);
        }
        return _basicProperties;
    }

    // -----------------------------------------------------
    //                                       Behavior Filter
    //                                       ---------------
    protected DfBehaviorFilterProperties _behaviorFilterProperties;

    public DfBehaviorFilterProperties getBehaviorFilterProperties(Properties prop) {
        if (_behaviorFilterProperties == null) {
            _behaviorFilterProperties = new DfBehaviorFilterProperties(prop);
        }
        return _behaviorFilterProperties;
    }

    // -----------------------------------------------------
    //                                        Classification
    //                                        --------------
    protected DfClassificationProperties _classificationProperties;

    public DfClassificationProperties getClassificationProperties(Properties prop) {
        if (_classificationProperties == null) {
            _classificationProperties = new DfClassificationProperties(prop);
        }
        return _classificationProperties;
    }

    // -----------------------------------------------------
    //                                         Common Column
    //                                         -------------
    protected DfCommonColumnProperties _commonColumnProperties;

    public DfCommonColumnProperties getCommonColumnProperties(Properties prop) {
        if (_commonColumnProperties == null) {
            _commonColumnProperties = new DfCommonColumnProperties(prop);
        }
        return _commonColumnProperties;
    }

    // -----------------------------------------------------
    //                                              Database
    //                                              --------
    protected DfDatabaseProperties _databaseProperties;

    public DfDatabaseProperties getDatabaseProperties(Properties prop) {
        if (_databaseProperties == null) {
            _databaseProperties = new DfDatabaseProperties(prop);
        }
        return _databaseProperties;
    }

    // -----------------------------------------------------
    //                                         DBFlute Dicon
    //                                         -------------
    protected DfDependencyInjectionProperties _dependencyInjectionProperties;

    public DfDependencyInjectionProperties getDependencyInjectionProperties(Properties prop) {
        if (_dependencyInjectionProperties == null) {
            _dependencyInjectionProperties = new DfDependencyInjectionProperties(prop);
        }
        return _dependencyInjectionProperties;
    }

    // -----------------------------------------------------
    //                                              Document
    //                                              --------
    protected DfDocumentProperties _documentProperties;

    public DfDocumentProperties getDocumentProperties(Properties prop) {
        if (_documentProperties == null) {
            _documentProperties = new DfDocumentProperties(prop);
        }
        return _documentProperties;
    }

    // -----------------------------------------------------
    //                                               ESFlute
    //                                               -------
    protected DfESFluteProperties _esfluteProperties;

    public DfESFluteProperties getESFluteProperties(Properties prop) {
        if (_esfluteProperties == null) {
            _esfluteProperties = new DfESFluteProperties(prop);
        }
        return _esfluteProperties;
    }

    // -----------------------------------------------------
    //                                              Flex DTO
    //                                              --------
    protected DfFlexDtoProperties _flexDtoProperties;

    public DfFlexDtoProperties getFlexDtoProperties(Properties prop) {
        if (_flexDtoProperties == null) {
            _flexDtoProperties = new DfFlexDtoProperties(prop);
        }
        return _flexDtoProperties;
    }

    // -----------------------------------------------------
    //                                               FreeGen
    //                                               -------
    protected DfFreeGenProperties _freeGenProperties;

    public DfFreeGenProperties getFreeGenProperties(Properties prop) {
        if (_freeGenProperties == null) {
            _freeGenProperties = new DfFreeGenProperties(prop);
        }
        return _freeGenProperties;
    }

    // -----------------------------------------------------
    //                                             Hibernate
    //                                             ---------
    protected DfHibernateProperties _hibernateProperties;

    public DfHibernateProperties getHibernateProperties(Properties prop) {
        if (_hibernateProperties == null) {
            _hibernateProperties = new DfHibernateProperties(prop);
        }
        return _hibernateProperties;
    }

    // -----------------------------------------------------
    //                                         Include Query
    //                                         -------------
    protected DfIncludeQueryProperties _includeQueryProperties;

    public DfIncludeQueryProperties getIncludeQueryProperties(Properties prop) {
        if (_includeQueryProperties == null) {
            _includeQueryProperties = new DfIncludeQueryProperties(prop);
        }
        return _includeQueryProperties;
    }

    // -----------------------------------------------------
    //                                                 Infra
    //                                                 -----
    protected DfInfraProperties _infraProperties;

    public DfInfraProperties getInfraProperties(Properties prop) {
        if (_infraProperties == null) {
            _infraProperties = new DfInfraProperties(prop);
        }
        return _infraProperties;
    }

    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected DfLastaFluteProperties _lastaflutePropertiess;

    public DfLastaFluteProperties getLastaFluteProperties(Properties prop) {
        if (_lastaflutePropertiess == null) {
            _lastaflutePropertiess = new DfLastaFluteProperties(prop);
        }
        return _lastaflutePropertiess;
    }

    // -----------------------------------------------------
    //                                     Little Adjustment
    //                                     -----------------
    protected DfLittleAdjustmentProperties _littleAdjustmentPropertiess;

    public DfLittleAdjustmentProperties getLittleAdjustmentProperties(Properties prop) {
        if (_littleAdjustmentPropertiess == null) {
            _littleAdjustmentPropertiess = new DfLittleAdjustmentProperties(prop);
        }
        return _littleAdjustmentPropertiess;
    }

    // -----------------------------------------------------
    //                                  Multiple FK Property
    //                                  --------------------
    protected DfMultipleFKPropertyProperties _multipleFKPropertyProperties;

    public DfMultipleFKPropertyProperties getMultipleFKPropertyProperties(Properties prop) {
        if (_multipleFKPropertyProperties == null) {
            _multipleFKPropertyProperties = new DfMultipleFKPropertyProperties(prop);
        }
        return _multipleFKPropertyProperties;
    }

    // -----------------------------------------------------
    //                                            OutsideSql
    //                                            ----------
    protected DfOptimisticLockProperties _optimisticLockProperties;

    public DfOptimisticLockProperties getOptimisticLockProperties(Properties prop) {
        if (_optimisticLockProperties == null) {
            _optimisticLockProperties = new DfOptimisticLockProperties(prop);
        }
        return _optimisticLockProperties;
    }

    // -----------------------------------------------------
    //                                            OutsideSql
    //                                            ----------
    protected DfOutsideSqlProperties _outsideSqlProperties;

    public DfOutsideSqlProperties getOutsideSqlProperties(Properties prop) {
        if (_outsideSqlProperties == null) {
            _outsideSqlProperties = new DfOutsideSqlProperties(prop);
        }
        return _outsideSqlProperties;
    }

    // -----------------------------------------------------
    //                                               Refresh
    //                                               -------
    protected DfRefreshProperties _refreshProperties;

    public DfRefreshProperties getRefreshProperties(Properties prop) {
        if (_refreshProperties == null) {
            _refreshProperties = new DfRefreshProperties(prop);
        }
        return _refreshProperties;
    }

    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected DfReplaceSchemaProperties _replaceSchemaPropertiess;

    public DfReplaceSchemaProperties getReplaceSchemaProperties(Properties prop) {
        if (_replaceSchemaPropertiess == null) {
            _replaceSchemaPropertiess = new DfReplaceSchemaProperties(prop);
        }
        return _replaceSchemaPropertiess;
    }

    // -----------------------------------------------------
    //                                     Sequence Identity
    //                                     -----------------
    protected DfSequenceIdentityProperties _sequenceIdentityProperties;

    public DfSequenceIdentityProperties getSequenceIdentityProperties(Properties prop) {
        if (_sequenceIdentityProperties == null) {
            _sequenceIdentityProperties = new DfSequenceIdentityProperties(prop);
        }
        return _sequenceIdentityProperties;
    }

    // -----------------------------------------------------
    //                                   Schema Policy Check
    //                                   -------------------
    protected DfSchemaPolicyProperties _schemaPolicyCheckProperties;

    public DfSchemaPolicyProperties getSchemaPolicyCheckProperties(Properties prop) {
        if (_schemaPolicyCheckProperties == null) {
            _schemaPolicyCheckProperties = new DfSchemaPolicyProperties(prop);
        }
        return _schemaPolicyCheckProperties;
    }

    // -----------------------------------------------------
    //                                            Simple DTO
    //                                            ----------
    protected DfSimpleDtoProperties _simpleDtoProperties;

    public DfSimpleDtoProperties getSimpleDtoProperties(Properties prop) {
        if (_simpleDtoProperties == null) {
            _simpleDtoProperties = new DfSimpleDtoProperties(prop);
        }
        return _simpleDtoProperties;
    }

    // -----------------------------------------------------
    //                                          Type Mapping
    //                                          ------------
    protected DfTypeMappingProperties _typeMappingProperties;

    public DfTypeMappingProperties getTypeMappingProperties(Properties prop) {
        if (_typeMappingProperties == null) {
            _typeMappingProperties = new DfTypeMappingProperties(prop);
        }
        return _typeMappingProperties;
    }
}