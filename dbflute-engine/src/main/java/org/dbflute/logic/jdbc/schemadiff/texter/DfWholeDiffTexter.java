/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.jdbc.schemadiff.texter;

import java.util.List;
import java.util.function.Function;

import org.dbflute.logic.jdbc.schemadiff.DfAbstractDiff.NextPreviousHandler;
import org.dbflute.logic.jdbc.schemadiff.DfCraftRowDiff;
import org.dbflute.logic.jdbc.schemadiff.DfCraftTitleDiff;
import org.dbflute.logic.jdbc.schemadiff.DfNestDiff;
import org.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.dbflute.logic.jdbc.schemadiff.DfProcedureDiff;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.logic.jdbc.schemadiff.DfSequenceDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff;
import org.dbflute.logic.jdbc.schemadiff.DfTableDiff.DfNestDiffContent;
import org.dbflute.system.DBFluteSystem;

/**
 * @author jflute
 * @since 1.3.2 (2026/07/04 Saturday at ichihara)
 */
public class DfWholeDiffTexter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String _ln = DBFluteSystem.ln(); // not null

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaDiff _schemaDiff; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfWholeDiffTexter(DfSchemaDiff schemaDiff) {
        _schemaDiff = schemaDiff;
    }

    // ===================================================================================
    //                                                                            Generate
    //                                                                            ========
    public String generateDiffText() {
        final StringBuilder sb = new StringBuilder();
        buildDiffHeader(sb);

        handleTableDiff(sb);
        handleSequenceDiff(sb);
        handleProcedureDiff(sb);
        handleCraftTitle(sb);

        return sb.toString();
    }

    // ===================================================================================
    //                                                                         Diff Header
    //                                                                         ===========
    protected void buildDiffHeader(StringBuilder sb) {
        sb.append(_schemaDiff.getDiffDate());
        if (_schemaDiff.hasDiffAuthor()) {
            sb.append(" ").append(_schemaDiff.getNativeDiffAuthor());
        }
        if (_schemaDiff.hasDiffGitBranch()) {
            sb.append(" on ").append(_schemaDiff.getNativeDiffGitBranch());
        }
        final DfNextPreviousDiff tableCountDiff = _schemaDiff.getTableCount();
        if (tableCountDiff != null) {
            sb.append(_ln).append("table count: ").append(tableCountDiff.getDisplayPlain());
        }
    }

    // ===================================================================================
    //                                                                          Table Diff
    //                                                                          ==========
    protected void handleTableDiff(StringBuilder sb) {
        buildTableAdded(sb);
        buildTableChanged(sb);
        buildTableDeleted(sb);
    }

    protected void buildTableAdded(StringBuilder sb) {
        final List<DfTableDiff> diffList = _schemaDiff.getAddedTableDiffList();
        setupSimpleDiffTable(sb, "Add", "Table", diffList, diff -> diff.getTableDispName());
    }

    protected void buildTableChanged(StringBuilder sb) {
        final List<DfTableDiff> changedTableDiffList = _schemaDiff.getChangedTableDiffList();
        if (changedTableDiffList.isEmpty()) {
            return;
        }
        appendRootTitleLineSep(sb).append(filterTitle("Change Table"));
        for (DfTableDiff tableDiff : changedTableDiffList) {
            sb.append(_ln).append("  ").append(tableDiff.getTableDispName());
            setupNextPreviousList(sb, tableDiff.getNextPreviousDiffList(), "    ");

            final List<DfNestDiffContent> nestDiffContentOrderedList = tableDiff.getNestDiffContentOrderedList();
            for (DfNestDiffContent nestDiffContent : nestDiffContentOrderedList) {
                sb.append(_ln).append("    ").append(filterTitle(nestDiffContent.getTitleName()));
                final List<? extends DfNestDiff> nestDiffList = nestDiffContent.getNestDiffList();
                for (DfNestDiff nestDiff : nestDiffList) {
                    sb.append(_ln).append("      ").append(nestDiff.getKeyName());
                    setupNextPreviousList(sb, nestDiff.getNextPreviousDiffList(), "        ");
                }
            }
        }
    }

    protected void buildTableDeleted(StringBuilder sb) {
        final List<DfTableDiff> diffList = _schemaDiff.getDeletedTableDiffList();
        setupSimpleDiffTable(sb, "Delete", "Table", diffList, diff -> diff.getTableDispName());
    }

    // ===================================================================================
    //                                                                       Sequence Diff
    //                                                                       =============
    protected void handleSequenceDiff(StringBuilder sb) {
        buildSequenceAdded(sb);
        buildSequenceChanged(sb);
        buildSequenceDeleted(sb);
    }

    protected void buildSequenceAdded(StringBuilder sb) {
        final List<DfSequenceDiff> diffList = _schemaDiff.getAddedSequenceDiffList();
        setupSimpleDiffTable(sb, "Add", "Sequence", diffList, diff -> diff.getSequenceDispName());
    }

    protected void buildSequenceChanged(StringBuilder sb) {
        final List<DfSequenceDiff> sequenceDiffList = _schemaDiff.getChangedSequenceDiffList();
        if (sequenceDiffList.isEmpty()) {
            return;
        }
        appendRootTitleLineSep(sb).append(filterTitle("Change Sequence"));
        for (DfSequenceDiff sequenceDiff : sequenceDiffList) {
            if (sequenceDiff.hasDiff()) {
                sb.append(_ln).append("  ").append(sequenceDiff.getSequenceDispName());
                setupNextPreviousList(sb, sequenceDiff.getNextPreviousDiffList(), "    ");
            }
        }
    }

    protected void buildSequenceDeleted(StringBuilder sb) {
        final List<DfSequenceDiff> diffList = _schemaDiff.getDeletedSequenceDiffList();
        setupSimpleDiffTable(sb, "Delete", "Sequence", diffList, diff -> diff.getSequenceDispName());
    }

    // ===================================================================================
    //                                                                      Procedure Diff
    //                                                                      ==============
    protected void handleProcedureDiff(StringBuilder sb) {
        buildProcedureAdded(sb);
        buildProcedureChanged(sb);
        buildProcedureDeleted(sb);
    }

    protected void buildProcedureAdded(StringBuilder sb) {
        final List<DfProcedureDiff> diffList = _schemaDiff.getAddedProcedureDiffList();
        setupSimpleDiffTable(sb, "Add", "Procedure", diffList, diff -> diff.getProcedureDispName());
    }

    protected void buildProcedureChanged(StringBuilder sb) {
        final List<DfProcedureDiff> procedureDiffList = _schemaDiff.getChangedProcedureDiffList();
        if (procedureDiffList.isEmpty()) {
            return;
        }
        appendRootTitleLineSep(sb).append(filterTitle("Change Procedure"));
        for (DfProcedureDiff procedureDiff : procedureDiffList) {
            if (procedureDiff.hasDiff()) {
                sb.append(_ln).append("  ").append(procedureDiff.getProcedureDispName());
                setupNextPreviousList(sb, procedureDiff.getNextPreviousDiffList(), "    ");
            }
        }
    }

    protected void buildProcedureDeleted(StringBuilder sb) {
        final List<DfProcedureDiff> diffList = _schemaDiff.getDeletedProcedureDiffList();
        setupSimpleDiffTable(sb, "Delete", "Procedure", diffList, diff -> diff.getProcedureDispName());
    }

    // ===================================================================================
    //                                                                     CraftTitle Diff
    //                                                                     ===============
    protected void handleCraftTitle(StringBuilder sb) {
        final List<DfCraftTitleDiff> craftTitleDiffList = _schemaDiff.getCraftTitleDiffList();
        for (DfCraftTitleDiff craftTitleDiff : craftTitleDiffList) {
            buildCraftRowAdded(sb, craftTitleDiff);
            buildCraftRowChanged(sb, craftTitleDiff);
            buildCraftRowDeleted(sb, craftTitleDiff);
        }
    }

    protected void buildCraftRowAdded(StringBuilder sb, DfCraftTitleDiff craftTitleDiff) {
        final String craftDispTitle = craftTitleDiff.getCraftDispTitle(); // e.g. MemberStatus
        final List<DfCraftRowDiff> craftRowDiffList = craftTitleDiff.getAddedCraftRowDiffList();
        setupSimpleDiffTable(sb, "Add", craftDispTitle, craftRowDiffList, diff -> diff.getCraftKeyDispName());
    }

    protected void buildCraftRowChanged(StringBuilder sb, DfCraftTitleDiff craftTitleDiff) {
        List<DfCraftRowDiff> craftRowDiffList = craftTitleDiff.getChangedCraftRowDiffList();
        if (craftRowDiffList.isEmpty()) {
            return;
        }
        final String title = filterTitle("Change " + craftTitleDiff.getCraftDispTitle());
        appendRootTitleLineSep(sb).append(title);
        for (DfCraftRowDiff craftRowDiff : craftRowDiffList) {
            final String craftKeyDispName = craftRowDiff.getCraftKeyDispName();
            final List<NextPreviousHandler> diffList = craftRowDiff.getNextPreviousDiffList();
            sb.append(_ln).append("  ").append(craftKeyDispName); // e.g. FML
            setupNextPreviousList(sb, diffList, "    "); // e.g. Value :: Formalized|1 -> Formalized|9
        }
    }

    protected void buildCraftRowDeleted(StringBuilder sb, DfCraftTitleDiff craftTitleDiff) {
        final String craftDispTitle = craftTitleDiff.getCraftDispTitle();
        final List<DfCraftRowDiff> craftRowDiffList = craftTitleDiff.getDeletedCraftRowDiffList();
        setupSimpleDiffTable(sb, "Delete", craftDispTitle, craftRowDiffList, diff -> diff.getCraftKeyDispName());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // -----------------------------------------------------
    //                                           Simple Diff
    //                                           -----------
    // means Add/Delete
    protected <DIFF> void setupSimpleDiffTable(StringBuilder sb, String diffType, String objectType, List<DIFF> diffList,
            Function<DIFF, String> nameProvider) {
        if (diffList.isEmpty()) {
            return;
        }
        final String title = filterTitle(diffType + " " + objectType);
        appendRootTitleLineSep(sb).append(title);
        for (DIFF diff : diffList) {
            sb.append(_ln).append("  ").append(nameProvider.apply(diff));
        }
    }

    // -----------------------------------------------------
    //                                         Next/Previous
    //                                         -------------
    protected void setupNextPreviousList(StringBuilder sb, List<NextPreviousHandler> nextPreviousDiffList, String indent) {
        for (NextPreviousHandler nextPreviousHandler : nextPreviousDiffList) {
            doSetupNextPrevious(sb, nextPreviousHandler, indent);
        }
    }

    protected void doSetupNextPrevious(StringBuilder sb, NextPreviousHandler nextPreviousHandler, String indent) {
        final DfNextPreviousDiff nextPreviousDiff = nextPreviousHandler.provide();
        if (nextPreviousDiff != null) {
            sb.append(_ln).append(indent).append(nextPreviousHandler.titleName());
            sb.append(" :: ").append(nextPreviousDiff.getDisplayPlain());
        }
    }

    // -----------------------------------------------------
    //                                                 Title
    //                                                 -----
    protected StringBuilder appendRootTitleLineSep(StringBuilder sb) {
        return sb.append(_ln).append(_ln);
    }

    protected String filterTitle(String title) {
        // may be adjusted at future (2026/07/07)
        /* e.g.
        [Change Table]
          MEMBER
            [Change Column]
              MEMBER_ACCOUNT
                Size :: 50 -> 80
         */
        return "[" + title + "]";
    }
}
