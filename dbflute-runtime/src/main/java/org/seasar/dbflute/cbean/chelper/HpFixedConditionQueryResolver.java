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
package org.seasar.dbflute.cbean.chelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.cbean.ConditionQuery;
import org.seasar.dbflute.cbean.sqlclause.join.FixedConditionResolver;
import org.seasar.dbflute.cbean.sqlclause.subquery.SubQueryIndentProcessor;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.name.TableSqlName;
import org.seasar.dbflute.exception.DBMetaNotFoundException;
import org.seasar.dbflute.exception.FixedConditionIllegalOverRelationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.IndexOfInfo;

/**
 * @author jflute
 * @since 0.9.7.5 (2010/10/11 Monday)
 */
public class HpFixedConditionQueryResolver implements FixedConditionResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LOCAL_ALIAS_MARK = "$$localAlias$$";
    public static final String FOREIGN_ALIAS_MARK = "$$foreignAlias$$";
    public static final String SQ_BEGIN_MARK = "$$sqbegin$$";
    public static final String SQ_END_MARK = "$$sqend$$";
    public static final String INLINE_MARK = "$$inline$$";
    public static final String LOCATION_BASE_MARK = "$$locationBase$$";
    public static final String OPTIMIZED_MARK = "$$optimized$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ConditionQuery _localCQ;
    protected final ConditionQuery _foreignCQ;
    protected final DBMetaProvider _dbmetaProvider;

    // analyzing result in variable resolution (internal bridge variables for next step)
    protected String _resolvedFixedCondition;
    protected Map<String, InlineViewResource> _inlineViewResourceMap;
    protected String _inlineViewOptimizedCondition;
    protected boolean _inlineViewOptimizationWholeCondition;
    protected Set<Integer> _inlineViewOptimizedLineNumberSet;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HpFixedConditionQueryResolver(ConditionQuery localCQ, ConditionQuery foreignCQ, DBMetaProvider dbmetaProvider) {
        _localCQ = localCQ;
        _foreignCQ = foreignCQ;
        _dbmetaProvider = dbmetaProvider;
    }

    // ===================================================================================
    //                                                                    Resolve Variable
    //                                                                    ================
    /**
     * {@inheritDoc}
     */
    public String resolveVariable(String fixedCondition, boolean fixedInline) {
        // should be called before optimization
        // because analyzing process has saving the fixed condition
        // so it needs to filter it before saved
        fixedCondition = filterLocationMark(fixedCondition, fixedInline);

        analyzeInlineViewOptimization(fixedCondition, fixedInline);
        fixedCondition = filterBasicMark(fixedCondition, fixedInline);
        fixedCondition = filterSubQueryIndentMark(fixedCondition, fixedInline, false);
        fixedCondition = resolveOverRelation(fixedCondition, fixedInline);

        final String resolvedFixedCondition;
        final String delimiter = getInlineMark();
        if (fixedCondition.contains(delimiter)) { // mark optimization
            final String inlineCondition = Srl.substringFirstFront(fixedCondition, delimiter);
            final String filtered = Srl.rtrim(inlineCondition);
            if (filtered.trim().length() > 0) {
                resolvedFixedCondition = filtered;
            } else { // mark exists at first line (same as whole actually)
                resolvedFixedCondition = OPTIMIZED_MARK;
            }
        } else {
            if (_inlineViewOptimizationWholeCondition) { // whole optimization
                resolvedFixedCondition = OPTIMIZED_MARK; // as dummy
            } else { // part optimization or no optimized
                resolvedFixedCondition = fixedCondition;
            }
        }
        _resolvedFixedCondition = resolvedFixedCondition;
        return _resolvedFixedCondition;
    }

    // ===================================================================================
    //                                                                Analyze Optimization
    //                                                                ====================
    protected void analyzeInlineViewOptimization(String fixedCondition, boolean fixedInline) {
        if (fixedInline) {
            return;
        }
        if (!hasFixedInlineView(fixedCondition)) {
            return;
        }
        if (doAnalyzeInlineViewOptimizationMark(fixedCondition)) {
            return;
        }
        if (doAnalyzeInlineViewOptimizationWhole(fixedCondition)) {
            return;
        }
        if (doAnalyzeInlineViewOptimizationPart(fixedCondition)) {
            return;
        }
    }

    protected boolean hasFixedInlineView(String fixedCondition) {
        final String relationBeginMark = getRelationBeginMark();
        final String foreignTableMark = getForeignTableMark();
        final String foreignOverMark = relationBeginMark + foreignTableMark;
        return fixedCondition.contains(foreignOverMark);
    }

    protected boolean doAnalyzeInlineViewOptimizationMark(String fixedCondition) {
        final String delimiter = getInlineMark();
        if (fixedCondition.contains(delimiter)) {
            final String inlineCondition = Srl.substringFirstRear(fixedCondition, delimiter);
            final String filtered = removePrefixConnector(inlineCondition);
            // null means no in-line, while it means suppressing optimization
            _inlineViewOptimizedCondition = filtered.trim().length() > 0 ? filtered : null;
            return true;
        }
        return false;
    }

    protected boolean doAnalyzeInlineViewOptimizationWhole(String fixedCondition) {
        _inlineViewOptimizationWholeCondition = canBeInlineViewOptimization(fixedCondition);
        if (_inlineViewOptimizationWholeCondition) {
            _inlineViewOptimizedCondition = fixedCondition;
            return true;
        }
        return false;
    }

    protected boolean doAnalyzeInlineViewOptimizationPart(String fixedCondition) {
        final String ifCommentMark = "/*IF";
        if (fixedCondition.contains(ifCommentMark)) {
            return false; // optimization part is unsupported when IF comment exists 
        }
        final List<String> lineList = Srl.splitList(fixedCondition, ln());
        String previous = null; // optimization candidate value
        final List<String> optLineList = new ArrayList<String>();
        final List<Integer> optLineNumberList = new ArrayList<Integer>(lineList.size());
        int lineNumber = 1;
        for (String current : lineList) {
            if (isCandidateOfOptimization(previous) && isOptimizationHitLine(previous, current, lineNumber)) {
                // and ... (previous) or ...
                // and ... (current)
                optLineList.add(previous); // previous line can be optimized
                optLineNumberList.add(lineNumber - 1);
            }
            previous = current;
            ++lineNumber;
        }
        if (isCandidateOfOptimization(previous)) {
            if (startsWithConnector(previous)) {
                // ...
                // and ... (previous)
                optLineList.add(previous);
                optLineNumberList.add(lineNumber - 1);
            }
        }
        if (!optLineNumberList.isEmpty()) {
            _inlineViewOptimizedLineNumberSet = new HashSet<Integer>(optLineNumberList);
        }
        if (!optLineList.isEmpty()) {
            final StringBuilder optSb = new StringBuilder();
            for (String line : optLineList) {
                if (optSb.length() > 0) {
                    optSb.append(ln());
                }
                optSb.append(line);
            }
            _inlineViewOptimizedCondition = removePrefixConnector(optSb.toString());
            return true;
        }
        return false;
    }

    protected boolean isCandidateOfOptimization(String previous) {
        return previous != null && canBeInlineViewOptimization(previous) && !hasUnclosedBrace(previous);
    }

    protected boolean isOptimizationHitLine(String previous, String current, int lineNumber) {
        if (lineNumber == 2 || (lineNumber >= 3 && startsWithConnector(previous))) { // first line or 'and'
            return startsWithConnector(current); // current (means next) line should start with 'and'
        }
        return false;
    }

    protected boolean canBeInlineViewOptimization(String fixedCondition) {
        final String relationBeginMark = getRelationBeginMark();
        final String foreignTableMark = getForeignTableMark();
        final String foreignOverMark = relationBeginMark + foreignTableMark;
        final String foreignAliasMark = getForeignAliasMark();
        if (!Srl.containsAny(fixedCondition, foreignOverMark, foreignAliasMark)) {
            return false;
        }
        final String removedCondition = replaceString(fixedCondition, foreignOverMark, "");
        if (removedCondition.contains(relationBeginMark)) { // other over relation exists
            return false;
        }
        final String localAliasMark = getLocalAliasMark();
        if (removedCondition.contains(localAliasMark)) { // local element exists
            return false;
        }
        // has (foreign over relation or foreign alias) and no local elements
        return true;
    }

    // ===================================================================================
    //                                                            Filter Mark and Variable
    //                                                            ========================
    protected String filterBasicMark(String fixedCondition, boolean fixedInline) {
        final String localAliasName = _localCQ.xgetAliasName();
        final String foreignAliasName = _foreignCQ.xgetAliasName();
        fixedCondition = replaceString(fixedCondition, "$$alias$$", foreignAliasName); // for compatibility
        fixedCondition = replaceString(fixedCondition, getLocalAliasMark(), localAliasName);
        if (!_inlineViewOptimizationWholeCondition) {
            fixedCondition = replaceString(fixedCondition, getForeignAliasMark(), foreignAliasName);
        }
        return fixedCondition;
    }

    protected String filterSubQueryIndentMark(String fixedCondition, boolean fixedInline, boolean optimized) {
        final String sqBeginMark = getSqBeginMark();
        final String sqEndMark = getSqEndMark();
        if (!fixedCondition.contains(sqBeginMark) || !fixedCondition.contains(sqEndMark)) {
            return fixedCondition;
        }
        final String sqEndIndent = calculateSqEndIndent(fixedInline, optimized);
        final String indentFrom = "\n)" + sqEndMark;
        final String indentTo = "\n" + sqEndIndent + ")" + sqEndMark;
        fixedCondition = Srl.replace(fixedCondition, indentFrom, indentTo);
        final SubQueryIndentProcessor processor = new SubQueryIndentProcessor();
        final String foreignAliasName = _foreignCQ.xgetAliasName();
        final String subQueryIdentity = "fixed_" + foreignAliasName;
        final String beginMark = processor.resolveSubQueryBeginMark(subQueryIdentity);
        fixedCondition = Srl.replace(fixedCondition, sqBeginMark, beginMark);
        final String endMark = processor.resolveSubQueryEndMark(subQueryIdentity);
        fixedCondition = Srl.replace(fixedCondition, sqEndMark, endMark);
        return fixedCondition;
    }

    protected String calculateSqEndIndent(boolean fixedInline, boolean optimized) {
        final String indent;
        if (fixedInline) {
            // ------"select ..."
            // ------"  from ..."
            // ------"    left outer join (select ..."
            // ------"                      where ..."
            indent = "                            ";
            // *inner-join gives up
        } else if (optimized) {
            // ------"(select ..."
            // ------"   from ..."
            // ------"     left outer join ..."
            // ------"       on ..."
            // ------"  where ..."
            indent = "        ";
        } else { // normal
            // ------"select ..."
            // ------"  from ..."
            // ------"    left outer join ..."
            // ------"      on ..."
            indent = "         ";
        }
        return indent;
    }

    protected String filterLocationMark(String fixedCondition, boolean fixedInline) {
        final String locationBase = _localCQ.xgetLocationBase();
        return replaceString(fixedCondition, getLocationBaseMark() + ".", "pmb." + locationBase);
    }

    // ===================================================================================
    //                                                               Resolve Over Relation
    //                                                               =====================
    protected String resolveOverRelation(String fixedCondition, boolean fixedInline) {
        // analyze:
        // - "$$over($localTable.memberSecurity)$$.REMINDER_QUESTION"
        // - "$$over($foreignTable.memberStatus, DISPLAY_ORDER)$$.ORDER_NO"
        // - "$$over(PURCHASE.product.productStatus)$$.PRODUCT_STATUS_NAME"
        final String relationBeginMark = getRelationBeginMark();
        final String relationEndMark = getRelationEndMark();
        String resolvedClause = fixedCondition;
        String remainder = resolvedClause;
        while (true) {
            // "|$$over(|$localTable.memberSecurity)$$.REMINDER_QUESTION"
            final IndexOfInfo relationBeginIndex = Srl.indexOfFirst(remainder, relationBeginMark);
            if (relationBeginIndex == null) {
                break;
            }
            remainder = relationBeginIndex.substringRear();
            // "$localTable.memberSecurity|)$$|.REMINDER_QUESTION"
            final IndexOfInfo relationEndIndex = Srl.indexOfFirst(remainder, relationEndMark);
            if (relationEndIndex == null) {
                break;
            }
            // remainder is e.g. "$localTable.memberSecurity)$$" now

            // e.g. "$localTable.memberSecurity" or "$foreignTable.memberStatus, DISPLAY_ORDER"
            final String relationExp = relationEndIndex.substringFront();
            // e.g. "$$over($localTable.memberSecurity)$$" or "$$over($foreignTable.memberStatus, DISPLAY_ORDER)$$"
            final String relationVariable = relationBeginMark + relationExp + relationEndMark;
            final String pointTable; // e.g. "$localTable" or "$foreignTable" or "PURCHASE"
            final String targetRelation; // e.g. "memberSecurity" or "product.productStatus" or null (means base only)
            final String secondArg; // e.g. DISPLAY_ORDER or null (means no argument)
            {
                final IndexOfInfo separatorIndex = Srl.indexOfFirst(relationExp, ".");
                if (separatorIndex != null) { // normally here
                    pointTable = separatorIndex.substringFrontTrimmed(); // e.g. $localTable
                    final String separatorRear = separatorIndex.substringRearTrimmed();
                    final IndexOfInfo argIndex = Srl.indexOfFirst(separatorRear, ",");
                    targetRelation = argIndex != null ? argIndex.substringFrontTrimmed() : separatorRear;
                    secondArg = argIndex != null ? argIndex.substringRearTrimmed() : null;
                } else { // e.g. "$$over(PURCHASE)$$"
                    final IndexOfInfo argIndex = Srl.indexOfFirst(relationExp, ",");
                    pointTable = argIndex != null ? argIndex.substringFrontTrimmed() : Srl.trim(relationExp);
                    targetRelation = null;
                    secondArg = argIndex != null ? argIndex.substringRearTrimmed() : null;
                }
            }

            final ConditionQuery relationPointCQ;
            final ConditionQuery columnTargetCQ;
            if (Srl.equalsPlain(pointTable, getLocalTableMark())) { // local table
                relationPointCQ = _localCQ;
                if (targetRelation != null) {
                    columnTargetCQ = invokeColumnTargetCQ(relationPointCQ, targetRelation);
                } else {
                    String notice = "The relation on fixed condition is required if the table is not referrer.";
                    throwIllegalFixedConditionOverRelationException(notice, pointTable, null, fixedCondition);
                    return null; // unreachable
                }
            } else if (Srl.equalsPlain(pointTable, getForeignTableMark())) { // foreign table
                relationPointCQ = _foreignCQ;
                columnTargetCQ = relationPointCQ;
                if (targetRelation == null) {
                    String notice = "The relation on fixed condition is required if the table is not referrer.";
                    throwIllegalFixedConditionOverRelationException(notice, pointTable, null, fixedCondition);
                    return null; // unreachable
                }
                // prepare fixed InlineView
                if (_inlineViewResourceMap == null) {
                    _inlineViewResourceMap = new LinkedHashMap<String, InlineViewResource>();
                }
                final InlineViewResource resource;
                if (_inlineViewResourceMap.containsKey(targetRelation)) {
                    resource = _inlineViewResourceMap.get(targetRelation);
                } else {
                    resource = new InlineViewResource();
                    _inlineViewResourceMap.put(targetRelation, resource);
                }
                final String columnName;
                {
                    // e.g. "$$over($localTable.memberSecurity)$$|.|REMINDER_QUESTION = ..."
                    final IndexOfInfo rearIndex = Srl.indexOfFirst(relationEndIndex.substringRearTrimmed(), ".");
                    if (rearIndex == null || rearIndex.getIndex() > 0) {
                        String notice = "The OverRelation variable should continue to column after the variable.";
                        throwIllegalFixedConditionOverRelationException(notice, pointTable, targetRelation,
                                fixedCondition);
                        return null; // unreachable
                    }
                    final String columnStart = rearIndex.substringRear(); // e.g. REMINDER_QUESTION = ...
                    final IndexOfInfo indexInfo = Srl.indexOfFirst(columnStart, " ", ",", ")", "\n", "\t");
                    columnName = indexInfo != null ? indexInfo.substringFront() : columnStart; // REMINDER_QUESTION
                }
                // the secondArg should be a column DB name, and then rear column is alias name
                final String resolvedColumn = secondArg != null ? secondArg + " as " + columnName : columnName;
                resource.addAdditionalColumn(resolvedColumn); // selected in in-line view

                if (!resource.hasJoinInfo()) { // first analyze
                    final List<String> splitList = Srl.splitList(targetRelation, ".");
                    DBMeta currentDBMeta = _dbmetaProvider.provideDBMeta(_foreignCQ.getTableDbName());
                    for (String element : splitList) {
                        final ForeignInfo foreignInfo = currentDBMeta.findForeignInfo(element);
                        resource.addJoinInfo(foreignInfo);
                        currentDBMeta = foreignInfo.getForeignDBMeta();
                    }
                }
                final List<ForeignInfo> joinInfoList = resource.getJoinInfoList();
                if (!joinInfoList.isEmpty()) { // basically true (but just in case)
                    final ForeignInfo latestForeignInfo = joinInfoList.get(joinInfoList.size() - 1);
                    resource.addOptimizedVariable(relationVariable, latestForeignInfo);
                }
            } else { // referrer table
                final DBMeta pointDBMeta;
                try {
                    pointDBMeta = _dbmetaProvider.provideDBMeta(pointTable);
                } catch (DBMetaNotFoundException e) {
                    String notice = "The table for relation on fixed condition does not exist.";
                    throwIllegalFixedConditionOverRelationException(notice, pointTable, targetRelation, fixedCondition,
                            e);
                    return null; // unreachable
                }
                ConditionQuery referrerQuery = _localCQ.xgetReferrerQuery();
                while (true) {
                    if (referrerQuery == null) { // means not found
                        break;
                    }
                    if (Srl.equalsPlain(pointDBMeta.getTableDbName(), referrerQuery.getTableDbName())) {
                        break;
                    }
                    referrerQuery = referrerQuery.xgetReferrerQuery();
                }
                relationPointCQ = referrerQuery;
                if (relationPointCQ == null) {
                    String notice = "The table for relation on fixed condition was not found in the scope.";
                    throwIllegalFixedConditionOverRelationException(notice, pointTable, targetRelation, fixedCondition);
                    return null; // unreachable
                }
                if (targetRelation != null) {
                    columnTargetCQ = invokeColumnTargetCQ(relationPointCQ, targetRelation);
                } else {
                    columnTargetCQ = relationPointCQ;
                }
            }

            // resolve over-relation variables in clause
            final String relationAlias = columnTargetCQ.xgetAliasName(); // e.g. "dfrel_4"
            resolvedClause = replaceString(resolvedClause, relationVariable, relationAlias);

            // after case for loop
            remainder = relationEndIndex.substringRear();

            // no replace even if same relation because of additional column
            //// to prevent from processing same one
            //remainder = replaceString(remainder, relationVariable, relationAlias);
        }
        resolvedClause = adjustOptimizedLine(resolvedClause);
        return resolvedClause;
    }

    protected ConditionQuery invokeColumnTargetCQ(ConditionQuery relationPointCQ, String targetRelation) {
        return relationPointCQ.invokeForeignCQ(targetRelation);
    }

    protected String adjustOptimizedLine(String resolvedClause) {
        if (_inlineViewOptimizedLineNumberSet == null) {
            return resolvedClause;
        }
        final List<String> lineList = Srl.splitList(resolvedClause, ln());
        final List<String> filteredList = new ArrayList<String>();
        int lineNumber = 1;
        for (String line : lineList) {
            if (!_inlineViewOptimizedLineNumberSet.contains(lineNumber)) {
                filteredList.add(line);
            }
            ++lineNumber;
        }
        final StringBuilder filteredSb = new StringBuilder();
        for (String line : filteredList) {
            if (filteredSb.length() > 0) {
                filteredSb.append(ln());
            }
            filteredSb.append(line);
        }
        return removePrefixConnector(filteredSb.toString());
    }

    // ===================================================================================
    //                                                            Resolve Fixed InlineView
    //                                                            ========================
    public String resolveFixedInlineView(String foreignTableSqlName, boolean treatedAsInnerJoin) {
        // it is precondition that the fixed condition has already been resolved here
        // so it can uses bridge variables here
        if (_inlineViewResourceMap == null || _inlineViewResourceMap.isEmpty()) {
            return foreignTableSqlName; // not uses InlineView
        }
        // alias is required because foreignTableSqlName may be (normal) InlineView
        final String baseAlias = "dffixedbase";
        final String baseIndent;
        if (treatedAsInnerJoin) {
            // ----------"    inner join "
            baseIndent = "               ";
        } else {
            // ----------"    left outer join "
            baseIndent = "                    ";
        }
        final StringBuilder joinSb = new StringBuilder();
        final Map<ForeignInfo, String> relationMap = new HashMap<ForeignInfo, String>();
        final List<String> additionalRealColumnList = new ArrayList<String>();
        final String resolvedFixedCondition = _resolvedFixedCondition; // basically not null
        String optimizedCondition = _inlineViewOptimizedCondition;
        int groupIndex = 0;
        for (InlineViewResource resource : _inlineViewResourceMap.values()) {
            final List<ForeignInfo> joinInfoList = resource.getJoinInfoList();
            final String aliasBase = "dffixedjoin";
            String preForeignAlias = null;
            String foreignAlias = null;
            int joinIndex = 0;
            final Map<ForeignInfo, String> foreignAliasMap = new HashMap<ForeignInfo, String>(joinInfoList.size());
            for (ForeignInfo joinInfo : joinInfoList) {
                if (relationMap.containsKey(joinInfo)) { // already joined
                    preForeignAlias = relationMap.get(joinInfo); // update previous alias
                    continue;
                }
                final TableSqlName foreignTable;
                final String localAlias;
                {
                    final DBMeta foreignDBMeta = joinInfo.getForeignDBMeta();
                    foreignTable = foreignDBMeta.getTableSqlName();
                    localAlias = (preForeignAlias != null ? preForeignAlias : baseAlias);
                    foreignAlias = aliasBase + "_" + groupIndex + "_" + joinIndex;
                    preForeignAlias = foreignAlias;
                }
                joinSb.append(ln()).append(baseIndent);
                joinSb.append("     left outer join ").append(foreignTable).append(" ").append(foreignAlias);
                joinSb.append(" on ");
                final Map<ColumnInfo, ColumnInfo> columnInfoMap = joinInfo.getLocalForeignColumnInfoMap();
                int columnIndex = 0;
                for (Entry<ColumnInfo, ColumnInfo> localForeignEntry : columnInfoMap.entrySet()) {
                    final ColumnInfo localColumnInfo = localForeignEntry.getKey();
                    final ColumnInfo foreignColumninfo = localForeignEntry.getValue();
                    if (columnIndex > 0) {
                        joinSb.append(" and ");
                    }
                    joinSb.append(localAlias).append(".").append(localColumnInfo.getColumnSqlName());
                    joinSb.append(" = ").append(foreignAlias).append(".").append(foreignColumninfo.getColumnSqlName());
                    ++columnIndex;
                }
                foreignAliasMap.put(joinInfo, foreignAlias);
                relationMap.put(joinInfo, foreignAlias);
                ++joinIndex;
            }
            if (optimizedCondition != null) {
                optimizedCondition = resolvedOptimizedCondition(optimizedCondition, resource, foreignAliasMap);
            }
            collectAdditionalRealColumnList(additionalRealColumnList, resolvedFixedCondition, resource, foreignAlias);
            ++groupIndex;
        }
        if (optimizedCondition != null) { // foreign alias for in-line view is resolved here
            optimizedCondition = replaceString(optimizedCondition, getForeignAliasMark(), baseAlias);
            optimizedCondition = filterSubQueryIndentMark(optimizedCondition, false, true);
        }
        final StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("(select ").append(baseAlias).append(".*");
        for (String columnName : additionalRealColumnList) {
            sqlSb.append(", ").append(columnName);
        }
        sqlSb.append(ln()).append(baseIndent);
        sqlSb.append("   from ").append(foreignTableSqlName).append(" ").append(baseAlias);
        sqlSb.append(joinSb);
        if (optimizedCondition != null) {
            buildOptimizedInlineWhereClause(optimizedCondition, baseIndent, sqlSb);
        }
        sqlSb.append(ln()).append(baseIndent);
        sqlSb.append(")");
        return sqlSb.toString();
    }

    protected String resolvedOptimizedCondition(String optimizedCondition, InlineViewResource resource,
            Map<ForeignInfo, String> foreignAliasMap) {
        final Set<String> additionalColumnSet = resource.getAdditionalColumnSet();
        if (additionalColumnSet == null) { // basically no way here (but just in case)
            return optimizedCondition;
        }
        Map<String, String> optimizedReverseColumnMap = null;
        for (String columnName : additionalColumnSet) {
            final String delimiter = " as ";
            if (columnName.contains(delimiter)) {
                if (optimizedReverseColumnMap == null) {
                    optimizedReverseColumnMap = new HashMap<String, String>(additionalColumnSet.size());
                }
                // e.g. MEMBER_STATUS_NAME as STATUS
                final String physicalName = Srl.substringLastFront(columnName, delimiter);
                final String logicalName = Srl.substringLastRear(columnName, delimiter);
                optimizedReverseColumnMap.put(logicalName, physicalName); // e.g. STATUS : MEMBER_STATUS_NAME
            }
        }
        optimizedCondition = resolveOptimizedForeignAlias(optimizedCondition, resource, foreignAliasMap);
        optimizedCondition = reverseOptimizedColumnAlias(optimizedCondition, optimizedReverseColumnMap);
        return optimizedCondition;
    }

    protected String resolveOptimizedForeignAlias(String optimizedCondition, InlineViewResource resource,
            Map<ForeignInfo, String> foreignAliasMap) {
        if (optimizedCondition == null) {
            return null;
        }
        final Map<String, ForeignInfo> optimizedVariableMap = resource.getOptimizedVariableMap();
        if (optimizedVariableMap == null) {
            return optimizedCondition;
        }
        for (Entry<String, ForeignInfo> entry : optimizedVariableMap.entrySet()) {
            final String relationVariable = entry.getKey();
            final ForeignInfo foreignInfo = entry.getValue();
            final String inlineAlias = foreignAliasMap.get(foreignInfo);
            optimizedCondition = replaceString(optimizedCondition, relationVariable, inlineAlias);
        }
        return optimizedCondition;
    }

    protected String reverseOptimizedColumnAlias(String optimizedCondition,
            Map<String, String> optimizedReverseColumnMap) {
        if (optimizedReverseColumnMap != null) {
            for (Entry<String, String> entry : optimizedReverseColumnMap.entrySet()) {
                final String logicalName = entry.getKey();
                final String physicalName = entry.getValue();
                optimizedCondition = replaceString(optimizedCondition, "." + logicalName, "." + physicalName);
            }
        }
        return optimizedCondition;
    }

    protected void collectAdditionalRealColumnList(List<String> additionalRealColumnList,
            String resolvedFixedCondition, InlineViewResource resource, String foreignAlias) {
        final Set<String> additionalColumnSet = resource.getAdditionalColumnSet();
        if (additionalColumnSet == null) { // basically no way here (but just in case)
            return;
        }
        for (String columnName : additionalColumnSet) {
            final String delimiter = " as ";
            final String columnMark;
            if (columnName.contains(delimiter)) {
                final String logicalName = Srl.substringLastRear(columnName, delimiter);
                columnMark = "." + logicalName;
            } else {
                columnMark = "." + columnName;
            }
            if (resolvedFixedCondition != null && resolvedFixedCondition.contains(columnMark)) {
                additionalRealColumnList.add(foreignAlias + "." + columnName);
            }
        }
    }

    protected void buildOptimizedInlineWhereClause(String optimizedCondition, String baseIndent, StringBuilder sqlSb) {
        sqlSb.append(ln()).append(baseIndent);
        sqlSb.append("  where ");

        // sub-query marks are already replaced here so it uses indent processor
        final String sqBeginMark = SubQueryIndentProcessor.BEGIN_MARK_PREFIX;
        final String sqEndMark = SubQueryIndentProcessor.END_MARK_PREFIX;

        final List<String> splitList = Srl.splitList(optimizedCondition, ln());
        boolean subQueryIndentScope = false;
        int index = 0;
        for (String line : splitList) {
            if (line.contains(sqEndMark)) {
                subQueryIndentScope = false;
            }
            if (index == 0) {
                sqlSb.append(line);
            } else {
                sqlSb.append(ln());
                if (!subQueryIndentScope) { // no sub-query: sub-query has own formatting
                    sqlSb.append(baseIndent);
                }
                final String trimmedLine = line.trim();
                if (trimmedLine.startsWith("and ")) {
                    sqlSb.append("    ").append(trimmedLine);
                } else {
                    sqlSb.append(line);
                }
            }
            if (line.contains(sqBeginMark)) {
                subQueryIndentScope = true;
            }
            ++index;
        }
    }

    // ===================================================================================
    //                                                                    InlineView Class
    //                                                                    ================
    protected static class InlineViewResource {
        protected Set<String> _additionalColumnSet;
        protected List<ForeignInfo> _joinInfoList;
        protected Map<String, ForeignInfo> _optimizedVariableMap;

        public Set<String> getAdditionalColumnSet() {
            return _additionalColumnSet;
        }

        public void addAdditionalColumn(String additionalColumn) {
            if (_additionalColumnSet == null) {
                _additionalColumnSet = new LinkedHashSet<String>();
            }
            _additionalColumnSet.add(additionalColumn);
        }

        public boolean hasJoinInfo() {
            return _joinInfoList != null && !_joinInfoList.isEmpty();
        }

        public List<ForeignInfo> getJoinInfoList() {
            return _joinInfoList;
        }

        public void addJoinInfo(ForeignInfo joinInfo) {
            if (_joinInfoList == null) {
                _joinInfoList = new ArrayList<ForeignInfo>();
            }
            _joinInfoList.add(joinInfo);
        }

        public Map<String, ForeignInfo> getOptimizedVariableMap() {
            return _optimizedVariableMap;
        }

        public void addOptimizedVariable(String relationVariable, ForeignInfo foreignInfo) {
            if (_optimizedVariableMap == null) {
                _optimizedVariableMap = new HashMap<String, ForeignInfo>();
            }
            _optimizedVariableMap.put(relationVariable, foreignInfo);
        }
    }

    // ===================================================================================
    //                                                                  Exception Handling
    //                                                                  ==================
    protected void throwIllegalFixedConditionOverRelationException(String notice, String tableName,
            String relationName, String fixedCondition) {
        throwIllegalFixedConditionOverRelationException(notice, tableName, relationName, fixedCondition, null);
    }

    protected void throwIllegalFixedConditionOverRelationException(String notice, String pointTable,
            String targetRelation, String fixedCondition, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Point Table");
        br.addElement(pointTable);
        br.addItem("Target Relation");
        br.addElement(targetRelation);
        br.addItem("Fixed Condition");
        br.addElement(fixedCondition);
        br.addItem("BizOneToOne's Local");
        br.addElement(_localCQ.getTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new FixedConditionIllegalOverRelationException(msg, e);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public boolean hasOverRelation(String fixedCondition) {
        final String relationBeginMark = getRelationBeginMark();
        final String relationEndMark = getRelationEndMark();
        return Srl.containsAll(fixedCondition, relationBeginMark, relationEndMark);
    }

    // ===================================================================================
    //                                                                       Variable Mark
    //                                                                       =============
    protected String getLocalAliasMark() {
        return LOCAL_ALIAS_MARK;
    }

    protected String getForeignAliasMark() {
        return FOREIGN_ALIAS_MARK;
    }

    protected String getSqBeginMark() {
        return SQ_BEGIN_MARK;
    }

    protected String getSqEndMark() {
        return SQ_END_MARK;
    }

    protected String getInlineMark() {
        return INLINE_MARK;
    }

    protected String getLocationBaseMark() {
        return LOCATION_BASE_MARK;
    }

    protected String getRelationBeginMark() {
        return "$$over(";
    }

    protected String getRelationEndMark() {
        return ")$$";
    }

    protected String getLocalTableMark() {
        return "$localTable";
    }

    protected String getForeignTableMark() {
        return "$foreignTable";
    }

    // ===================================================================================
    //                                                                       Clause Helper
    //                                                                       =============
    protected boolean isOneLine(String fixedCondition) {
        return !fixedCondition.contains(ln());
    }

    protected boolean hasUnclosedBrace(String line) {
        return line.contains("(") && !line.contains(")");
    }

    protected boolean startsWithConnector(String line) {
        return line.trim().startsWith("and ");
    }

    protected String removePrefixConnector(String clause) {
        return Srl.ltrim(Srl.ltrim(Srl.ltrim(clause), "and "));
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected final String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
