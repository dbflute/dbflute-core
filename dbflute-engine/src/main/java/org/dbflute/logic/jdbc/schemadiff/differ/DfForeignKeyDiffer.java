/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.jdbc.schemadiff.differ;

import java.util.List;

import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.jdbc.schemadiff.DfForeignKeyDiff;
import org.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfForeignKeyDiffer extends DfBasicConstraintKeyDiffer<ForeignKey, DfForeignKeyDiff> {

    public DfForeignKeyDiffer(DfTableDiff tableDiff) {
        super(tableDiff);
    }

    public String constraintName(ForeignKey key) {
        return key.getName();
    }

    public List<ForeignKey> keyList(Table table) {
        return DfCollectionUtil.newArrayList(table.getForeignKeys());
    }

    public String column(ForeignKey key) {
        return key.getLocalColumnNameCommaString();
    }

    @Override
    public boolean isAutoGeneratedName(String name) {
        if (isDatabaseMySQL()) {
            if (name != null && Srl.containsIgnoreCase(name, "_ibfk_")) {
                return true;
            }
        }
        return super.isAutoGeneratedName(name);
    }

    @Override
    public boolean isSameStructure(ForeignKey next, ForeignKey previous) {
        if (isSame(column(next), column(previous))) {
            if (isSame(next.getForeignTable().getTableDbName(), previous.getForeignTable().getTableDbName())) {
                return true;
            }
        }
        return false;
    }

    public void diff(DfForeignKeyDiff diff, ForeignKey nextKey, ForeignKey previousKey) {
        // foreignTable
        if (nextKey != null && previousKey != null) { // means change
            final String nextFKTable = nextKey.getForeignTableDbName();
            final String previousFKTable = previousKey.getForeignTableDbName();
            if (!isSame(nextFKTable, previousFKTable)) {
                final DfNextPreviousDiff fkTableDiff = createNextPreviousDiff(nextFKTable, previousFKTable);
                diff.setForeignTableDiff(fkTableDiff);
            }
        }
        if (diff.hasDiff()) {
            _tableDiff.addForeignKeyDiff(diff);
        }
    }

    public DfForeignKeyDiff createAddedDiff(String constraintName) {
        return DfForeignKeyDiff.createAdded(constraintName);
    }

    public DfForeignKeyDiff createChangedDiff(String constraintName) {
        return DfForeignKeyDiff.createChanged(constraintName);
    }

    public DfForeignKeyDiff createDeletedDiff(String constraintName) {
        return DfForeignKeyDiff.createDeleted(constraintName);
    }
}
