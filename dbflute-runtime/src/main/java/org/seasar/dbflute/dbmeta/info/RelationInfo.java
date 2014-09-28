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
package org.seasar.dbflute.dbmeta.info;

import java.util.Map;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.DBMeta;

/**
 * The class of relation information.
 * @author jflute
 */
public interface RelationInfo {

    /**
     * Get the name of the relation constraint. <br />
     * The foreign info and the referrer info of the same relation have the same name.
     * @return The name of the relation constraint. (NotNull)
     */
    String getConstraintName();

    /**
     * Get the property name of the relation. <br />
     * This is unique name in the table.
     * @return The property name of the relation. (NotNull)
     */
    String getRelationPropertyName();

    /**
     * Get the DB meta of the local table. <br />
     * For example, if the relation MEMBER and MEMBER_STATUS, this returns MEMBER's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    DBMeta getLocalDBMeta();

    /**
     * Get the DB meta of the target table. <br />
     * For example, if the relation MEMBER and MEMBER_STATUS, this returns MEMBER_STATUS's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    DBMeta getTargetDBMeta();

    /**
     * Get the read-only map, key is a local column info, value is a target column info.
     * @return The read-only map. (NotNull)
     */
    Map<ColumnInfo, ColumnInfo> getLocalTargetColumnInfoMap();

    /**
     * Get the native type mapped to object for the column. (NOT property access type) <br />
     * It returns basically relation entity type even if the property type is optional. <br />
     * And also there is the other method that returns property access type.
     * @return The class type for the relation entity. (NotNull)
     */
    Class<?> getObjectNativeType();

    /**
     * Get the type of property access for the relation. <br />
     * It is defined at getter/setter in entity. (e.g. Entity or Optional) <br />
     * And also there is the other method that always returns object native type.
     * @return The class type to access the property, e.g. Entity or Optional. (NotNull)
     */
    Class<?> getPropertyAccessType();

    /**
     * Does the relation is one-to-one?
     * @return The determination, true or false.
     */
    boolean isOneToOne();

    /**
     * Does the relation is referrer?
     * @return The determination, true or false.
     */
    boolean isReferrer();

    /**
     * Get the relation info of reverse relation.
     * @return The instance of relation info. (NullAllowed: if null, means one-way reference)
     */
    RelationInfo getReverseRelation();

    /**
     * Read the value to the entity by its gateway (means no reflection). <br />
     * It returns plain value in entity as property access type.
     * @param <PROPERTY> The type of property, might be optional or list.
     * @param localEntity The local entity of this column to read. (NotNull)
     * @return The read instance of relation entity, might be optional or list. (NotNull: when optional or list, NullAllowed: when native type)
     */
    <PROPERTY> PROPERTY read(Entity localEntity);

    /**
     * Write the value to the entity by its gateway (means no reflection). <br />
     * No converting to optional so check the property access type.
     * @param localEntity The local entity of this column to write. (NotNull)
     * @param foreignEntity The written instance of relation entity, might be optional or list. (NullAllowed: if null, null written)
     */
    void write(Entity localEntity, Object foreignEntity);
}
