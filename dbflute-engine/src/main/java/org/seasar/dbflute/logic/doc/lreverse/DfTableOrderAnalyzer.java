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
package org.seasar.dbflute.logic.doc.lreverse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/23 Saturday)
 */
public class DfTableOrderAnalyzer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final int STANDARD_SIZE = 9;

    // ===================================================================================
    //                                                                      Analyzer Order
    //                                                                      ==============
    public List<List<Table>> analyzeOrder(List<Table> tableList, List<Table> skippedTableList) {
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
        int level = 1;
        while (true) {
            final int beforeSize = unregisteredTableList.size();
            unregisteredTableList = doAnalyzeOrder(unregisteredTableList, alreadyRegisteredSet, orderedList, level);
            if (unregisteredTableList.isEmpty()) {
                break; // happy end
            }
            final int afterSize = unregisteredTableList.size();
            if (beforeSize == afterSize) { // means it cannot analyze more
                if (level == 1) { // level finished: next challenge, ignores additional foreign key
                    ++level;
                } else { // level 2 finished: however unregistered tables exist 
                    orderedList.add(unregisteredTableList);
                    break; // sadly end
                }
            }
        }
        return groupingSize(groupingCategory(orderedList));
    }

    /**
     * @param tableList The list of table, which may be registered. (NotNull)
     * @param alreadyRegisteredSet The (pure) name set of already registered table. (NotNull)
     * @param outputOrderedList The ordered list of table for output. (NotNull)
     * @return The list of unregistered table. (NotNull)
     */
    protected List<Table> doAnalyzeOrder(List<Table> tableList, Set<String> alreadyRegisteredSet,
            List<List<Table>> outputOrderedList, final int level) {
        final List<Table> unregisteredTableList = new ArrayList<Table>();
        final List<Table> elementList = new ArrayList<Table>();
        for (Table table : tableList) {
            final List<ForeignKey> foreignKeyList = table.getForeignKeyList();
            boolean dependsOnAny = false;
            for (ForeignKey fk : foreignKeyList) {
                final String foreignTablePureName = fk.getForeignTablePureName();
                if (level >= 1 && fk.hasFixedCondition()) { // from first level, ignore fixed condition
                    continue;
                }
                if (level >= 2 && fk.isAdditionalForeignKey()) { // from second level, ignore additional FK
                    continue;
                }
                if (!fk.isSelfReference() && !alreadyRegisteredSet.contains(foreignTablePureName)) {
                    dependsOnAny = true; // found non-registered parent table so it still depends on any 
                    break;
                }
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

    protected List<List<Table>> groupingCategory(List<List<Table>> outputOrderedList) {
        return doGroupingCategory(doGroupingCategory(outputOrderedList, false), true);
    }

    protected List<List<Table>> doGroupingCategory(List<List<Table>> outputOrderedList, boolean secondLevel) {
        final int standardSize = STANDARD_SIZE;
        final List<List<Table>> groupedList = new ArrayList<List<Table>>();
        for (List<Table> tableList : outputOrderedList) {
            if (secondLevel && (!isFirstLevelGroup(tableList) || tableList.size() <= standardSize)) {
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
        final int standardSize = STANDARD_SIZE;
        final List<List<Table>> groupedList = new ArrayList<List<Table>>();
        for (List<Table> tableList : outputOrderedList) {
            final int tableSize = tableList.size();

            if (!groupedList.isEmpty() && tableSize < standardSize) {
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
                        if (!isFirstLevelGroup(previousList) && previousList.size() < standardSize) {
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
        return result && (lastList.size() + tableList.size()) <= STANDARD_SIZE;
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
}
