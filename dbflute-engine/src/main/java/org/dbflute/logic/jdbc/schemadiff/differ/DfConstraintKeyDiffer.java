/*
 * Copyright 2014-2022 the original author or authors.
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

import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.jdbc.schemadiff.DfConstraintDiff;

/**
 * @author jflute
 * @param <KEY> The type of constraint key.
 * @param <DIFF> The type of constraint diff.
 */
public interface DfConstraintKeyDiffer<KEY, DIFF extends DfConstraintDiff> {

    List<KEY> keyList(Table table);

    String constraintName(KEY key);

    String column(KEY key);

    boolean isSameConstraintName(String next, String previous);

    boolean isAutoGeneratedName(String name);

    boolean isSameStructure(KEY next, KEY previous);

    void diff(DIFF diff, KEY nextKey, KEY previousKey);

    DIFF createAddedDiff(String constraintName);

    DIFF createChangedDiff(String constraintName);

    DIFF createDeletedDiff(String constraintName);
}
