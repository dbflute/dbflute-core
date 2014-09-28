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
public class DfUniqueKeyDiff extends DfConstraintDiff {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfUniqueKeyDiff(String columnName, DfDiffType diffType) {
        super(columnName, diffType);
    }

    protected DfUniqueKeyDiff(Map<String, Object> uniqueKeyDiffMap) {
        super(uniqueKeyDiffMap);
    }

    public static DfUniqueKeyDiff createAdded(String constraintName) {
        return new DfUniqueKeyDiff(constraintName, DfDiffType.ADD);
    }

    public static DfUniqueKeyDiff createChanged(String constraintName) {
        return new DfUniqueKeyDiff(constraintName, DfDiffType.CHANGE);
    }

    public static DfUniqueKeyDiff createDeleted(String constraintName) {
        return new DfUniqueKeyDiff(constraintName, DfDiffType.DELETE);
    }

    public static DfUniqueKeyDiff createFromDiffMap(Map<String, Object> uniqueKeyDiffMap) {
        return new DfUniqueKeyDiff(uniqueKeyDiffMap);
    }
}
