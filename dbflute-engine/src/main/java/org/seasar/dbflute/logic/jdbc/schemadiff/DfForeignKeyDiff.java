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
package org.seasar.dbflute.logic.jdbc.schemadiff;

import java.util.Map;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfForeignKeyDiff extends DfConstraintDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _foreignTableDiff;

    {
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "FK Table";
            }

            public String propertyName() {
                return "foreignTableDiff";
            }

            public DfNextPreviousDiff provide() {
                return _foreignTableDiff;
            }

            public void restore(Map<String, Object> foreignKeyDiffMap) {
                _foreignTableDiff = restoreNextPreviousDiff(foreignKeyDiffMap, propertyName());
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfForeignKeyDiff(String columnName, DfDiffType diffType) {
        super(columnName, diffType);
    }

    protected DfForeignKeyDiff(Map<String, Object> foreignKeyDiffMap) {
        super(foreignKeyDiffMap);
    }

    public static DfForeignKeyDiff createAdded(String constraintName) {
        return new DfForeignKeyDiff(constraintName, DfDiffType.ADD);
    }

    public static DfForeignKeyDiff createChanged(String constraintName) {
        return new DfForeignKeyDiff(constraintName, DfDiffType.CHANGE);
    }

    public static DfForeignKeyDiff createDeleted(String constraintName) {
        return new DfForeignKeyDiff(constraintName, DfDiffType.DELETE);
    }

    public static DfForeignKeyDiff createFromDiffMap(Map<String, Object> columnDiffMap) {
        return new DfForeignKeyDiff(columnDiffMap);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    public boolean hasForeignTableDiff() {
        return _foreignTableDiff != null;
    }

    public DfNextPreviousDiff getForeignTableDiff() {
        return _foreignTableDiff;
    }

    public void setForeignTableDiff(DfNextPreviousDiff foreignTableDiff) {
        _foreignTableDiff = foreignTableDiff;
    }
}
