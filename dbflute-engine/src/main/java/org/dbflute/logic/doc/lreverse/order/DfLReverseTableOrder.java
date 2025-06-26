/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.lreverse.DfLReverseProcess;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseTableOrder {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final int _sectionTableGuidelineLimit; // not minus

    protected boolean _frameworkDebug;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseTableOrder(int sectionTableGuidelineLimit) {
        _sectionTableGuidelineLimit = sectionTableGuidelineLimit;
    }

    public DfLReverseTableOrder enableFrameworkDebug() {
        _frameworkDebug = true;
        return this;
    }

    // ===================================================================================
    //                                                                      Analyzer Order
    //                                                                      ==============
    public List<List<Table>> analyzeOrder(List<Table> tableList, List<Table> skippedTableList) {
        if (_frameworkDebug) {
            _log.debug("#lrev_tableOrder ...Analyzing order: tables={}, skipped={}", tableList.size(), skippedTableList.size());
        }
        final Set<String> alreadyRegisteredSet = new HashSet<String>();
        for (Table skippedTable : skippedTableList) {
            alreadyRegisteredSet.add(skippedTable.getName()); // pure name here
        }
        final List<List<Table>> orderedList = new ArrayList<List<Table>>();

        List<Table> unregisteredTableList;
        {
            final TreeSet<Table> allTableSet = new TreeSet<Table>(new Comparator<Table>() {
                public int compare(Table o1, Table o2) {
                    // e.g. order, order_detail, order_detail_more, ...
                    return o1.getTableDbName().compareTo(o2.getTableDbName());
                }
            });
            allTableSet.addAll(tableList);
            unregisteredTableList = new ArrayList<Table>(allTableSet);
        }
        int doCount = 0;
        int level = 1;
        while (true) {
            final int beforeSize = unregisteredTableList.size();
            if (_frameworkDebug) {
                _log.debug("#lrev_tableOrder ...Doing analyze: doCount={}, level={}, before={}", doCount, level, beforeSize);
            }
            unregisteredTableList = doAnalyzeOrder(unregisteredTableList, alreadyRegisteredSet, orderedList, level);
            ++doCount;
            if (unregisteredTableList.isEmpty()) {
                if (_frameworkDebug) {
                    _log.debug("#lrev_tableOrder Happy end: doCount={}, level={}", doCount, level);
                }
                break; // happy end
            }
            final int afterSize = unregisteredTableList.size();
            if (_frameworkDebug) {
                final String tableListDisp = toTableListDisp(unregisteredTableList);
                _log.debug("#lrev_tableOrder one analyzed: doCount={}, level={}, unreg={}", doCount, level, tableListDisp);
            }
            if (beforeSize == afterSize) { // means it cannot analyze more
                if (level == 1) { // level finished: next challenge, ignores additional foreign key
                    ++level;
                } else { // level 2 finished: however unregistered tables exist (cyclic?)
                    if (_frameworkDebug) {
                        final int sadlyCount = unregisteredTableList.size();
                        _log.debug("#lrev_tableOrder Sadly end: doCount={}, level={}, sadlyCount={}", doCount, level, sadlyCount);
                        for (Table sadlyTable : unregisteredTableList) {
                            _log.debug("#lrev_tableOrder sadlyTable: {}", sadlyTable.getTableDbName());
                        }
                    }
                    final List<Table> recoveredTableList = recoverSadly(unregisteredTableList);
                    orderedList.add(recoveredTableList);
                    break; // sadly end
                }
            }
        }
        if (_frameworkDebug) {
            _log.debug("#lrev_tableOrder before grouping: flatList={}", toTableListListDisp(orderedList));
        }
        return groupingSize(groupingCategory(orderedList));
    }

    // -----------------------------------------------------
    //                                            do Analyze
    //                                            ----------
    /**
     * @param tableList The list of table, which may be registered. (NotNull)
     * @param alreadyRegisteredSet The (pure) name set of already registered table. (NotNull)
     * @param outputOrderedList The ordered list of table for output. (NotNull)
     * @param level The level to determine dependencies. (NotMinus)
     * @return The list of unregistered table. (NotNull)
     */
    protected List<Table> doAnalyzeOrder(List<Table> tableList, Set<String> alreadyRegisteredSet, List<List<Table>> outputOrderedList,
            int level) {
        final List<Table> unregisteredTableList = new ArrayList<Table>();
        final List<Table> elementList = new ArrayList<Table>();
        for (Table table : tableList) {
            final List<ForeignKey> foreignKeyList = table.getForeignKeyList();
            boolean dependsOnAny = false;
            for (ForeignKey fk : foreignKeyList) {
                if (level >= 1 && fk.hasFixedCondition()) { // from first level, ignore fixed condition
                    continue;
                }
                if (level >= 2 && fk.isAdditionalForeignKey()) { // from second level, ignore additional FK
                    continue;
                }
                if (determineDependingOnUnregisteredOther(fk, alreadyRegisteredSet)) {
                    dependsOnAny = true; // found non-registered parent table so it still depends on any 
                    break;
                }
            }
            if (_frameworkDebug) {
                _log.debug("#lrev_tableOrder level={}, table={}, dependsOnAny={}", level, table.getTableDbName(), dependsOnAny);
            }
            if (dependsOnAny) {
                unregisteredTableList.add(table);
            } else {
                elementList.add(table);
                alreadyRegisteredSet.add(table.getName()); // pure name here
            }
        }
        if (!elementList.isEmpty()) {
            outputOrderedList.add(elementList);
        }
        return unregisteredTableList;
    }

    protected boolean determineDependingOnUnregisteredOther(ForeignKey fk, Set<String> alreadyRegisteredSet) {
        if (fk.isSelfReference()) {
            return false;
        }
        // FK to others here
        return !alreadyRegisteredSet.contains(fk.getForeignTablePureName()); // depends on unregistered yet
    }

    // -----------------------------------------------------
    //                                         Recover Sadly
    //                                         -------------
    protected List<Table> recoverSadly(List<Table> unregisteredTableList) {
        _log.debug("#lrev_tableOrder ...Recovering sadly: tables={}", unregisteredTableList.size());
        final Map<String, Table> remainedSadlyTableMap = new LinkedHashMap<>(); // should be mutable
        for (Table unregisteredTable : unregisteredTableList) {
            remainedSadlyTableMap.put(unregisteredTable.getName(), unregisteredTable); // pure name here
        }

        final List<Collection<Table>> recoveredTableListList = new ArrayList<>();
        int doCount = 0;
        while (true) {
            if (remainedSadlyTableMap.isEmpty()) { // recovery done
                if (_frameworkDebug) {
                    final long recovered = recoveredTableListList.stream().flatMap(ls -> ls.stream()).count();
                    _log.debug("#lrev_tableOrder happy recovery: doCount={}, recovered={}", doCount, recovered);
                }
                break; // happy end
            }
            final int beforeSize = remainedSadlyTableMap.size();

            if (_frameworkDebug) {
                _log.debug("#lrev_tableOrder ...Doing recover sadly: doCount={}, before={}", doCount, beforeSize);
            }
            List<Table> recoveredTableList = doRecoverSadly(remainedSadlyTableMap);

            recoveredTableListList.add(recoveredTableList);
            ++doCount;
            final int afterSize = remainedSadlyTableMap.size();
            if (_frameworkDebug) {
                _log.debug("#lrev_tableOrder one recovery: doCount={}, before={}, after={}", doCount, beforeSize, afterSize);
            }

            if (beforeSize == afterSize) { // no change
                if (_frameworkDebug) {
                    _log.debug("#lrev_tableOrder sadly recovery: doCount={}, remained={}", doCount, remainedSadlyTableMap.size());
                }
                recoveredTableListList.add(remainedSadlyTableMap.values()); // only cyclic?
                break; // sadly end
            }
        }

        // MYSTIC, ZA_REFERRER | XA_CYCLIC, YA_CYCLIC
        // XA_CYCLIC, YA_CYCLIC | MYSTIC, ZA_REFERRER
        Collections.reverse(recoveredTableListList);

        return recoveredTableListList.stream().flatMap(ls -> ls.stream()).collect(Collectors.toList());
    }

    protected List<Table> doRecoverSadly(Map<String, Table> remainedSadlyTableMap) {
        final List<Table> recoveredTableList = new ArrayList<>();
        final List<Table> sadlyTableList = new ArrayList<>(remainedSadlyTableMap.values());
        for (Table sadlyTable : sadlyTableList) {
            final String tableName = sadlyTable.getName(); // pure name here

            final List<ForeignKey> referrerList = sadlyTable.getReferrerList();
            boolean referredByOtherSadlyTable = false;
            for (ForeignKey referrer : referrerList) {
                final String referrerTableName = referrer.getTable().getName(); // pure name here
                if (tableName.equals(referrerTableName)) { // self reference
                    continue; // not related to order determination
                }
                // other table here
                final Table sadlyReferrerTable = remainedSadlyTableMap.get(referrerTableName);
                if (sadlyReferrerTable != null) { // referred by other sadly table here
                    referredByOtherSadlyTable = true;
                    break;
                }
            }
            if (!referredByOtherSadlyTable) { // maybe no referrer so move to the latter
                _log.debug("#lrev_tableOrder recovered: table={}, referrers={}", tableName, referrerList.size());
                recoveredTableList.add(sadlyTable);
            }
        }
        // lazy removing to order tables refering to the same group table in the group
        for (Table recoveredTable : recoveredTableList) {
            remainedSadlyTableMap.remove(recoveredTable.getName()); // pure name here
        }
        return recoveredTableList;
    }

    // ===================================================================================
    //                                                                            Grouping
    //                                                                            ========
    protected List<List<Table>> groupingCategory(List<List<Table>> outputOrderedList) {
        return doGroupingCategory(doGroupingCategory(outputOrderedList, false), true);
    }

    protected List<List<Table>> doGroupingCategory(List<List<Table>> outputOrderedList, boolean secondLevel) {
        final int sectionTableGuidelineLimit = getSectionTableGuidelineLimit();
        final List<List<Table>> groupedList = new ArrayList<List<Table>>();
        for (List<Table> tableList : outputOrderedList) {
            if (secondLevel && (!isFirstLevelGroup(tableList) || tableList.size() <= sectionTableGuidelineLimit)) {
                groupedList.add(new ArrayList<Table>(tableList));
                continue;
            }
            List<Table> workTableList = new ArrayList<Table>(); // as initial instance
            String currentPrefix = null;
            boolean inGroup = false;
            for (Table table : tableList) {
                final String tableName = table.getName();
                if (currentPrefix != null) {
                    if (tableName.startsWith(currentPrefix)) { // grouped
                        inGroup = true;
                        final int workSize = workTableList.size();
                        if (workSize >= 2) {
                            final Table requiredSizeBefore = workTableList.get(workSize - 2);
                            if (!requiredSizeBefore.getName().startsWith(currentPrefix)) {
                                // the work list has non-group elements at the front so split them
                                final Table groupBase = workTableList.remove(workSize - 1);
                                groupedList.add(workTableList);
                                workTableList = new ArrayList<Table>();
                                workTableList.add(groupBase);
                            }
                        }
                        workTableList.add(table);
                    } else {
                        if (inGroup) { // switched
                            groupedList.add(workTableList);
                            workTableList = new ArrayList<Table>();
                            inGroup = false;
                        }
                        currentPrefix = null;
                    }
                }
                if (currentPrefix == null) {
                    if (secondLevel) {
                        currentPrefix = extractSecondLevelPrefix(tableName);
                    } else {
                        currentPrefix = extractFirstLevelPrefix(tableName);
                    }
                    workTableList.add(table);
                }
            }
            if (!workTableList.isEmpty()) {
                groupedList.add(workTableList);
            }
        }
        assertAdjustmentBeforeAfter(outputOrderedList, groupedList);
        return groupedList;
    }

    protected List<List<Table>> groupingSize(List<List<Table>> outputOrderedList) {
        final int sectionTableGuidelineLimit = getSectionTableGuidelineLimit();
        final List<List<Table>> groupedList = new ArrayList<List<Table>>();
        for (List<Table> tableList : outputOrderedList) {
            final int tableSize = tableList.size();

            if (!groupedList.isEmpty() && tableSize < sectionTableGuidelineLimit) {
                // handle only-one table
                if (tableSize == 1) {
                    final Table onlyOneTable = tableList.get(0);
                    final List<ForeignKey> foreignKeyList = onlyOneTable.getForeignKeyList();
                    final Set<String> foreignTableSet = new HashSet<String>();
                    for (ForeignKey fk : foreignKeyList) {
                        if (!fk.hasFixedCondition()) { // ignore fixed condition
                            foreignTableSet.add(fk.getForeignTablePureName());
                        }
                    }
                    List<Table> candidatePreviousList = null;
                    for (int i = groupedList.size() - 1; i >= 0; --i) { // reverse loop
                        final List<Table> previousList = groupedList.get(i);
                        boolean existsFK = false;
                        for (Table previousTable : previousList) {
                            if (foreignTableSet.contains(previousTable.getName())) {
                                existsFK = true;
                            }
                        }
                        if (!isFirstLevelGroup(previousList) && previousList.size() < sectionTableGuidelineLimit) {
                            // not group and small
                            candidatePreviousList = previousList;
                        }
                        if (existsFK) {
                            break;
                        }
                    }
                    if (candidatePreviousList != null) {
                        candidatePreviousList.add(onlyOneTable);
                        continue;
                    }
                }
                // join small sections
                final List<Table> lastList = groupedList.get(groupedList.size() - 1);
                if (isSecondLevelGroup(lastList) && isSecondLevelGroup(tableList)) {

                }
                if (isJoinSmallSection(lastList, tableList)) {
                    lastList.addAll(tableList);
                    continue;
                }
            }
            groupedList.add(new ArrayList<Table>(tableList)); // needs new list to manipulate
        }
        assertAdjustmentBeforeAfter(outputOrderedList, groupedList);
        return groupedList;
    }

    protected boolean isJoinSmallSection(List<Table> lastList, List<Table> tableList) {
        boolean result = false;
        if (!isFirstLevelGroup(lastList) || !isFirstLevelGroup(tableList)) { // either not group
            result = true;
        } else if (isFirstLevelGroup(lastList) && isFirstLevelGroup(tableList)) {
            final String lastPrefix = extractFirstLevelPrefix(lastList.get(0).getName());
            final String mainPrefix = extractFirstLevelPrefix(tableList.get(0).getName());
            if (lastPrefix.equals(mainPrefix)) {
                result = true; // same category groups are joined
            } else if (lastList.size() <= 3 && tableList.size() <= 3) {
                result = true; // small groups are joined
            }
        }
        return result && (lastList.size() + tableList.size()) <= getSectionTableGuidelineLimit();
    }

    protected boolean isFirstLevelGroup(List<Table> tableList) {
        if (tableList.size() == 1) {
            return false;
        }
        final Set<String> prefixSet = new HashSet<String>();
        for (Table table : tableList) {
            final String prefix = extractFirstLevelPrefix(table.getName());
            prefixSet.add(prefix);
        }
        return prefixSet.size() == 1;
    }

    protected boolean isSecondLevelGroup(List<Table> tableList) {
        if (tableList.size() == 1) {
            return false;
        }
        final Set<String> prefixSet = new HashSet<String>();
        for (Table table : tableList) {
            final String prefix = extractSecondLevelPrefix(table.getName());
            if (prefix == null) {
                return false;
            }
            prefixSet.add(prefix);
        }
        return prefixSet.size() == 1;
    }

    protected void assertAdjustmentBeforeAfter(List<List<Table>> outputOrderedList, List<List<Table>> groupedList) {
        int resourceCount = 0;
        for (List<Table> tableList : outputOrderedList) {
            resourceCount = resourceCount + tableList.size();
        }
        int groupedCount = 0;
        for (List<Table> tableList : groupedList) {
            groupedCount = groupedCount + tableList.size();
        }
        if (resourceCount != groupedCount) {
            String msg = "The grouping process had the loss:";
            msg = msg + " resourceCount=" + resourceCount + " groupedCount=" + groupedCount;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                           Main Name
    //                                                                           =========
    public String extractMainName(List<Table> tableList) {
        final String miscName = "misc";
        if (tableList.size() < 2) {
            return miscName;
        }
        final String firstTableName = tableList.get(0).getName();
        if (isSecondLevelGroup(tableList)) {
            return extractSecondLevelPrefix(firstTableName).toUpperCase();
        }
        final String secondLevelPrefix = deriveMostName(tableList, true);
        if (secondLevelPrefix != null) {
            return secondLevelPrefix;
        }
        if (isFirstLevelGroup(tableList)) {
            return extractFirstLevelPrefix(firstTableName).toUpperCase();
        }
        final String firstLevelPrefix = deriveMostName(tableList, false);
        if (firstLevelPrefix != null) {
            return firstLevelPrefix;
        }
        return miscName;
    }

    protected String deriveMostName(List<Table> tableList, boolean secondLevel) {
        final String plusSuffix = "-plus";
        final Map<String, Integer> prefixMap = new HashMap<String, Integer>();
        for (Table table : tableList) {
            final String tableName = table.getName();
            final String prefix;
            if (secondLevel) {
                final String secondLevelPrefix = extractSecondLevelPrefix(tableName);
                if (secondLevelPrefix != null) {
                    prefix = secondLevelPrefix;
                } else {
                    prefix = extractFirstLevelPrefix(tableName);
                }
            } else {
                prefix = extractFirstLevelPrefix(tableName);
            }
            final Integer size = prefixMap.get(prefix);
            if (size != null) {
                prefixMap.put(prefix, size + 1);
            } else {
                prefixMap.put(prefix, 1);
            }
        }
        if (prefixMap.size() == 1) { // no way because of process before
            return prefixMap.keySet().iterator().next().toUpperCase() + plusSuffix;
        } else if (prefixMap.size() >= 2) {
            String mostPrefix = null;
            Integer mostSize = 0;
            for (Entry<String, Integer> entry : prefixMap.entrySet()) {
                final Integer count = entry.getValue();
                if (mostSize < count) {
                    mostPrefix = entry.getKey();
                    mostSize = count;
                }
            }
            if (secondLevel && mostPrefix.contains("_")) {
                final String firstLevelPrefix = extractFirstLevelPrefix(mostPrefix);
                final Integer firstLevelSize = prefixMap.get(firstLevelPrefix);
                if (firstLevelSize != null && firstLevelSize > 0) {
                    return firstLevelPrefix.toUpperCase() + (isFirstLevelGroup(tableList) ? "" : plusSuffix);
                }
            }
            if (mostSize > 1 && mostSize > (tableList.size() / 2)) {
                return mostPrefix.toUpperCase() + plusSuffix;
            }
        }
        return null;
    }

    // ===================================================================================
    //                                                                    Prefix Extractor
    //                                                                    ================
    protected String extractFirstLevelPrefix(String tableName) { // not null
        return Srl.substringFirstFront(tableName, "_");
    }

    protected String extractSecondLevelPrefix(String tableName) { // null allowed
        if (!tableName.contains("_")) {
            return null;
        }
        final String firstPrefix = Srl.substringFirstFront(tableName, "_");
        final String firstRear = Srl.substringFirstRear(tableName, "_");
        return firstPrefix + "_" + Srl.substringFirstFront(firstRear, "_");
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String toTableListDisp(List<Table> tableList) {
        return tableList.stream().map(table -> table.getTableDbName()).collect(Collectors.toList()).toString();
    }

    protected String toTableListListDisp(List<List<Table>> tableListList) {
        return tableListList.stream()
                .flatMap(ls -> ls.stream())
                .map(table -> table.getTableDbName())
                .collect(Collectors.toList())
                .toString();
    }

    // ===================================================================================
    //                                                                        Section Size
    //                                                                        ============
    protected int getSectionTableGuidelineLimit() {
        return _sectionTableGuidelineLimit;
    }
}
