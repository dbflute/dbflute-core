/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.dbmeta.info.structural.referrer;

import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.ForeignInfo;

/**
 * The information of (structural) whole referrer. <br>
 * The "structure-logical referrer" means one-to-many + one-to-one referrers wholly.
 * 
 * <p>Independent interface style for logical compatible with existing info classes.
 * It's KUNIKU-NO-SAKU in Japanese.</p>
 * 
 * @author jflute
 * @since 1.3.0 (2025/07/12 Saturday at ichihara)
 */
public interface StructuralReferrerInfo {

    // ===================================================================================
    //                                                                       Relation Name
    //                                                                       =============
    /**
     * Get the name of the relation constraint. <br>
     * The foreign info and the referrer info of the same relation have the same name.
     * @return The name of the relation constraint. (NotNull)
     */
    String getConstraintName();

    /**
     * Get the property name of the relation. <br>
     * This is unique name in the table.
     * @return The property name of the relation. (NotNull)
     */
    String getRelationPropertyName();

    // ===================================================================================
    //                                                                            Metadata
    //                                                                            ========
    /**
     * Get the DB meta of the local table. <br>
     * If the relation is MEMBER from PURCHASE, this returns MEMBER's one. <br>
     * If the relation is MEMBER from MEMBER_SECURITY, this returns MEMBER's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    DBMeta getLocalDBMeta();

    /**
     * Get the DB meta of the referrer table. <br>
     * If the relation is MEMBER from PURCHASE, this returns PURCHASE's one. <br>
     * If the relation is MEMBER from MEMBER_SECURITY, this returns MEMBER_SECURITY's one.
     * @return The DB meta singleton instance. (NotNull)
     */
    DBMeta getReferrerDBMeta();

    /**
     * Get the read-only map, key is a local column info, value is a referrer column info.
     * If the relation is MEMBER from PURCHASE, this returns { MEMBER's PK = PURCHASE's FK }. <br>
     * If the relation is MEMBER from MEMBER_SECURITY, this returns { MEMBER's PK = MEMBER_SECURITY's PK-FK }. <br>
     * @return The read-only map. (NotNull)
     */
    Map<ColumnInfo, ColumnInfo> getLocalReferrerColumnInfoMap();

    /**
     * Get the read-only map, key is a referrer column info, value is a local column info.
     * If the relation is MEMBER from PURCHASE, this returns { PURCHASE's FK = MEMBER's PK}. <br>
     * If the relation is MEMBER from MEMBER_SECURITY, this returns { MEMBER_SECURITY's PK-FK = MEMBER's PK }. <br>
     * @return The read-only map. (NotNull)
     */
    Map<ColumnInfo, ColumnInfo> getReferrerLocalColumnInfoMap();

    // ===================================================================================
    //                                                              Programing Information
    //                                                              ======================
    /**
     * Get the native type mapped to object for the column. (NOT property access type) <br>
     * It returns basically relation entity type even if the property type is optional. <br>
     * And also there is the other method that returns property access type.
     * @return The class type for the relation entity. (NotNull)
     */
    Class<?> getObjectNativeType();

    /**
     * Get the type of property access for the relation. <br>
     * It is defined at getter/setter in entity. (e.g. Entity or Optional) <br>
     * And also there is the other method that always returns object native type.
     * @return The class type to access the property, e.g. Entity or Optional. (NotNull)
     */
    Class<?> getPropertyAccessType();

    // ===================================================================================
    //                                                          Relationship Determination
    //                                                          ==========================
    /**
     * Does the relation is one-to-one? (means referrer-as-one) <br>
     * Is the FK column PK-FK or UQ-FK?
     * @return The determination, true or false.
     */
    boolean isOneToOne();

    /**
     * Does the relation is from additional foreign key? <br>
     * Basically only simple virtual FK pattern because biz-one-t-one does not have referrer.
     * @return The determination, true or false.
     */
    boolean isAdditionalFK();

    /**
     * Is the relation key compound key?
     * @return The determination, true or false.
     */
    boolean isCompoundKey();

    // ===================================================================================
    //                                                                 Reverse Information
    //                                                                 ===================
    /**
     * Get the relation info of reverse relation (fixedly as foreign). <br>
     * e.g. MEMBER's purchaseList (referrer) has PURCHASE's member (foreign) as reverse. <br>
     * e.g. MEMBER's memberSecurityAsOne (referrer) has MEMBER_SECURITY's member (foreign) as reverse. <br>
     * Basically not null, because referrer-only relationship (as-many) does not exist. <br>
     * @return The instance of relation info as foreign. (NotNull)
     */
    ForeignInfo getReverseForeign();

    // ===================================================================================
    //                                                                          Reflection
    //                                                                          ==========
    /**
     * Read the value to the entity by its gateway (means no reflection). <br>
     * It returns plain value in entity as property access type.
     * @param <PROPERTY> The type of property, might be optional or list.
     * @param localEntity The local entity of this column to read. (NotNull)
     * @return The read instance of relation entity, might be optional or list. (NotNull: when optional or list, NullAllowed: when native type)
     */
    <PROPERTY> PROPERTY read(Entity localEntity);

    /**
     * Write the value to the entity by its gateway (means no reflection). <br>
     * No converting to optional so check the property access type.
     * @param localEntity The local entity of this column to write. (NotNull)
     * @param relationEntityObj The written instance of relation entity, might be optional or list. (NullAllowed: if null, null written)
     */
    void write(Entity localEntity, Object relationEntityObj);
}
