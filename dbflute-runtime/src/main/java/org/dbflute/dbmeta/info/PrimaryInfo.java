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
package org.dbflute.dbmeta.info;

import java.util.List;

import org.dbflute.dbmeta.DBMeta;

/**
 * The information of primary key constraint.
 * @author jflute
 * @since 1.1.0-sp1 (2015/01/19 Monday)
 */
public class PrimaryInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UniqueInfo _uniqueInfo;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PrimaryInfo(UniqueInfo uniqueInfo) {
        _uniqueInfo = uniqueInfo;
    }

    // ===================================================================================
    //                                                                    Column Existence
    //                                                                    ================
    /**
     * Does the primary key contain the column?
     * @param columnInfo The judged column. (NotNull)
     * @return The determination, true or false.
     */
    public boolean containsColumn(ColumnInfo columnInfo) {
        return _uniqueInfo.containsColumn(columnInfo);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _uniqueInfo.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PrimaryInfo)) {
            return false;
        }
        final PrimaryInfo target = (PrimaryInfo) obj;
        return this._uniqueInfo.equals(target._uniqueInfo);
    }

    @Override
    public String toString() {
        return "{" + _uniqueInfo.getDBMeta().getTableDbName() + "." + _uniqueInfo.getUniqueColumnList() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the DB meta of the primary key's table.
     * @return The instance of DB meta. (NotNull)
     */
    public DBMeta getDBMeta() {
        return _uniqueInfo.getDBMeta();
    }

    /**
     * Get the read-only list of primary column.
     * @return The read-only list of primary column. (NotNull)
     */
    public List<ColumnInfo> getPrimaryColumnList() {
        return _uniqueInfo.getUniqueColumnList(); // as snapshot
    }

    /**
     * Get the column information of the first in primary columns.
     * @return The column information of the first in primary columns. (NotNull)
     */
    public ColumnInfo getFirstColumn() {
        return _uniqueInfo.getFirstColumn();
    }

    /**
     * Is the primary key key compound key?
     * @return The determination, true or false.
     */
    public boolean isCompoundKey() {
        return _uniqueInfo.isCompoundKey();
    }

    /**
     * Get the unique info as the primary key.
     * @return The cached instance of unique info. (NotNull)
     */
    public UniqueInfo getUniqueInfo() {
        return _uniqueInfo;
    }
}
