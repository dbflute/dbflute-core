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
package org.seasar.dbflute.s2dao.metadata;

import java.util.List;

/**
 * The property type for relation. <br />
 * This interface provides relation meta info.
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnRelationPropertyType extends TnPropertyType {

    /**
     * Get the relation No, which indicates number in the base table's relations. 
     * @return The value of Integer. (NotMinus)
     */
    int getRelationNo();

    /**
     * Get the suffix part of relation No for relation path that indicates unique location. <br /> 
     * This suffix is added to selected column label.
     * @return The suffix string. e.g. _0 (NotNull)
     */
    String getRelationNoSuffixPart();

    /**
     * Get the size of key, which means how many relation keys exist.
     * @return The value of Integer. (NotMinus, NotZero: no-key, no-relation)
     */
    int getKeySize();

    /**
     * Get the my key, which is local column DB name as relation key, by the key index.
     * @param index The index to find the corresponding relation key.
     * @return The found DB name of local column. (NotNull)
     */
    String getMyKey(int index);

    /**
     * Get the your key, which is foreign column DB name as relation key, by the key index. 
     * @param index The index to find the corresponding relation key.
     * @return The found DB name of foreign column. (NotNull)
     */
    String getYourKey(int index);

    /**
     * Is the column in foreign columns?
     * @param columnName The DB name of column. (NotNull)
     * @return The determination, true or false.
     */
    boolean isYourKey(String columnName);

    /**
     * Get the my bean meta data, which is for base point of the relation (local entity).
     * @return The instance of bean meta data. (NotNull)
     */
    TnBeanMetaData getMyBeanMetaData();

    /**
     * Get the your bean meta data, which is for the relation (foreign entity).
     * @return The instance of bean meta data. (NotNull)
     */
    TnBeanMetaData getYourBeanMetaData();

    /**
     * Get the list of property type of unique key (basically primary key).
     * @return The list of property type. (NotNull)
     */
    List<TnPropertyType> getUniquePropertyTypeList();

    /**
     * Does it have simple unique key? (not compound?) <br />
     * Derived method for performance.
     * @return The determination, true or false.
     */
    boolean hasSimpleUniqueKey();

    /**
     * Does it have compound unique key? <br />
     * Derived method for performance.
     * @return The determination, true or false.
     */
    boolean hasCompoundUniqueKey();

    /**
     * Get the property type of simple unique key.
     * @return The property type of simple unique key. (NullAllowed: when not simple)
     */
    TnPropertyType getSimpleUniquePropertyType();
}
