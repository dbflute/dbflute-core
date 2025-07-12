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
import org.dbflute.dbmeta.info.RelationInfo;

/**
 * The information of structural referrer to-one. <br>
 * The "structural referrer" means one-to-many + one-to-one referrers wholly.
 * 
 * <p>Independent interface style for logical compatible
 * with existing info classes (e.g. RelationInfo interface).
 * It's KUNIKU-NO-SAKU in Japanese.</p>
 * 
 * @author jflute
 * @since 1.3.0 (2025/07/12 Saturday at ichihara)
 */
public class StructuralReferrerAsOneInfo implements StructuralReferrerInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final ForeignInfo foreignInfo; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public StructuralReferrerAsOneInfo(ForeignInfo foreignInfo) {
        this.foreignInfo = foreignInfo;
    }

    // ===================================================================================
    //                                                                       Relation Name
    //                                                                       =============
    public String getConstraintName() {
        return foreignInfo.getConstraintName();
    }

    public String getRelationPropertyName() {
        return foreignInfo.getRelationPropertyName();
    }

    // ===================================================================================
    //                                                                            Metadata
    //                                                                            ========
    public DBMeta getLocalDBMeta() {
        return foreignInfo.getLocalDBMeta(); // e.g. MEMBER's
    }

    public DBMeta getReferrerDBMeta() {
        return foreignInfo.getForeignDBMeta(); // e.g. MEMBER_SECURITY's
    }

    public Map<ColumnInfo, ColumnInfo> getLocalReferrerColumnInfoMap() { // e.g. { MEMBER's = MEMBER_SECURITY's }
        return foreignInfo.getLocalForeignColumnInfoMap();
    }

    public Map<ColumnInfo, ColumnInfo> getReferrerLocalColumnInfoMap() { // e.g. { MEMBER_SECURITY's = MEMBER's }
        return foreignInfo.getForeignLocalColumnInfoMap();
    }

    // ===================================================================================
    //                                                              Programing Information
    //                                                              ======================
    public Class<?> getObjectNativeType() {
        return foreignInfo.getObjectNativeType();
    }

    public Class<?> getPropertyAccessType() {
        return foreignInfo.getPropertyAccessType();
    }

    // ===================================================================================
    //                                                          Relationship Determination
    //                                                          ==========================
    public boolean isOneToOne() {
        return foreignInfo.isOneToOne();
    }

    public boolean isAdditionalFK() {
        return foreignInfo.isAdditionalFK();
    }

    public boolean isCompoundKey() {
        return foreignInfo.isCompoundKey();
    }

    // ===================================================================================
    //                                                                 Reverse Information
    //                                                                 ===================
    public ForeignInfo getReverseForeign() {
        final RelationInfo reverseRelation = foreignInfo.getReverseRelation(); // basically not null
        if (reverseRelation instanceof ForeignInfo) {
            return (ForeignInfo) reverseRelation;
        }
        // no way, just in case
        String msg = "Unknown reverse relation: referrer=" + foreignInfo + ", reverse=" + reverseRelation;
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                          Reflection
    //                                                                          ==========
    public <PROPERTY> PROPERTY read(Entity localEntity) {
        return foreignInfo.read(localEntity);
    }

    public void write(Entity localEntity, Object relationEntityObj) {
        foreignInfo.write(localEntity, relationEntityObj);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "structuralAsOne:{" + foreignInfo + "}";
    }
}
