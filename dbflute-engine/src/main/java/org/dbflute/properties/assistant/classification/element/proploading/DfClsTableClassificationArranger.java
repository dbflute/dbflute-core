/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.properties.assistant.classification.element.proploading;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.coins.DfClassificationJavaNameFilter;
import org.dbflute.properties.assistant.classification.coins.DfClassificationJdbcCloser;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/10 Saturday at ikspiari)
 */
public class DfClsTableClassificationArranger {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfClsTableClassificationArranger.class);

    protected static final String SQL_MARK = "$sql:";
    protected static final Map<String, String> _nameFromToMap;
    static {
        // move preparing process to filter class to be pull-request-able
        Map<String, String> workingMap = new LinkedHashMap<>();
        new DfClassificationJavaNameFilter().prepareNameFromTo(workingMap);
        _nameFromToMap = workingMap;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropertyValueHandler _propertyValueHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTableClassificationArranger(DfPropertyValueHandler propertyValueHandler) {
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                Table Classification
    //                                                                ====================
    public void arrangeTableClassification(Map<String, DfClassificationElement> tableClassificationMap,
            DfClassificationTop classificationTop, Map<String, Object> elementMap, String table, List<DfClassificationElement> elementList,
            Connection conn) {
        final DfClassificationElement metaElement = createBasicElement(classificationTop, elementMap, table);
        if (isCompatibleElementMapSuppressAutoDeploy(elementMap)) { // for compatible
            classificationTop.setSuppressAutoDeploy(true);
        }
        final String where = (String) elementMap.get("where");
        final String orderBy = (String) elementMap.get("orderBy");
        final Set<String> exceptCodeSet = extractExceptCodeSet(classificationTop, elementMap);
        final String sql = buildTableClassificationSql(metaElement, table, where, orderBy);
        selectTableClassification(classificationTop, elementList, metaElement, exceptCodeSet, conn, sql);
        final String classificationName = classificationTop.getClassificationName();
        tableClassificationMap.put(classificationName, metaElement); // e.g. for auto-deploy and determination
        metaElement.setClassificationTop(classificationTop);
    }

    protected DfClassificationElement createBasicElement(DfClassificationTop classificationTop, Map<String, Object> elementMap,
            String table) {
        final DfClassificationElement metaElement = new DfClassificationElement();
        metaElement.setClassificationName(classificationTop.getClassificationName());
        metaElement.setTable(table);
        metaElement.acceptBasicItemMap(elementMap);
        return metaElement;
    }

    @SuppressWarnings("unchecked")
    protected boolean isCompatibleElementMapSuppressAutoDeploy(Map<?, ?> elementMap) {
        // copied from literal arranger to suppress dependency
        final String key = DfClassificationTop.KEY_SUPPRESS_AUTO_DEPLOY;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    // -----------------------------------------------------
    //                                             Build SQL
    //                                             ---------
    protected String buildTableClassificationSql(DfClassificationElement element, String table, String where, String orderBy) {
        final String code = quoteColumnNameIfNeedsDirectUse(element.getCode());
        final String name = quoteColumnNameIfNeedsDirectUse(element.getName());
        final String alias = quoteColumnNameIfNeedsDirectUse(element.getAlias());
        final String comment = quoteColumnNameIfNeedsDirectUse(element.getComment());
        final String[] sisters = element.getSisters();
        final Map<String, Object> subItemPropMap = element.getSubItemMap();
        final StringBuilder sb = new StringBuilder();
        sb.append("select ").append(code).append(" as cls_code");
        sb.append(", ").append(name).append(" as cls_name");
        sb.append(ln());
        sb.append("     , ").append(alias).append(" as cls_alias");
        final String commentColumn = Srl.is_NotNull_and_NotTrimmedEmpty(comment) ? comment : "null";
        sb.append(", ").append(commentColumn).append(" as cls_comment");
        if (sisters != null && sisters.length > 0) {
            for (String sister : sisters) {
                sb.append(", ").append(sister).append(" as cls_sister");
            }
        }
        if (subItemPropMap != null && !subItemPropMap.isEmpty()) {
            for (Entry<String, Object> entry : subItemPropMap.entrySet()) {
                sb.append(", ").append(entry.getValue()).append(" as cls_").append(entry.getKey());
            }
        }
        sb.append(ln());
        sb.append("  from ").append(quoteTableNameIfNeedsDirectUse(table));
        // where and order-by is unsupported to be quoted
        if (Srl.is_NotNull_and_NotTrimmedEmpty(where)) {
            // no line feed when normal table to suppress big logging when normal
            if (hasSqlMark(table)) {
                sb.append(ln());
            }
            sb.append(" where ").append(where);
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(orderBy)) {
            // same logic as where clause about line feed
            if (hasSqlMark(table)) {
                sb.append(ln());
            }
            sb.append(" order by ").append(orderBy);
        }
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                        Quote SQL Name
    //                                        --------------
    protected String quoteTableNameIfNeedsDirectUse(String tableName) {
        if (Srl.is_Null_or_TrimmedEmpty(tableName)) {
            return tableName;
        }
        if (hasSqlMark(tableName)) {
            return extractSqlMarkRemovedName(tableName);
        }
        final DfLittleAdjustmentProperties littleProp = getLittleAdjustmentProperties();
        return littleProp.quoteTableNameIfNeedsDirectUse(tableName);
    }

    protected String quoteColumnNameIfNeedsDirectUse(String columnName) {
        if (Srl.is_Null_or_TrimmedEmpty(columnName)) {
            return columnName;
        }
        if (hasSqlMark(columnName)) {
            return extractSqlMarkRemovedName(columnName);
        }
        final DfLittleAdjustmentProperties littleProp = getLittleAdjustmentProperties();
        return littleProp.quoteColumnNameIfNeedsDirectUse(columnName);
    }

    // -----------------------------------------------------
    //                                              SQL Mark
    //                                              --------
    protected boolean hasSqlMark(String tableName) {
        final String sqlMark = SQL_MARK;
        return Srl.startsWithIgnoreCase(tableName, sqlMark);
    }

    protected String extractSqlMarkRemovedName(String name) {
        final String sqlMark = SQL_MARK;
        return Srl.substringFirstRearIgnoreCase(name, sqlMark).trim();
    }

    // -----------------------------------------------------
    //                                           Except Code
    //                                           -----------
    protected Set<String> extractExceptCodeSet(DfClassificationTop classificationTop, final Map<?, ?> elementMap) {
        final Set<String> exceptCodeSet;
        final Object exceptCodeObj = (String) elementMap.get("exceptCodeList");
        if (exceptCodeObj != null) {
            if (!(exceptCodeObj instanceof List<?>)) {
                String msg = "'exceptCodeList' should be java.util.List! But: " + exceptCodeObj.getClass();
                msg = msg + " value=" + exceptCodeObj + " " + classificationTop.getClassificationName();
                throw new DfIllegalPropertyTypeException(msg);
            }
            final List<?> exceptCodeList = (List<?>) exceptCodeObj;
            exceptCodeSet = StringSet.createAsCaseInsensitive();
            for (Object exceptCode : exceptCodeList) {
                exceptCodeSet.add((String) exceptCode);
            }
        } else {
            exceptCodeSet = DfCollectionUtil.emptySet(); // default empty
        }
        return exceptCodeSet;
    }

    // ===================================================================================
    //                                                         Select Table Classification
    //                                                         ===========================
    protected void selectTableClassification(DfClassificationTop classificationTop, List<DfClassificationElement> elementList,
            DfClassificationElement metaElement, Set<String> exceptCodeSet, Connection conn, String sql) {
        final String classificationName = classificationTop.getClassificationName();
        final String[] sisters = metaElement.getSisters();
        final Map<String, Object> subItemPropMap = metaElement.getSubItemMap();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            _log.info("...Selecting for " + classificationName + " classification" + ln() + sql);
            rs = st.executeQuery(sql);
            final Set<String> duplicateCheckSet = StringSet.createAsCaseInsensitive();
            while (rs.next()) {
                final String code = rs.getString("cls_code");
                final String name = rs.getString("cls_name");
                final String alias = rs.getString("cls_alias");
                final String comment = rs.getString("cls_comment");

                if (exceptCodeSet.contains(code)) {
                    _log.info("  exceptd: " + code);
                    continue;
                }

                if (duplicateCheckSet.contains(code)) {
                    _log.info("  duplicate: " + code);
                    continue;
                } else {
                    duplicateCheckSet.add(code);
                }

                final Map<String, Object> selectedMap = new LinkedHashMap<>();
                selectedMap.put(DfClassificationElement.KEY_CODE, code);
                selectedMap.put(DfClassificationElement.KEY_NAME, filterTableClassificationName(classificationTop, name));
                selectedMap.put(DfClassificationElement.KEY_ALIAS, filterTableClassificationLiteralOutput(alias));
                if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) { // because of not required
                    selectedMap.put(DfClassificationElement.KEY_COMMENT, comment);
                }
                if (sisters != null && sisters.length > 0) {
                    final String sisterValue = rs.getString("cls_sister");
                    selectedMap.put(DfClassificationElement.KEY_SISTER_CODE, sisterValue);
                }
                if (subItemPropMap != null && !subItemPropMap.isEmpty()) {
                    final Map<String, Object> subItemMap = new LinkedHashMap<String, Object>();
                    for (String subItemKey : subItemPropMap.keySet()) {
                        final String clsKey = "cls_" + subItemKey;
                        final String subItemValue = rs.getString(clsKey);
                        final String subItemVeloFriendlyValue;
                        if (subItemValue != null) {
                            subItemVeloFriendlyValue = filterTableClassificationLiteralOutput(subItemValue);
                        } else {
                            subItemVeloFriendlyValue = "null"; // for determination in templates
                        }
                        subItemMap.put(subItemKey, subItemVeloFriendlyValue);
                    }
                    selectedMap.put(DfClassificationElement.KEY_SUB_ITEM_MAP, subItemMap);
                }
                final DfClassificationElement element = new DfClassificationElement();
                element.setClassificationName(classificationName);
                element.acceptBasicItemMap(selectedMap);
                elementList.add(element);
            }
        } catch (SQLException e) {
            throwTableClassificationSelectSQLFailureException(classificationName, sql, e);
            throw new SQLFailureException("Failed to execute the SQL:" + ln() + sql, e);
        } finally {
            new DfClassificationJdbcCloser().closeStatement(st, rs);
        }
    }

    protected void throwTableClassificationSelectSQLFailureException(String classificationName, String sql, SQLException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to select the classification resource from the table.");
        br.addItem("Advice");
        br.addElement("Make sure your classificationDefinitionMap.dfprop.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("     ; MemberStatus = list:{");
        br.addElement("         ; map:{");
        br.addElement("             ; table=NOEXISTING_STATUS                       // *NG");
        br.addElement("             ; code=MEMBER_STATUS_CODE; name=MEMBER_STATUS_NAME");
        br.addElement("             ; comment=DESCRIPTION; orderBy=DISPLAY_ORDER");
        br.addElement("         }");
        br.addElement("     }");
        br.addElement("  (x):");
        br.addElement("     ; MemberStatus = list:{");
        br.addElement("         ; map:{");
        br.addElement("             ; table=MEMBER_STATUS");
        br.addElement("             ; code=MEMBER_STATUS_CODE; name=NOEXISTING_NAME // *NG");
        br.addElement("             ; comment=DESCRIPTION; orderBy=DISPLAY_ORDER");
        br.addElement("         }");
        br.addElement("     }");
        br.addElement("  (o):");
        br.addElement("     ; MemberStatus = list:{");
        br.addElement("         ; map:{");
        br.addElement("             ; table=MEMBER_STATUS                           // OK");
        br.addElement("             ; code=MEMBER_STATUS_CODE; name=MEMBER_STATUS_NAME");
        br.addElement("             ; comment=DESCRIPTION; orderBy=DISPLAY_ORDER");
        br.addElement("         }");
        br.addElement("     }");
        br.addElement("");
        br.addElement("Or remove it if the table is deleted from your schema.");
        br.addItem("Classification");
        br.addElement(classificationName);
        br.addItem("SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new SQLFailureException(msg, e);
    }

    // ===================================================================================
    //                                                          Filter Classification Name 
    //                                                          ==========================
    protected String filterTableClassificationName(DfClassificationTop classificationTop, String name) {
        if (Srl.is_Null_or_TrimmedEmpty(name)) {
            return name;
        }
        name = Srl.replaceBy(name, _nameFromToMap);
        if (Character.isDigit(name.charAt(0))) {
            name = "N" + name;
        }
        if (classificationTop.isSuppressNameCamelizing()) {
            // basically plain but only remove characters that cannot be method name
            return Srl.replace(name, "-", "");
        } else { // normally here
            return Srl.camelize(name, "_", "-"); // for method name
        }
    }

    protected final Map<String, String> _literalOutputFromToMap = new LinkedHashMap<>();
    {
        _literalOutputFromToMap.put("\r", "\\r");
        _literalOutputFromToMap.put("\n", "\\n");
        _literalOutputFromToMap.put("\"", "\\\"");
    }

    protected String filterTableClassificationLiteralOutput(String alias) {
        if (Srl.is_Null_or_TrimmedEmpty(alias)) {
            return alias;
        }
        return Srl.replaceBy(alias, _literalOutputFromToMap);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                    Flexible Handler
    //                                                                    ================
    protected String getProperty(String key, String defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.getProperty(key, defaultValue, map);
    }

    protected boolean isProperty(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.isProperty(key, defaultValue, map);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}
