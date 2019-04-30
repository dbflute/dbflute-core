/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.s2dao.metadata;

import java.util.List;
import java.util.Set;

import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.s2dao.identity.TnIdentifierGenerator;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnBeanMetaData {

    // ===================================================================================
    //                                                                          Basic Info
    //                                                                          ==========
    /**
     * Get the type of bean.
     * @return The type of bean. (NotNull) 
     */
    Class<?> getBeanClass();

    /**
     * Get the DB meta of bean.
     * @return The instance of DB meta. (NullAllowed: but if it's DBFlute entity, NotNull)
     */
    DBMeta getDBMeta();

    /**
     * Get the table name of the bean.
     * @return The name of table.  (NotNull: if it's not entity, this value is 'df:Unknown')
     */
    String getTableName();

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    /**
     * Get the list of property type.
     * @return The list of property type. (NotNull)
     */
    List<TnPropertyType> getPropertyTypeList();

    /**
     * Get the property type by the key as case insensitive.
     * @param propertyName The name of property. (NotNull)
     * @return The type of property. (NullAllowed)
     */
    TnPropertyType getPropertyType(String propertyName);

    /**
     * Does it has the property type by the key as case insensitive.
     * @param propertyName The name of property. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasPropertyType(String propertyName);

    TnPropertyType getPropertyTypeByAliasName(String aliasName);

    TnPropertyType getPropertyTypeByColumnName(String columnName);

    boolean hasPropertyTypeByColumnName(String columnName);

    boolean hasPropertyTypeByAliasName(String aliasName);

    String convertFullColumnName(String alias);

    // ===================================================================================
    //                                                                     Optimistic Lock
    //                                                                     ===============
    TnPropertyType getVersionNoPropertyType();

    String getVersionNoPropertyName();

    boolean hasVersionNoPropertyType();

    TnPropertyType getTimestampPropertyType();

    String getTimestampPropertyName();

    boolean hasTimestampPropertyType();

    // ===================================================================================
    //                                                              Relation Property Type
    //                                                              ======================
    List<TnRelationPropertyType> getRelationPropertyTypeList();

    int getRelationPropertyTypeSize();

    TnRelationPropertyType getRelationPropertyType(int index);

    TnRelationPropertyType getRelationPropertyType(String propertyName);

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    int getPrimaryKeySize();

    String getPrimaryKeyDbName(int index);

    ColumnSqlName getPrimaryKeySqlName(int index);

    int getIdentifierGeneratorSize();

    TnIdentifierGenerator getIdentifierGenerator(int index);

    TnIdentifierGenerator getIdentifierGenerator(String propertyName);

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    Set<String> getModifiedPropertyNames(Object bean);
}
