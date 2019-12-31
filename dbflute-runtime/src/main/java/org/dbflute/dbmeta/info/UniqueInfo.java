/*
 * Copyright 2014-2020 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import org.dbflute.dbmeta.DBMeta;

/**
 * The information of unique constraint.
 * @author jflute
 */
public class UniqueInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DBMeta _dbmeta;
    protected final List<ColumnInfo> _uniqueColumnList;
    protected final boolean _primary;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public UniqueInfo(DBMeta dbmeta, List<ColumnInfo> uniqueColumnList, boolean primary) {
        assertObjectNotNull("dbmeta", dbmeta);
        assertObjectNotNull("uniqueColumnList", uniqueColumnList);
        _dbmeta = dbmeta;
        _uniqueColumnList = Collections.unmodifiableList(uniqueColumnList);
        _primary = primary;
    }

    // ===================================================================================
    //                                                                    Column Existence
    //                                                                    ================
    /**
     * Does the unique key contain the column?
     * @param columnInfo The judged column. (NotNull)
     * @return The determination, true or false.
     */
    public boolean containsColumn(ColumnInfo columnInfo) {
        assertObjectNotNull("columnInfo", columnInfo);
        return doContainsColumn(columnInfo.getColumnDbName());
    }

    protected boolean doContainsColumn(String columnName) {
        for (ColumnInfo columnInfo : _uniqueColumnList) {
            if (columnInfo.getColumnDbName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _dbmeta.hashCode() + _uniqueColumnList.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UniqueInfo)) {
            return false;
        }
        final UniqueInfo target = (UniqueInfo) obj;
        if (!this._dbmeta.equals(target.getDBMeta())) {
            return false;
        }
        if (!this._uniqueColumnList.equals(target.getUniqueColumnList())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + _dbmeta.getTableDbName() + "." + _uniqueColumnList + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the DB meta of the unique key's table.
     * @return The instance of DB meta. (NotNull)
     */
    public DBMeta getDBMeta() {
        return _dbmeta;
    }

    /**
     * Get the read-only list of unique column.
     * @return The read-only list of unique column. (NotNull)
     */
    public List<ColumnInfo> getUniqueColumnList() {
        return _uniqueColumnList; // as snapshot
    }

    /**
     * Get the column information of the first in primary columns.
     * @return The column information of the first in primary columns. (NotNull)
     */
    public ColumnInfo getFirstColumn() {
        return _uniqueColumnList.get(0);
    }

    /**
     * Is the unique key compound key?
     * @return The determination, true or false.
     */
    public boolean isCompoundKey() {
        return _uniqueColumnList.size() > 1;
    }

    /**
     * Is the unique key two-or-more keys? (compound key?)
     * @return The determination, true or false.
     * @deprecated use isCompoundKey()
     */
    public boolean isTwoOrMore() { // old style
        return isCompoundKey();
    }

    /**
     * Is the unique key primary key's unique?
     * @return The determination, true or false.
     */
    public boolean isPrimary() {
        return _primary;
    }
}
