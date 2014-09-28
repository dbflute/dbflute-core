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

import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.Unique;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfUniqueKeyDiff;

/**
 * @author jflute
 */
public class DfUniqueKeyDiffer extends DfBasicConstraintKeyDiffer<Unique, DfUniqueKeyDiff> {

    public DfUniqueKeyDiffer(DfTableDiff tableDiff) {
        super(tableDiff);
    }

    public String constraintName(Unique key) {
        return key.getName();
    }

    public List<Unique> keyList(Table table) {
        return table.getUniqueList();
    }

    public String column(Unique key) {
        return buildCommaString(key.getIndexColumnMap().values());
    }

    public void diff(DfUniqueKeyDiff diff, Unique nextKey, Unique previousKey) {
        if (diff.hasDiff()) {
            _tableDiff.addUniqueKeyDiff(diff);
        }
    }

    public DfUniqueKeyDiff createAddedDiff(String constraintName) {
        return DfUniqueKeyDiff.createAdded(constraintName);
    }

    public DfUniqueKeyDiff createChangedDiff(String constraintName) {
        return DfUniqueKeyDiff.createChanged(constraintName);
    }

    public DfUniqueKeyDiff createDeletedDiff(String constraintName) {
        return DfUniqueKeyDiff.createDeleted(constraintName);
    }
}
