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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public abstract class DfAbstractDiff {

    // ===================================================================================
    //                                                                         Create Diff
    //                                                                         ===========
    protected DfTableDiff createTableDiff(Map<String, Object> tableDiffMap) {
        return DfTableDiff.createFromDiffMap(tableDiffMap);
    }

    protected DfColumnDiff createColumnDiff(Map<String, Object> columnDiffMap) {
        return DfColumnDiff.createFromDiffMap(columnDiffMap);
    }

    protected DfPrimaryKeyDiff createPrimaryKeyDiff(Map<String, Object> primaryKeyDiffMap) {
        return DfPrimaryKeyDiff.createFromDiffMap(primaryKeyDiffMap);
    }

    protected DfForeignKeyDiff createForeignKeyDiff(Map<String, Object> foreignKeyDiffMap) {
        return DfForeignKeyDiff.createFromDiffMap(foreignKeyDiffMap);
    }

    protected DfUniqueKeyDiff createUniqueKeyDiff(Map<String, Object> uniqueKeyDiffMap) {
        return DfUniqueKeyDiff.createFromDiffMap(uniqueKeyDiffMap);
    }

    protected DfIndexDiff createIndexDiff(Map<String, Object> indexDiffMap) {
        return DfIndexDiff.createFromDiffMap(indexDiffMap);
    }

    protected DfSequenceDiff createSequenceDiff(Map<String, Object> sequenceDiffMap) {
        return DfSequenceDiff.createFromDiffMap(sequenceDiffMap);
    }

    protected DfProcedureDiff createProcedureDiff(Map<String, Object> procedureDiffMap) {
        return DfProcedureDiff.createFromDiffMap(procedureDiffMap);
    }

    protected DfCraftTitleDiff createCraftDiffTitle(Map<String, Object> craftDiffTitleMap) {
        return DfCraftTitleDiff.createFromDiffMap(craftDiffTitleMap);
    }

    protected DfCraftRowDiff createCraftRowDiff(Map<String, Object> craftDiffRowMap) {
        return DfCraftRowDiff.createFromDiffMap(craftDiffRowMap);
    }

    // ===================================================================================
    //                                                                  Next Previous Diff
    //                                                                  ==================
    protected DfNextPreviousDiff createNextPreviousDiff(String next, String previous) {
        return DfNextPreviousDiff.create(next, previous);
    }

    protected DfNextPreviousDiff createNextPreviousDiff(Integer next, Integer previous) {
        return DfNextPreviousDiff.create(next.toString(), previous.toString());
    }

    protected DfNextPreviousDiff createNextPreviousDiff(Boolean next, Boolean previous) {
        return DfNextPreviousDiff.create(next.toString(), previous.toString());
    }

    protected DfNextPreviousDiff restoreNextPreviousDiff(Map<String, Object> diffMap, String key) {
        return doRestoreNextPreviousDiff(diffMap, key, false);
    }

    protected DfNextPreviousDiff restoreNextPreviousDiffUnquote(Map<String, Object> diffMap, String key) {
        return doRestoreNextPreviousDiff(diffMap, key, true);
    }

    protected DfNextPreviousDiff doRestoreNextPreviousDiff(Map<String, Object> diffMap, String key, boolean unquote) {
        final Object value = diffMap.get(key);
        if (value == null) {
            return null;
        }
        assertElementValueMap(key, value, diffMap);
        @SuppressWarnings("unchecked")
        final Map<String, Object> nextPreviousDiffMap = (Map<String, Object>) value;
        if (unquote) {
            return DfNextPreviousDiff.createUnquote(nextPreviousDiffMap);
        } else {
            return DfNextPreviousDiff.create(nextPreviousDiffMap);
        }
    }

    protected static interface NextPreviousDiffer<OBJECT, DIFF, TYPE> {
        TYPE provide(OBJECT obj);

        boolean isMatch(TYPE next, TYPE previous);

        void diff(DIFF diff, DfNextPreviousDiff nextPreviousDiff);

        String disp(TYPE obj, boolean next);
    }

    protected abstract class StringNextPreviousDiffer<OBJECT, DIFF> implements NextPreviousDiffer<OBJECT, DIFF, String> {
        public boolean isMatch(String next, String previous) {
            return isSame(next, previous);
        }

        public String disp(String obj, boolean next) {
            return obj;
        }
    }

    // *trimming option is under review
    //protected abstract class TrimmedStringNextPreviousDiffer<OBJECT, DIFF> extends
    //        StringNextPreviousDiffer<OBJECT, DIFF> {
    //    @Override
    //    public boolean isMatch(String next, String previous) {
    //        next = next != null ? next.trim() : null;
    //        previous = previous != null ? previous.trim() : null;
    //        return super.isMatch(next, previous);
    //    }
    //
    //    @Override
    //    public String disp(String obj, boolean next) {
    //        return obj != null ? obj.trim() : null;
    //    }
    //}

    protected abstract class BooleanNextPreviousDiffer<OBJECT, DIFF> implements
            NextPreviousDiffer<OBJECT, DIFF, Boolean> {
        public boolean isMatch(Boolean next, Boolean previous) {
            return isSame(next, previous);
        }

        public String disp(Boolean obj, boolean next) {
            return obj != null ? obj.toString() : null;
        }
    }

    public static interface NextPreviousHandler { // accessed from Velocity template
        String titleName();

        String propertyName();

        DfNextPreviousDiff provide();

        void save(Map<String, Object> diffMap);

        void restore(Map<String, Object> diffMap);
    }

    public static abstract class NextPreviousHandlerBase implements NextPreviousHandler {
        public void save(Map<String, Object> diffMap) {
            if (provide() != null) {
                doSave(diffMap);
            }
        }

        protected void doSave(Map<String, Object> diffMap) {
            diffMap.put(propertyName(), createSavedNextPreviousDiffMap());
        }

        protected Map<String, String> createSavedNextPreviousDiffMap() {
            return provide().createNextPreviousDiffMap();
        }

        protected void quoteDispIfNeeds() {
            if (provide() != null) {
                provide().quoteDispIfNeeds();
            }
        }
    }

    // ===================================================================================
    //                                                                           Nest Diff
    //                                                                           =========
    protected void restoreNestDiff(Map<String, Object> parentDiffMap, NestDiffSetupper setupper) {
        final String key = setupper.propertyName();
        final Object value = parentDiffMap.get(key);
        if (value == null) {
            return;
        }
        assertElementValueMap(key, value, parentDiffMap);
        @SuppressWarnings("unchecked")
        final Map<String, Object> diffAllMap = (Map<String, Object>) value;
        final Set<Entry<String, Object>> entrySet = diffAllMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            final String name = entry.getKey();
            final Object diffObj = entry.getValue();
            assertElementValueMap(name, diffObj, diffAllMap);
            @SuppressWarnings("unchecked")
            final Map<String, Object> nestDiffMap = (Map<String, Object>) diffObj;
            setupper.setup(nestDiffMap);
        }
    }

    protected static interface NestDiffSetupper {
        String propertyName();

        List<? extends DfNestDiff> provide();

        void setup(Map<String, Object> diff);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return DfBuildProperties.getInstance().getBasicProperties().getDatabaseTypeFacadeProp();
    }

    protected boolean isDatabaseMySQL() {
        return getDatabaseTypeFacadeProp().isDatabaseMySQL();
    }

    protected boolean isDatabasePostgreSQL() {
        return getDatabaseTypeFacadeProp().isDatabasePostgreSQL();
    }

    protected boolean isDatabaseOracle() {
        return getDatabaseTypeFacadeProp().isDatabaseOracle();
    }

    protected boolean isDatabaseDB2() {
        return getDatabaseTypeFacadeProp().isDatabaseDB2();
    }

    protected boolean isDatabaseSQLServer() {
        return getDatabaseTypeFacadeProp().isDatabaseSQLServer();
    }

    protected boolean isDatabaseH2() {
        return getDatabaseTypeFacadeProp().isDatabaseH2();
    }

    protected boolean isDatabaseDerby() {
        return getDatabaseTypeFacadeProp().isDatabaseDerby();
    }

    // ===================================================================================
    //                                                                         Same Helper
    //                                                                         ===========
    protected boolean isSame(Object next, Object previous) {
        return DfDiffAssist.isSame(next, previous);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertElementValueMap(String key, Object value, Map<String, Object> diffMap) {
        if (!(value instanceof Map<?, ?>)) { // basically no way
            String msg = "The element in diff-map should be Map:";
            msg = msg + " key=" + key + " value=" + value + " diffMap=" + diffMap;
            throw new IllegalStateException(msg);
        }
    }
}
