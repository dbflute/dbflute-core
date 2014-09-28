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
package org.seasar.dbflute.logic.jdbc.schemadiff.differ;

import java.util.List;

import org.apache.torque.engine.database.model.Index;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfIndexDiff;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfTableDiff;

/**
 * @author jflute
 */
public class DfIndexDiffer extends DfBasicConstraintKeyDiffer<Index, DfIndexDiff> {

    public DfIndexDiffer(DfTableDiff tableDiff) {
        super(tableDiff);
    }

    public String constraintName(Index key) {
        return key.getName();
    }

    public List<Index> keyList(Table table) {
        return table.getIndexList();
    }

    public String column(Index key) {
        return buildCommaString(key.getIndexColumnMap().values());
    }

    public void diff(DfIndexDiff diff, Index nextKey, Index previousKey) {
        if (diff.hasDiff()) {
            _tableDiff.addIndexDiff(diff);
        }
    }

    public DfIndexDiff createAddedDiff(String constraintName) {
        return DfIndexDiff.createAdded(constraintName);
    }

    public DfIndexDiff createChangedDiff(String constraintName) {
        return DfIndexDiff.createChanged(constraintName);
    }

    public DfIndexDiff createDeletedDiff(String constraintName) {
        return DfIndexDiff.createDeleted(constraintName);
    }
}
