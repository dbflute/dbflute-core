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
import org.dbflute.dbmeta.info.ReferrerInfo;
import org.dbflute.dbmeta.info.RelationInfo;

/**
 * The information of structural referrer as-many. <br>
 * The "structural referrer" means one-to-many + one-to-one referrers wholly.
 * 
 * <p>Independent interface style for logical compatible
 * with existing info classes (e.g. RelationInfo interface).
 * It's KUNIKU-NO-SAKU in Japanese.</p>
 * 
 * @author jflute
 * @since 1.3.0 (2025/07/12 Saturday at ichihara)
 */
public class StructuralReferrerAsManyInfo implements StructuralReferrerInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final ReferrerInfo referrerInfo; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public StructuralReferrerAsManyInfo(ReferrerInfo referrerInfo) {
        this.referrerInfo = referrerInfo;
    }

    // ===================================================================================
    //                                                                       Relation Name
    //                                                                       =============
    public String getConstraintName() {
        return referrerInfo.getConstraintName();
    }

    public String getRelationPropertyName() {
        return referrerInfo.getRelationPropertyName();
    }

    // ===================================================================================
    //                                                                            Metadata
    //                                                                            ========
    public DBMeta getLocalDBMeta() {
        return referrerInfo.getLocalDBMeta(); // e.g. MEMBER's
    }

    public DBMeta getReferrerDBMeta() {
        return referrerInfo.getReferrerDBMeta(); // e.g. PURCHASE's
    }

    public Map<ColumnInfo, ColumnInfo> getLocalReferrerColumnInfoMap() { // e.g. { MEMBER's = PURCHASE's }
        return referrerInfo.getLocalReferrerColumnInfoMap();
    }

    public Map<ColumnInfo, ColumnInfo> getReferrerLocalColumnInfoMap() { // e.g. { PURCHASE's = MEMBER's }
        return referrerInfo.getReferrerLocalColumnInfoMap();
    }

    // ===================================================================================
    //                                                              Programing Information
    //                                                              ======================
    public Class<?> getObjectNativeType() {
        return referrerInfo.getObjectNativeType();
    }

    public Class<?> getPropertyAccessType() {
        return referrerInfo.getPropertyAccessType();
    }

    // ===================================================================================
    //                                                          Relationship Determination
    //                                                          ==========================
    public boolean isOneToOne() {
        return referrerInfo.isOneToOne();
    }

    public boolean isAdditionalFK() {
        // #hope jflute essentially ReferrerInfo should have the determination method (2025/07/12)
        final RelationInfo reverseRelation = referrerInfo.getReverseRelation();
        if (reverseRelation != null && reverseRelation instanceof ForeignInfo) { // should be true
            // referrerAsMany always has reverse because referrer-only relation does not exist.
            return ((ForeignInfo) reverseRelation).isAdditionalFK();
        }
        // no way just in case
        String msg = "Unknown reverse relation: " + referrerInfo + " :: " + reverseRelation;
        throw new IllegalStateException(msg);
    }

    public boolean isCompoundKey() {
        return referrerInfo.isCompoundKey();
    }

    // ===================================================================================
    //                                                                 Reverse Information
    //                                                                 ===================
    public ForeignInfo getReverseForeign() {
        final RelationInfo reverseRelation = referrerInfo.getReverseRelation(); // basically not null
        if (reverseRelation instanceof ForeignInfo) {
            return (ForeignInfo) reverseRelation;
        }
        // no way, just in case
        String msg = "Unknown reverse relation: referrer=" + referrerInfo + ", reverse=" + reverseRelation;
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                          Reflection
    //                                                                          ==========
    public <PROPERTY> PROPERTY read(Entity localEntity) {
        return referrerInfo.read(localEntity);
    }

    public void write(Entity localEntity, Object relationEntityObj) {
        referrerInfo.write(localEntity, relationEntityObj);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "structuralAsMany:{" + referrerInfo + "}";
    }
}
