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
package org.seasar.dbflute.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.torque.engine.database.model.Column;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 */
public final class DfIncludeQueryProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ALL_MARK = "$$ALL$$";
    public static final String COMMON_COLUMN_MARK = "$$CommonColumn$$";
    public static final String VERSION_NO_MARK = "$$VersionNo$$";
    public static final String TYPE_MARK = "type:";
    protected static final String PROP_STRING = "String";
    protected static final String PROP_NUMBER = "Number";
    protected static final String PROP_DATE = "Date";
    protected static final String PROP_ORDER_BY = "OrderBy";
    protected static final String PROP_RELATION = "Relation";
    protected static final String PROP_MYSELF = "Myself"; // old style

    protected static final Set<String> _stringCKeySet = new LinkedHashSet<String>();
    static {
        _stringCKeySet.add("NotEqual");
        _stringCKeySet.add("GreaterThan");
        _stringCKeySet.add("GreaterEqual");
        _stringCKeySet.add("LessThan");
        _stringCKeySet.add("LessEqual");
        _stringCKeySet.add("InScope");
        _stringCKeySet.add("NotInScope");
        _stringCKeySet.add("PrefixSearch");
        _stringCKeySet.add("LikeSearch");
        _stringCKeySet.add("NotLikeSearch");
        _stringCKeySet.add("EmptyString");
        _stringCKeySet.add("IsNull");
        _stringCKeySet.add("IsNullOrEmpty");
        _stringCKeySet.add("IsNotNull");
    }

    protected static final Set<String> _numberCKeySet = new LinkedHashSet<String>();
    static {
        _numberCKeySet.add("NotEqual");
        _numberCKeySet.add("GreaterThan");
        _numberCKeySet.add("GreaterEqual");
        _numberCKeySet.add("LessThan");
        _numberCKeySet.add("LessEqual");
        _numberCKeySet.add("RangeOf");
        _numberCKeySet.add("InScope");
        _numberCKeySet.add("NotInScope");
        _numberCKeySet.add("IsNull");
        _numberCKeySet.add("IsNullOrEmpty");
        _numberCKeySet.add("IsNotNull");
    }

    protected static final Set<String> _dateCKeySet = new LinkedHashSet<String>();
    static {
        _dateCKeySet.add("NotEqual");
        _dateCKeySet.add("GreaterThan");
        _dateCKeySet.add("GreaterEqual");
        _dateCKeySet.add("LessThan");
        _dateCKeySet.add("LessEqual");
        _dateCKeySet.add("FromTo");
        _dateCKeySet.add("DateFromTo");
        _dateCKeySet.add("InScope");
        _dateCKeySet.add("NotInScope");
        _dateCKeySet.add("IsNull");
        _dateCKeySet.add("IsNullOrEmpty");
        _dateCKeySet.add("IsNotNull");
    }

    protected static final Set<String> _orderByCKeySet = new LinkedHashSet<String>();
    static {
        _orderByCKeySet.add("Asc");
        _orderByCKeySet.add("Desc");
    }

    protected static final Map<String, Set<String>> _ckeySetMap = new LinkedHashMap<String, Set<String>>();
    static {
        _ckeySetMap.put("String", _stringCKeySet);
        _ckeySetMap.put("Number", _numberCKeySet);
        _ckeySetMap.put("Date", _dateCKeySet);
        _ckeySetMap.put("OrderBy", _orderByCKeySet);
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfIncludeQueryProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                   Include Query Map
    //                                                                   =================
    // map:{
    //     ; String = map:{
    //         # [Include]
    //         # String columns may not be needed
    //         # to be set these condition-keys basically.
    //         # *set UPDATE_USER for the test of geared cipher.
    //         ; GreaterThan = map:{ MEMBER = list:{UPDATE_USER} }
    //         ; LessThan = map:{ MEMBER = list:{UPDATE_USER} }
    //         ; GreaterEqual = map:{}
    //         ; LessEqual = map:{}
    //
    //         # [Exclude]
    //         # Common columns of String type may not be needed
    //         # to be set these condition-keys basically.
    //         ; !NotEqual = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !GreaterThan = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !LessThan = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !GreaterEqual = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !LessEqual = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !InScope = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !NotInScope = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !PrefixSearch = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         # for the test of geared cipher
    //         #; !LikeSearch = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !NotLikeSearch = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //     }
    //     ; Number = map:{
    //         # [Include]
    //         ; NotEqual = map:{}
    //
    //         # [Exclude]
    //         # VersionNo column may not be needed
    //         # to be set these condition-keys basically.
    //         ; !GreaterThan = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //         ; !LessThan = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //         ; !GreaterEqual = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //         ; !LessEqual = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //         ; !InScope = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //         ; !NotInScope = map:{ $$ALL$$ = list:{ $$VersionNo$$ } }
    //     }
    //     ; Date = map:{
    //         # [Include]
    //         # Date columns may not be needed
    //         # to be set these condition-keys basically.
    //         ; NotEqual = map:{}
    //         ; InScope = map:{}
    //         ; NotInScope = map:{}
    //
    //         # [Exclude]
    //         # Common columns of Date type may not be needed
    //         # to be set these condition-keys basically.
    //         ; !GreaterThan = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !LessThan = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !GreaterEqual = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         # for the test of column hints
    //         ; !LessEqual = map:{ $$ALL$$ = list:{ prefix:REGISTER_ ; prefix:UPDATE_ } }
    //         ; !FromTo = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //         ; !DateFromTo = map:{ $$ALL$$ = list:{ $$CommonColumn$$ } }
    //     }
    //     ; OrderBy = map:{
    //         ; !Asc = map:{ $$ALL$$ = list:{ DESCRIPTION ; type:CLOB } }
    //         ; !Desc = map:{ $$ALL$$ = list:{ suffix:_ORDER ; DESCRIPTION } }
    //         ; %Desc = map:{ MEMBER_STATUS = list:{ DISPLAY_ORDER } }
    //     }
    //     ; Myself = map:{
    //         ; !ScalarCondition = map:{ suffix:_STATUS = list:{} }
    //         ; !MyselfDerived = map:{ prefix:PRODUCT_ST = list:{} ; suffix:_STATUS = list:{ dummy } }
    //         ; !MyselfExists = map:{ suffix:_STATUS = list:{} ; SERVICE_RANK = list:{} }
    //         ; !MyselfInScope = map:{ suffix:_STATUS = list:{} ; SERVICE_RANK = list:{ SERVICE_RANK_CODE } }
    //     }
    //     ; $MEMBER = map:{
    //         ; BIRTHDATE(Date) = list:{ !GreaterThan ; !LessThan }
    //         ; MEMBER_ACCOUNT(String) = list:{}
    //     }
    // }
    protected Map<String, Map<String, Map<String, List<String>>>> _includeQueryMap;
    protected final Map<String, Map<String, Map<String, List<String>>>> _excludeQueryMap = newLinkedHashMap();
    protected final Map<String, Map<String, Map<String, List<String>>>> _excludeReviveQueryMap = newLinkedHashMap();

    public Map<String, Map<String, Map<String, List<String>>>> getIncludeQueryMap() {
        if (_includeQueryMap != null) {
            return _includeQueryMap;
        }
        try {
            _includeQueryMap = doGetIncludeQueryMap();
        } catch (RuntimeException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to parse includeQueryMap.dfprop.");
            br.addItem("Advice");
            br.addElement("Make sure your map!");
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg, e);
        }
        return _includeQueryMap;
    }

    protected Map<String, Map<String, Map<String, List<String>>>> doGetIncludeQueryMap() {
        final Map<String, Map<String, Map<String, List<String>>>> resultMap = newLinkedHashMap();
        Map<String, Object> targetMap = mapProp("torque.includeQueryMap", DEFAULT_EMPTY_MAP);
        targetMap = adjustColumnDrivenMergedDummyPropIfNeeds(targetMap);
        final Map<String, Map<String, Map<String, List<String>>>> columnDrivenTranslatedMap = extractColumnDrivenTranslatedMap(targetMap);
        for (Entry<String, Object> propEntry : targetMap.entrySet()) {
            final String propType = propEntry.getKey();
            final Object value = propEntry.getValue();
            if (!(value instanceof Map)) {
                String msg = "The key 'includeQueryMap' should have map value:";
                msg = msg + " key=" + propType + " value=" + value + " targetMap=" + targetMap;
                throw new DfIllegalPropertyTypeException(msg);
            }
            if (propType.startsWith("$")) {
                continue; // except column-driven
            }
            @SuppressWarnings("unchecked")
            final Map<String, Map<String, List<String>>> ckeyColumnMap = (Map<String, Map<String, List<String>>>) value;
            mergeColumnDriven(columnDrivenTranslatedMap, propType, ckeyColumnMap);
            resultMap.put(propType, prepareElementMap(propType, ckeyColumnMap));
        }
        return resultMap;
    }

    protected Map<String, Object> adjustColumnDrivenMergedDummyPropIfNeeds(Map<String, Object> targetMap) {
        final Map<String, Object> adjustedMap = newLinkedHashMap();
        adjustedMap.putAll(targetMap);
        for (String prop : _ckeySetMap.keySet()) {
            final Object element = adjustedMap.get(prop);
            if (element == null) {
                // you can specify e.g. OrderBy as column-driven without normal settings
                adjustedMap.put(prop, new LinkedHashMap<String, Object>());
            }
        }
        return adjustedMap;
    }

    protected Map<String, Map<String, List<String>>> prepareElementMap(String propType,
            Map<String, Map<String, List<String>>> queryMap) {
        final Map<String, Map<String, List<String>>> elementMap = newLinkedHashMap();
        for (Entry<String, Map<String, List<String>>> entry : queryMap.entrySet()) {
            final String ckey = entry.getKey();
            final Map<String, List<String>> tableColumnMap = entry.getValue();
            if (ckey.startsWith("!")) { // means exclude
                final String filteredKey = ckey.substring("!".length());
                reflectExcludeQuery(propType, filteredKey, tableColumnMap);
            } else if (ckey.startsWith("%")) { // means exclude-revive
                final String filteredKey = ckey.substring("%".length());
                reflectExcludeReviveQuery(propType, filteredKey, tableColumnMap);
            } else { // means independent include
                elementMap.put(ckey, tableColumnMap);
            }
        }
        return elementMap;
    }

    public Map<String, Map<String, Map<String, List<String>>>> getExcludeQueryMap() {
        getIncludeQueryMap(); // initialize
        return _excludeQueryMap;
    }

    public Map<String, Map<String, Map<String, List<String>>>> getExcludeReviveQueryMap() {
        getIncludeQueryMap(); // initialize
        return _excludeReviveQueryMap;
    }

    protected void reflectExcludeQuery(String javaType, String queryType, Map<String, List<String>> tableColumnMap) {
        Map<String, Map<String, List<String>>> elementMap = _excludeQueryMap.get(javaType);
        if (elementMap == null) {
            elementMap = newLinkedHashMap();
            _excludeQueryMap.put(javaType, elementMap);
        }
        elementMap.put(queryType, tableColumnMap);
    }

    protected void reflectExcludeReviveQuery(String javaType, String queryType, Map<String, List<String>> tableColumnMap) {
        Map<String, Map<String, List<String>>> elementMap = _excludeReviveQueryMap.get(javaType);
        if (elementMap == null) {
            elementMap = newLinkedHashMap();
            _excludeReviveQueryMap.put(javaType, elementMap);
        }
        elementMap.put(queryType, tableColumnMap);
    }

    // ===================================================================================
    //                                                                       Column Driven
    //                                                                       =============
    protected void mergeColumnDriven(Map<String, Map<String, Map<String, List<String>>>> columnDrivenTargetMap,
            String propType, Map<String, Map<String, List<String>>> ckeyColumnMap) {
        final Map<String, Map<String, List<String>>> columnDrivenQueryMap = columnDrivenTargetMap.get(propType);
        if (columnDrivenQueryMap == null || columnDrivenQueryMap.isEmpty()) {
            return;
        }
        for (Entry<String, Map<String, List<String>>> entry : columnDrivenQueryMap.entrySet()) {
            final String ckey = entry.getKey();
            final Map<String, List<String>> fromCKeyMap = entry.getValue();
            Map<String, List<String>> toCKeyMap = ckeyColumnMap.get(ckey);
            if (toCKeyMap == null) {
                toCKeyMap = newLinkedHashMap();
                ckeyColumnMap.put(ckey, toCKeyMap);
            }
            for (Entry<String, List<String>> ckeyEntry : fromCKeyMap.entrySet()) {
                final String tableName = ckeyEntry.getKey();
                final List<String> fromColumnList = ckeyEntry.getValue();
                final StringKeyMap<List<String>> flexibleMap = StringKeyMap.createAsCaseInsensitive();
                flexibleMap.putAll(toCKeyMap);
                List<String> toColumnList = flexibleMap.get(tableName);
                if (toColumnList == null) {
                    toColumnList = new ArrayList<String>();
                    toCKeyMap.put(tableName, toColumnList);
                }
                for (String fromColumn : fromColumnList) {
                    if (!Srl.containsElementIgnoreCase(toColumnList, fromColumn)) {
                        toColumnList.add(fromColumn);
                    }
                }
            }
        }
    }

    protected Map<String, Map<String, Map<String, List<String>>>> extractColumnDrivenTranslatedMap(
            Map<String, Object> plainMap) {
        final Map<String, Map<String, List<String>>> interfaceMap = extractColumnDrivenInterfaceMap(plainMap);
        final Map<String, Map<String, Map<String, List<String>>>> translatedMap = newLinkedHashMap();
        for (Entry<String, Map<String, List<String>>> tableEntry : interfaceMap.entrySet()) {
            final String tableName = tableEntry.getKey();
            final Map<String, List<String>> columnTypeCKeyMap = tableEntry.getValue();
            for (Entry<String, List<String>> columnTypeCKeyEntry : columnTypeCKeyMap.entrySet()) {
                final String columnExp = columnTypeCKeyEntry.getKey();
                final String columnName = Srl.substringFirstFront(columnExp, "(").trim();
                final ScopeInfo scopeFirst = Srl.extractScopeFirst(columnExp, "(", ")");
                if (scopeFirst == null) {
                    String msg = "The column expression should be e.g. Member(Date) but: " + columnExp;
                    throw new DfIllegalPropertySettingException(msg);
                }
                final String propType = scopeFirst.getContent().trim(); // e.g. String, OrderBy
                final List<String> ckeyList = columnTypeCKeyEntry.getValue();
                Map<String, Map<String, List<String>>> ckeyColumnMap = translatedMap.get(propType);
                if (ckeyColumnMap == null) {
                    ckeyColumnMap = newLinkedHashMap();
                    translatedMap.put(propType, ckeyColumnMap);
                }
                for (String ckey : ckeyList) {
                    Map<String, List<String>> tableColumnMap = ckeyColumnMap.get(ckey);
                    if (tableColumnMap == null) {
                        tableColumnMap = newLinkedHashMap();
                        ckeyColumnMap.put(ckey, tableColumnMap);
                    }
                    List<String> columnList = tableColumnMap.get(tableName);
                    if (columnList == null) {
                        columnList = new ArrayList<String>();
                        tableColumnMap.put(tableName, columnList);
                    }
                    columnList.add(columnName);
                }
            }
        }
        return translatedMap;
    }

    protected Map<String, Map<String, List<String>>> extractColumnDrivenInterfaceMap(Map<String, Object> targetMap) {
        // ; $MEMBER = map:{
        //     ; BIRTHDATE(Date) = list:{ !GreaterThan ; !LessThan }
        //     ; MEMBER_NAME(String) = list:{}
        // }
        final Map<String, Map<String, List<String>>> interfaceMap = newLinkedHashMap();
        for (Entry<String, Object> propEntry : targetMap.entrySet()) {
            final String propType = propEntry.getKey();
            final Object value = propEntry.getValue();
            if (!(value instanceof Map)) {
                String msg = "The key 'includeQueryMap' should have map value:";
                msg = msg + " key=" + propType + " value=" + value + " targetMap=" + targetMap;
                throw new DfIllegalPropertyTypeException(msg);
            }
            if (!propType.startsWith("$")) {
                continue;
            }
            final String tableName = Srl.substringFirstRear(propType, "$");
            @SuppressWarnings("unchecked")
            final Map<String, List<String>> columnCKeyMap = (Map<String, List<String>>) value;
            Set<Entry<String, List<String>>> entrySet = columnCKeyMap.entrySet();
            for (Entry<String, List<String>> entry : entrySet) {
                final String columnExp = entry.getKey();
                final List<String> ckeyList = entry.getValue();
                if (!ckeyList.isEmpty()) {
                    columnCKeyMap.put(columnExp, ckeyList);
                } else {
                    final ScopeInfo scopeFirst = Srl.extractScopeFirst(columnExp, "(", ")");
                    if (scopeFirst == null) {
                        String msg = "The column expression should be e.g. Member(Date) but: " + columnExp;
                        throw new DfIllegalPropertySettingException(msg);
                    }
                    final String currentPropType = scopeFirst.getContent().trim();
                    final Set<String> ckeySet = _ckeySetMap.get(currentPropType);
                    if (ckeySet == null) {
                        String msg = "Unknown condition-key: " + currentPropType + ", expected=" + _ckeySetMap.keySet();
                        throw new DfIllegalPropertySettingException(msg);
                    }
                    final List<String> allCKeyList = new ArrayList<String>();
                    for (String ckey : ckeySet) {
                        allCKeyList.add("!" + ckey);
                    }
                    columnCKeyMap.put(columnExp, allCKeyList);
                }
            }
            interfaceMap.put(tableName, columnCKeyMap);
        }
        return interfaceMap;
    }

    // ===================================================================================
    //                                                                           Available
    //                                                                           =========
    // -----------------------------------------------------
    //                                                String
    //                                                ------
    public boolean isAvailableStringNotEqual(Column column) {
        return isAvailable(PROP_STRING, "NotEqual", column);
    }

    public boolean isAvailableStringGreaterThan(Column column) {
        return isAvailable(PROP_STRING, "GreaterThan", column);
    }

    public boolean isAvailableStringGreaterEqual(Column column) {
        return isAvailable(PROP_STRING, "GreaterEqual", column);
    }

    public boolean isAvailableStringLessThan(Column column) {
        return isAvailable(PROP_STRING, "LessThan", column);
    }

    public boolean isAvailableStringLessEqual(Column column) {
        return isAvailable(PROP_STRING, "LessEqual", column);
    }

    public boolean isAvailableStringInScope(Column column) {
        return isAvailable(PROP_STRING, "InScope", column);
    }

    public boolean isAvailableStringNotInScope(Column column) {
        return isAvailable(PROP_STRING, "NotInScope", column);
    }

    public boolean isAvailableStringPrefixSearch(Column column) {
        return isAvailable(PROP_STRING, "PrefixSearch", column);
    }

    public boolean isAvailableStringLikeSearch(Column column) {
        return isAvailable(PROP_STRING, "LikeSearch", column);
    }

    public boolean isAvailableStringNotLikeSearch(Column column) {
        return isAvailable(PROP_STRING, "NotLikeSearch", column);
    }

    public boolean isAvailableStringEmptyString(Column column) {
        return isAvailable(PROP_STRING, "EmptyString", column);
    }

    public boolean isAvailableStringIsNull(Column column) {
        return isAvailable(PROP_STRING, "IsNull", column);
    }

    public boolean isAvailableStringIsNullOrEmpty(Column column) {
        return isAvailable(PROP_STRING, "IsNullOrEmpty", column);
    }

    public boolean isAvailableStringIsNotNull(Column column) {
        return isAvailable(PROP_STRING, "IsNotNull", column);
    }

    // -----------------------------------------------------
    //                                                Number
    //                                                ------
    public boolean isAvailableNumberNotEqual(Column column) {
        return isAvailable(PROP_NUMBER, "NotEqual", column);
    }

    public boolean isAvailableNumberGreaterThan(Column column) {
        return isAvailable(PROP_NUMBER, "GreaterThan", column);
    }

    public boolean isAvailableNumberGreaterEqual(Column column) {
        return isAvailable(PROP_NUMBER, "GreaterEqual", column);
    }

    public boolean isAvailableNumberLessThan(Column column) {
        return isAvailable(PROP_NUMBER, "LessThan", column);
    }

    public boolean isAvailableNumberLessEqual(Column column) {
        return isAvailable(PROP_NUMBER, "LessEqual", column);
    }

    public boolean isAvailableNumberRangeOf(Column column) {
        return isAvailable(PROP_NUMBER, "RangeOf", column);
    }

    public boolean isAvailableNumberInScope(Column column) {
        return isAvailable(PROP_NUMBER, "InScope", column);
    }

    public boolean isAvailableNumberNotInScope(Column column) {
        return isAvailable(PROP_NUMBER, "NotInScope", column);
    }

    public boolean isAvailableNumberIsNull(Column column) {
        return isAvailable(PROP_NUMBER, "IsNull", column);
    }

    public boolean isAvailableNumberIsNullOrEmpty(Column column) {
        return isAvailable(PROP_NUMBER, "IsNullOrEmpty", column);
    }

    public boolean isAvailableNumberIsNotNull(Column column) {
        return isAvailable(PROP_NUMBER, "IsNotNull", column);
    }

    // -----------------------------------------------------
    //                                                  Date
    //                                                  ----
    public boolean isAvailableDateNotEqual(Column column) {
        return isAvailable(PROP_DATE, "NotEqual", column);
    }

    public boolean isAvailableDateGreaterThan(Column column) {
        return isAvailable(PROP_DATE, "GreaterThan", column);
    }

    public boolean isAvailableDateGreaterEqual(Column column) {
        return isAvailable(PROP_DATE, "GreaterEqual", column);
    }

    public boolean isAvailableDateLessThan(Column column) {
        return isAvailable(PROP_DATE, "LessThan", column);
    }

    public boolean isAvailableDateLessEqual(Column column) {
        return isAvailable(PROP_DATE, "LessEqual", column);
    }

    public boolean isAvailableDateFromTo(Column column) {
        return isAvailable(PROP_DATE, "FromTo", column); // means FromTo of Date type
    }

    public boolean isAvailableDateDateFromTo(Column column) {
        return isAvailable(PROP_DATE, "DateFromTo", column); // means DateFromTo of Date type
    }

    public boolean isAvailableDateInScope(Column column) {
        return isAvailable(PROP_DATE, "InScope", column);
    }

    public boolean isAvailableDateNotInScope(Column column) {
        return isAvailable(PROP_DATE, "NotInScope", column);
    }

    public boolean isAvailableDateIsNull(Column column) {
        return isAvailable(PROP_DATE, "IsNull", column);
    }

    public boolean isAvailableDateIsNullOrEmpty(Column column) {
        return isAvailable(PROP_DATE, "IsNullOrEmpty", column);
    }

    public boolean isAvailableDateIsNotNull(Column column) {
        return isAvailable(PROP_DATE, "IsNotNull", column);
    }

    // -----------------------------------------------------
    //                                               OrderBy
    //                                               -------
    public boolean isAvailableOrderByAsc(Column column) {
        return isAvailable(PROP_ORDER_BY, "Asc", column);
    }

    public boolean isAvailableOrderByDesc(Column column) {
        return isAvailable(PROP_ORDER_BY, "Desc", column);
    }

    // -----------------------------------------------------
    //                                              Relation
    //                                              --------
    public boolean isAvailableRelationExistsReferrer(Column column) {
        return isAvailable(PROP_RELATION, "ExistsReferrer", column);
    }

    public boolean isAvailableRelationInScopeRelation(Column column) {
        return isAvailable(PROP_RELATION, "InScopeRelation", column);
    }

    public boolean isAvailableRelationDerivedReferrer(Column column) {
        return isAvailable(PROP_RELATION, "DerivedReferrer", column);
    }

    public boolean isAvailableRelationSpecifiedDerivedOrderBy(Column column) {
        return isAvailable(PROP_RELATION, "SpecifiedDerivedOrderBy", column);
    }

    // -----------------------------------------------------
    //                                                Myself
    //                                                ------
    public boolean isAvailableMyselfInlineView(Column column) {
        return isAvailable(PROP_MYSELF, "InlineView", column); // contains OnClause
    }

    public boolean isAvailableMyselfScalarCondition(Column column) {
        return isAvailable(PROP_MYSELF, "ScalarCondition", column);
    }

    public boolean isAvailableMyselfMyselfDerived(Column column) {
        return isAvailable(PROP_MYSELF, "MyselfDerived", column);
    }

    public boolean isAvailableMyselfMyselfExists(Column column) {
        return isAvailable(PROP_MYSELF, "MyselfExists", column);
    }

    public boolean isAvailableMyselfMyselfInScope(Column column) {
        return isAvailable(PROP_MYSELF, "MyselfInScope", column);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isAvailable(String propType, String ckey, Column column) {
        if (hasQueryTypeIncludeQueryMap(propType, ckey)) {
            return containsTableColumnIncludeQueryMap(propType, ckey, column);
        }
        if (hasQueryTypeExcludeQueryMap(propType, ckey)) {
            final boolean excluded = containsTableColumnExcludeQueryMap(propType, ckey, column);
            if (excluded) {
                if (hasQueryTypeExcludeReviveQueryMap(propType, ckey)) {
                    final boolean revived = containsTableColumnExcludeReviveQueryMap(propType, ckey, column);
                    if (revived) {
                        return true; // excluded but revived
                    }
                }
                return false; // excluded
            } else {
                return true; // not excluded
            }
        }
        return true; // as default
    }

    protected boolean hasQueryTypeIncludeQueryMap(String propType, String ckey) {
        final Map<String, Map<String, List<String>>> map = getIncludeQueryMap().get(propType);
        return map != null && map.get(ckey) != null;
    }

    protected boolean hasQueryTypeExcludeQueryMap(String propType, String ckey) {
        final Map<String, Map<String, List<String>>> map = getExcludeQueryMap().get(propType);
        return map != null && map.get(ckey) != null;
    }

    protected boolean hasQueryTypeExcludeReviveQueryMap(String propType, String ckey) {
        final Map<String, Map<String, List<String>>> map = getExcludeReviveQueryMap().get(propType);
        return map != null && map.get(ckey) != null;
    }

    protected boolean containsTableColumnIncludeQueryMap(String propType, String ckey, Column column) {
        return doContainsTableColumnQueryMap(propType, ckey, column, getIncludeQueryMap());
    }

    protected boolean containsTableColumnExcludeQueryMap(String propType, String ckey, Column column) {
        return doContainsTableColumnQueryMap(propType, ckey, column, getExcludeQueryMap());
    }

    protected boolean containsTableColumnExcludeReviveQueryMap(String propType, String ckey, Column column) {
        return doContainsTableColumnQueryMap(propType, ckey, column, getExcludeReviveQueryMap());
    }

    protected boolean doContainsTableColumnQueryMap(String propType, String ckey, Column column,
            Map<String, Map<String, Map<String, List<String>>>> queryMap) {
        assertQueryMap(propType, ckey, queryMap);
        final String tableDbName = column.getTable().getTableDbName();
        final Set<String> columnSet = gatherColumnSet(propType, ckey, queryMap, tableDbName);
        if (isTableOnlyProp(propType)) { // e.g. ExistsReferrer, ScalarCondition
            return columnSet != null; // only null check here, empty column list means specified
        } else {
            if (columnSet == null || columnSet.isEmpty()) {
                return false;
            }
        }
        // either has a list element
        if (columnSet.contains(COMMON_COLUMN_MARK) && column.isCommonColumn()) {
            return true;
        }
        if (columnSet.contains(VERSION_NO_MARK) && column.isVersionNo()) {
            return true;
        }
        final String columnName = column.getName();
        final String typeMark = TYPE_MARK;
        for (String columnExp : columnSet) {
            if (Srl.startsWithIgnoreCase(columnExp, typeMark)) { // e.g. type:LONGVARCHAR
                final String specifiedType = Srl.substringFirstRear(columnExp, typeMark).trim();
                final String jdbcType = column.getJdbcType();
                if (jdbcType != null && jdbcType.equalsIgnoreCase(specifiedType)) {
                    return true;
                }
            }
            if (isHitByTheHint(columnName, columnExp)) {
                return true;
            }
        }
        return false;
    }

    protected void assertQueryMap(String propType, String ckey,
            Map<String, Map<String, Map<String, List<String>>>> queryMap) {
        if (queryMap.get(propType) == null) {
            String msg = "The propType[" + propType + "] should have the value of queryMap: " + queryMap;
            throw new IllegalStateException(msg);
        }
        if (queryMap.get(propType).get(ckey) == null) {
            String msg = "The conditionKey[" + ckey + "] should have the value of queryMap: " + queryMap;
            throw new IllegalStateException(msg);
        }
    }

    protected Set<String> gatherColumnSet(String propType, String ckey,
            Map<String, Map<String, Map<String, List<String>>>> queryMap, final String tableDbName) {
        final Map<String, List<String>> tableColumnMap = queryMap.get(propType).get(ckey);
        Set<String> columnSet = null;
        for (Entry<String, List<String>> entry : tableColumnMap.entrySet()) {
            final String tableHint = entry.getKey();
            if (ALL_MARK.equalsIgnoreCase(tableHint) || isHitByTheHint(tableDbName, tableHint)) {
                if (columnSet == null) {
                    columnSet = new HashSet<String>();
                }
                columnSet.addAll(entry.getValue());
            }
        }
        return columnSet;
    }

    protected boolean isTableOnlyProp(String propType) {
        return PROP_RELATION.equalsIgnoreCase(propType) || PROP_MYSELF.equalsIgnoreCase(propType);
    }
}