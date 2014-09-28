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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.exception.DfClassificationIllegalPropertyTypeException;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationAllInOneSqlExecutor;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationElement;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationLiteralArranger;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationResourceAnalyzer;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationSqlResourceCloser;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationTop;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The properties for classification.
 * @author jflute
 */
public final class DfClassificationProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfClassificationProperties.class);
    private static final String SQL_MARK = "$sql:";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The element map of table classification. The key is classification name. (NotNull) */
    protected final Map<String, DfClassificationElement> _tableClassificationMap = newLinkedHashMap();

    /** The name set of table suppressing DB-access-class. (NotNull) */
    protected final Set<String> _suppressedDBAccessClassTableSet = StringSet.createAsFlexible();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                           Classification Definition
    //                                                           =========================
    public static final String KEY_classificationDefinitionMap = "classificationDefinitionMap";

    protected Map<String, DfClassificationTop> _classificationTopMap;
    protected final Set<String> _documentOnlyClassificationSet = newLinkedHashSet();

    // -----------------------------------------------------
    //                                       Public Accessor
    //                                       ---------------
    public boolean hasClassificationDefinition() {
        return !getClassificationTopMap().isEmpty();
    }

    public List<String> getClassificationNameList() { // all classifications
        return new ArrayList<String>(getClassificationTopMap().keySet());
    }

    public boolean hasClassificationTop(String classificationName) {
        return getClassificationTopMap().containsKey(classificationName);
    }

    public DfClassificationTop getClassificationTop(String classificationName) {
        return getClassificationTopMap().get(classificationName);
    }

    /**
     * Get the map of classification TOP info.
     * @return The classification TOP info. (NotNull)
     */
    public Map<String, DfClassificationTop> getClassificationTopMap() {
        if (_classificationTopMap != null) {
            return _classificationTopMap;
        }
        initializeClassificationDefinition();
        return _classificationTopMap;
    }

    // -----------------------------------------------------
    //                                     Native Definition
    //                                     -----------------
    /**
     * Get the map of classification definition.
     * @return The map of classification definition. (NotNull)
     */
    protected Map<String, DfClassificationTop> getClassificationDefinitionMap() {
        if (_classificationTopMap != null) {
            return _classificationTopMap;
        }
        _classificationTopMap = newLinkedHashMap();

        final Map<String, Object> plainDefinitionMap;
        {
            final String mapName = KEY_classificationDefinitionMap;
            final String propKey = "torque." + mapName;
            plainDefinitionMap = resolveSplit(mapName, mapProp(propKey, DEFAULT_EMPTY_MAP));
        }
        final DfClassificationLiteralArranger literalArranger = new DfClassificationLiteralArranger();
        String allInOneSql = null;
        Connection conn = null;
        try {
            for (Entry<String, Object> entry : plainDefinitionMap.entrySet()) {
                final String classificationName = entry.getKey();
                final Object objValue = entry.getValue();

                // handle special elements
                if (classificationName.equalsIgnoreCase("$$SQL$$")) {
                    allInOneSql = (String) objValue;
                    continue;
                }

                // check duplicate classification
                if (_classificationTopMap.containsKey(classificationName)) {
                    String msg = "Duplicate classification: " + classificationName;
                    throw new DfIllegalPropertySettingException(msg);
                }
                final DfClassificationTop classificationTop = new DfClassificationTop();
                classificationTop.setClassificationName(classificationName);
                _classificationTopMap.put(classificationName, classificationTop);

                // handle classification elements
                if (!(objValue instanceof List<?>)) {
                    throwClassificationMapValueIllegalListTypeException(objValue);
                }
                final List<?> plainList = (List<?>) objValue;
                final List<Map<String, Object>> elementMapList = new ArrayList<Map<String, Object>>();
                final List<DfClassificationElement> elementList = new ArrayList<DfClassificationElement>();
                boolean tableClassification = false;
                for (Object element : plainList) {
                    if (!(element instanceof Map<?, ?>)) {
                        throwClassificationListElementIllegalMapTypeException(element);
                    }
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> elementMap = (Map<String, Object>) element;

                    // from table
                    final String table = (String) elementMap.get(DfClassificationElement.KEY_TABLE);
                    if (Srl.is_NotNull_and_NotTrimmedEmpty(table)) {
                        tableClassification = true;
                        if (conn == null) {
                            conn = createMainSchemaConnection(); // on demand
                        }
                        processTableClassification(classificationTop, elementMap, table, elementList, conn);
                        continue;
                    }

                    // from literal
                    if (isElementMapClassificationTop(elementMap)) { // top definition
                        processClassificationTopFromLiteralIfNeeds(classificationTop, elementMap);
                    } else {
                        literalArranger.arrange(classificationName, elementMap, elementMapList);
                        final DfClassificationElement classificationElement = new DfClassificationElement();
                        classificationElement.setClassificationName(classificationName);
                        classificationElement.acceptBasicItemMap(elementMap);
                        elementList.add(classificationElement);
                    }
                }

                // adjust classification top
                classificationTop.addClassificationElementAll(elementList);
                classificationTop.setTableClassification(tableClassification);
                _classificationTopMap.put(classificationName, classificationTop);
            }

            if (allInOneSql != null) {
                if (conn == null) {
                    conn = createMainSchemaConnection(); // on demand
                }
                processAllInOneTableClassification(conn, allInOneSql);
            }

            reflectClassificationResourceToDefinition(); // *Classification Resource Point!
            filterUseDocumentOnly();
            checkClassificationConstraints();
            prepareSuppressedDBAccessClassTableSet();
        } finally {
            new DfClassificationSqlResourceCloser().closeConnection(conn);
        }
        return _classificationTopMap;
    }

    protected void checkClassificationConstraints() {
        for (DfClassificationTop classificationTop : _classificationTopMap.values()) {
            // only check one that is not compile-safe
            // (e.g. groupingMap gives us compile error if no-existence element)
            classificationTop.checkDeprecatedElementExistence();
        }
    }

    protected void prepareSuppressedDBAccessClassTableSet() {
        for (Entry<String, DfClassificationElement> entry : _tableClassificationMap.entrySet()) {
            final DfClassificationElement element = entry.getValue();
            if (element.getClassificationTop().isSuppressDBAccessClass()) {
                _suppressedDBAccessClassTableSet.add(element.getTable());
            }
        }
    }

    // -----------------------------------------------------
    //                                            Connection
    //                                            ----------
    protected Connection createMainSchemaConnection() {
        return getDatabaseProperties().createMainSchemaConnection();
    }

    // -----------------------------------------------------
    //                                    Classification Top
    //                                    ------------------
    protected boolean isElementMapClassificationTop(Map<?, ?> elementMap) {
        return elementMap.get(DfClassificationTop.KEY_TOP_COMMENT) != null; // topComment is main mark
    }

    // -----------------------------------------------------
    //                                    Exception Handling
    //                                    ------------------
    protected void throwClassificationMapValueIllegalListTypeException(Object value) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The value of map for classification definition was not map type.");
        br.addItem("Advice");
        br.addElement("A value of map for classification definition should be list");
        br.addElement("for classification on classificationDefinitionMap.dfprop.");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Illegal Element");
        if (value != null) {
            br.addElement(value.getClass());
            br.addElement(value);
        } else {
            br.addElement(null);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationIllegalPropertyTypeException(msg);
    }

    protected void throwClassificationListElementIllegalMapTypeException(Object element) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The element of list for classification was not map type.");
        br.addItem("Advice");
        br.addElement("An element of list for classification should be map");
        br.addElement("for classification elements on classificationDefinitionMap.dfprop.");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Illegal Element");
        if (element != null) {
            br.addElement(element.getClass());
            br.addElement(element);
        } else {
            br.addElement(null);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationIllegalPropertyTypeException(msg);
    }

    // -----------------------------------------------------
    //                                          DocumentOnly
    //                                          ------------
    protected void filterUseDocumentOnly() {
        final boolean docOnlyTask = isDocOnlyTask();
        for (Entry<String, DfClassificationTop> entry : _classificationTopMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = entry.getValue();
            if (!docOnlyTask && classificationTop.isUseDocumentOnly()) {
                _log.info("...Skipping document-only classification: " + classificationName);
                // e.g. Generate or Sql2Entity, and document-only classification
                _documentOnlyClassificationSet.add(classificationName);
            }
        }
        for (String documentOnlyClassificationName : _documentOnlyClassificationSet) {
            _classificationTopMap.remove(documentOnlyClassificationName);
            _tableClassificationMap.remove(documentOnlyClassificationName);
        }
    }

    // -----------------------------------------------------
    //                                            Initialize
    //                                            ----------
    public void initializeClassificationDefinition() {
        getClassificationDefinitionMap(); // initialize
    }

    // ===================================================================================
    //                                                                  Classification Top
    //                                                                  ==================
    // -----------------------------------------------------
    //                                        Process Method
    //                                        --------------
    protected void processClassificationTopFromLiteralIfNeeds(DfClassificationTop classificationTop,
            Map<?, ?> elementMap) {
        classificationTop.acceptClassificationTopBasicItemMap(elementMap);
        classificationTop.setCheckClassificationCode(isElementMapCheckClassificationCode(elementMap));
        classificationTop.setUndefinedHandlingType(getElementMapUndefinedHandlingType(elementMap));
        classificationTop.setCheckImplicitSet(isElementMapCheckImplicitSet(elementMap));
        classificationTop.setCheckSelectedClassification(isElementMapCheckSelectedClassification(elementMap));
        classificationTop.setForceClassificationSetting(isElementMapForceClassificationSetting(elementMap));
        classificationTop.setUseDocumentOnly(isElementMapUseDocumentOnly(elementMap));
        classificationTop.setSuppressAutoDeploy(isElementMapSuppressAutoDeploy(elementMap));
        classificationTop.setSuppressDBAccessClass(isElementMapSuppressDBAccessClass(elementMap));
        classificationTop.setDeprecated(isElementMapDeprecated(elementMap));
        classificationTop.putGroupingAll(getElementMapGroupingMap(elementMap));
        classificationTop.putDeprecatedAll(getElementMapDeprecatedMap(elementMap));
    }

    // -----------------------------------------------------
    //                                             Code Type
    //                                             ---------
    // classification interface
    public boolean isCodeTypeNeedsQuoted(String classificationName) {
        final DfClassificationTop classificationTop = getClassificationTop(classificationName);
        if (classificationTop == null) {
            return false;
        }
        final String codeType = classificationTop.getCodeType();
        if (codeType == null) { // unknown
            return true; // quoted
        }
        return codeType.equalsIgnoreCase(DfClassificationTop.CODE_TYPE_STRING);
    }

    // -----------------------------------------------------
    //                                    Undefined Handling
    //                                    ------------------
    // user public since 1.1 (implemented when 1.0.5K)
    protected boolean isElementMapCheckClassificationCode(Map<?, ?> elementMap) {
        final boolean checked = getElementMapUndefinedHandlingType(elementMap).isChecked();
        if (hasElementMapUndefinedHandlingTypeProperty(elementMap)) {
            return checked;
        }
        if (hasElementMapCheckImplicitSetProperty(elementMap) && !isElementMapCheckImplicitSet(elementMap)) {
            return false;
        }
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (prop.isSuppressDefaultCheckClassificationCode()) {
            return false;
        }
        return checked;
    }

    @SuppressWarnings("unchecked")
    protected boolean hasElementMapUndefinedHandlingTypeProperty(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_UNDEFINED_HANDLING_TYPE;
        if (getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null) {
            return true;
        }
        if (getLittleAdjustmentProperties().hasClassificationUndefinedHandlingTypeProperty()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected ClassificationUndefinedHandlingType getElementMapUndefinedHandlingType(Map<?, ?> elementMap) {
        if (isElementMapCheckImplicitSet(elementMap)) {
            return ClassificationUndefinedHandlingType.EXCEPTION;
        }
        final String key = DfClassificationTop.KEY_UNDEFINED_HANDLING_TYPE;
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final ClassificationUndefinedHandlingType defaultType = prop.getClassificationUndefinedHandlingType();
        final String defaultValue = defaultType.code();
        final String code = getProperty(key, defaultValue, (Map<String, ? extends Object>) elementMap);
        final ClassificationUndefinedHandlingType handlingType = ClassificationUndefinedHandlingType.codeOf(code);
        if (handlingType == null) {
            throwUnknownClassificationUndefinedCodeHandlingTypeException(code);
        }
        return handlingType;
    }

    protected void throwUnknownClassificationUndefinedCodeHandlingTypeException(String code) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown handling type of classification undefined code.");
        br.addItem("Advice");
        br.addElement("You can specify following types:");
        for (ClassificationUndefinedHandlingType handlingType : ClassificationUndefinedHandlingType.values()) {
            br.addElement(" " + handlingType.code());
        }
        final String exampleCode = ClassificationUndefinedHandlingType.EXCEPTION.code();
        br.addElement("");
        br.addElement("For example: (classificationDefinitionMap.dfprop)");
        br.addElement("; [classification-name] = list:{");
        br.addElement("    ; map:{");
        br.addElement("        ; topComment=...");
        br.addElement("        ; codeType=String");
        br.addElement("        ; classificationUndefinedCodeHandlingType = " + exampleCode);
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("}");
        br.addItem("Specified Unknown Type");
        br.addElement(code);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // user closet, old style
    @SuppressWarnings("unchecked")
    protected boolean hasElementMapCheckImplicitSetProperty(Map<?, ?> elementMap) { // has been existed since old
        final String key = DfClassificationTop.KEY_CHECK_IMPLICIT_SET;
        return getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null;
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapCheckImplicitSet(Map<?, ?> elementMap) { // has been existed since old
        // table classification determination is set at DfClassificationTop
        final String key = DfClassificationTop.KEY_CHECK_IMPLICIT_SET;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    protected boolean isElementMapCheckSelectedClassification(Map<?, ?> elementMap) { // has been existed since old
        return getLittleAdjustmentProperties().isCheckSelectedClassification();
    }

    // ReplaceSchema check
    protected Boolean _isCheckReplaceSchemaImplicitClassificationCode; // cached for performance

    public boolean isCheckReplaceSchemaImplicitClassificationCode() {
        if (_isCheckReplaceSchemaImplicitClassificationCode != null) {
            return _isCheckReplaceSchemaImplicitClassificationCode;
        }
        _isCheckReplaceSchemaImplicitClassificationCode = false;
        for (Entry<String, DfClassificationTop> entry : getClassificationTopMap().entrySet()) {
            final DfClassificationTop classificationTop = entry.getValue();
            if (classificationTop.isCheckImplicitSet() || classificationTop.isCheckClassificationCode()) {
                _isCheckReplaceSchemaImplicitClassificationCode = true;
            }
        }
        return _isCheckReplaceSchemaImplicitClassificationCode;
    }

    // -----------------------------------------------------
    //                                  Force Classification
    //                                  --------------------
    // user public since 1.1 (implemented when 1.0.5K)
    @SuppressWarnings("unchecked")
    protected boolean hasClassificationMakeNativeTypeProperty(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_MAKE_NATIVE_TYPE_SETTER;
        return getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null;
    }

    @SuppressWarnings("unchecked")
    protected boolean isClassificationMakeNativeTypeSetter(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_MAKE_NATIVE_TYPE_SETTER;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    // user closet, but primitive control
    protected boolean isElementMapForceClassificationSetting(Map<?, ?> elementMap) {
        if (hasClassificationMakeNativeTypeProperty(elementMap)) {
            return !isClassificationMakeNativeTypeSetter(elementMap);
        }
        return getLittleAdjustmentProperties().isForceClassificationSetting();
    }

    // -----------------------------------------------------
    //                                          Small Option
    //                                          ------------
    @SuppressWarnings("unchecked")
    protected boolean isElementMapUseDocumentOnly(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_USE_DOCUMENT_ONLY;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapSuppressAutoDeploy(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_SUPPRESS_AUTO_DEPLOY;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapSuppressDBAccessClass(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_SUPPRESS_DBACCESS_CLASS;
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final boolean defaultValue = prop.isSuppressTableClassificationDBAccessClass();
        return isProperty(key, defaultValue, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapDeprecated(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_DEPRECATED;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    // -----------------------------------------------------
    //                                      Mapping Settings
    //                                      ----------------
    protected Map<String, Map<String, Object>> getElementMapGroupingMap(Map<?, ?> elementMap) {
        final Object obj = elementMap.get(DfClassificationTop.KEY_GROUPING_MAP);
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> groupingMap = (Map<String, Map<String, Object>>) obj;
        return groupingMap;
    }

    protected Map<String, String> getElementMapDeprecatedMap(Map<?, ?> elementMap) {
        final Object obj = elementMap.get(DfClassificationTop.KEY_DEPRECATED_MAP);
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> deprecatedList = (Map<String, String>) obj;
        return deprecatedList;
    }

    // ===================================================================================
    //                                                                Table Classification
    //                                                                ====================
    // -----------------------------------------------------
    //                            Basic Table Classification
    //                            --------------------------
    public boolean isTableClassification(String classificationName) {
        return _tableClassificationMap.containsKey(classificationName);
    }

    public boolean isSuppressDBAccessClassTable(String tableDbName) {
        return _suppressedDBAccessClassTableSet.contains(tableDbName);
    }

    protected void processTableClassification(DfClassificationTop classificationTop, Map<?, ?> elementMap,
            String table, List<DfClassificationElement> elementList, Connection conn) {
        final DfClassificationElement metaElement = new DfClassificationElement();
        metaElement.setClassificationName(classificationTop.getClassificationName());
        metaElement.setTable(table);
        metaElement.acceptBasicItemMap(elementMap);
        if (isElementMapSuppressAutoDeploy(elementMap)) { // for compatible
            classificationTop.setSuppressAutoDeploy(true);
        }
        final String where = (String) elementMap.get("where");
        final String orderBy = (String) elementMap.get("orderBy");
        final Set<String> exceptCodeSet = extractExceptCodeSet(classificationTop, elementMap);
        final String sql = buildTableClassificationSql(metaElement, table, where, orderBy);
        setupTableClassification(classificationTop, elementList, metaElement, exceptCodeSet, conn, sql);
        final String classificationName = classificationTop.getClassificationName();
        _tableClassificationMap.put(classificationName, metaElement); // e.g. for auto-deploy and determination
        metaElement.setClassificationTop(classificationTop);
    }

    protected String buildTableClassificationSql(DfClassificationElement element, String table, String where,
            String orderBy) {
        final String code = quoteColumnNameIfNeedsDirectUse(element.getCode());
        final String name = quoteColumnNameIfNeedsDirectUse(element.getName());
        final String alias = quoteColumnNameIfNeedsDirectUse(element.getAlias());
        final String comment = quoteColumnNameIfNeedsDirectUse(element.getComment());
        final Map<String, Object> subItemPropMap = element.getSubItemMap();
        final StringBuilder sb = new StringBuilder();
        sb.append("select ").append(code).append(" as cls_code");
        sb.append(", ").append(name).append(" as cls_name");
        sb.append(ln());
        sb.append("     , ").append(alias).append(" as cls_alias");
        final String commentColumn = Srl.is_NotNull_and_NotTrimmedEmpty(comment) ? comment : "null";
        sb.append(", ").append(commentColumn).append(" as cls_comment");
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

    protected boolean hasSqlMark(String tableName) {
        final String sqlMark = SQL_MARK;
        return Srl.startsWithIgnoreCase(tableName, sqlMark);
    }

    protected String extractSqlMarkRemovedName(String name) {
        final String sqlMark = SQL_MARK;
        return Srl.substringFirstRearIgnoreCase(name, sqlMark).trim();
    }

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

    protected void setupTableClassification(DfClassificationTop classificationTop,
            List<DfClassificationElement> elementList, DfClassificationElement metaElement, Set<String> exceptCodeSet,
            Connection conn, String sql) {
        final String classificationName = classificationTop.getClassificationName();
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

                final Map<String, Object> selectedMap = newLinkedHashMap();
                selectedMap.put(DfClassificationElement.KEY_CODE, code);
                selectedMap.put(DfClassificationElement.KEY_NAME, filterTableClassificationName(name));
                selectedMap.put(DfClassificationElement.KEY_ALIAS, filterTableClassificationLiteralOutput(alias));
                if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) { // because of not required
                    selectedMap.put(DfClassificationElement.KEY_COMMENT, comment);
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
            throw new SQLFailureException("Failed to execute the SQL:" + ln() + sql, e);
        } finally {
            new DfClassificationSqlResourceCloser().closeStatement(st, rs);
        }
    }

    protected final Map<String, String> _nameFromToMap = newLinkedHashMap();
    {
        // basic unexpected marks
        _nameFromToMap.put(" ", "_");
        _nameFromToMap.put("!", "_");
        _nameFromToMap.put("\"", "");
        _nameFromToMap.put("#", "_");
        _nameFromToMap.put("%", "_PERCENT_");
        _nameFromToMap.put("&", "_AND_");
        _nameFromToMap.put("'", "");
        _nameFromToMap.put("(", "_");
        _nameFromToMap.put(")", "_");
        _nameFromToMap.put(",", "_");
        _nameFromToMap.put(".", "_");
        _nameFromToMap.put("/", "_");
        _nameFromToMap.put("<", "_");
        _nameFromToMap.put(">", "_");
        _nameFromToMap.put("?", "_");
        _nameFromToMap.put("\n", "_");
        _nameFromToMap.put("\t", "_");

        // basic full-width marks
        _nameFromToMap.put("\uff05", "_PERCENT_");
        _nameFromToMap.put("\uff06", "_AND_");
        _nameFromToMap.put("\uff08", "_");
        _nameFromToMap.put("\uff09", "_");

        // full-width space
        _nameFromToMap.put("\u3000", "_");

        // non-compilable hyphens
        _nameFromToMap.put("\u2010", "_");
        _nameFromToMap.put("\u2212", "_");
        _nameFromToMap.put("\uff0d", "_");
    }

    protected String filterTableClassificationName(String name) {
        if (Srl.is_Null_or_TrimmedEmpty(name)) {
            return name;
        }
        name = Srl.replaceBy(name, _nameFromToMap);
        if (Character.isDigit(name.charAt(0))) {
            name = "N" + name;
        }
        return Srl.camelize(name, " ", "_", "-"); // for method name
    }

    protected final Map<String, String> _literalOutputFromToMap = newLinkedHashMap();
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

    // -----------------------------------------------------
    //                       All-in-One Table Classification
    //                       -------------------------------
    protected void processAllInOneTableClassification(Connection conn, String sql) {
        final DfClassificationAllInOneSqlExecutor executor = new DfClassificationAllInOneSqlExecutor();
        final List<Map<String, String>> resultList = executor.executeAllInOneSql(conn, sql);
        for (Map<String, String> map : resultList) {
            final String classificationName = map.get("classificationName");
            final DfClassificationTop classificationTop;
            {
                DfClassificationTop tmpTop = _classificationTopMap.get(classificationName);
                if (tmpTop == null) {
                    tmpTop = createAllInOneTableClassificationTop(classificationName);
                    _classificationTopMap.put(classificationName, tmpTop);
                }
                classificationTop = tmpTop;
            }
            // *no check and merge it
            //if (alreadySet.contains(classificationName)) {
            //    throwClassificationAlreadyExistsInDfPropException(classificationName, "All-in-One");
            //}
            if (!classificationTop.hasTopComment()) {
                final String topComment = map.get(DfClassificationTop.KEY_TOP_COMMENT);
                classificationTop.setTopComment(topComment);
            }
            if (!classificationTop.hasCodeType()) {
                final String codeType;
                {
                    String tmpType = map.get(DfClassificationTop.KEY_CODE_TYPE);
                    if (Srl.is_Null_or_TrimmedEmpty(tmpType)) {
                        // for compatibility
                        tmpType = map.get(DfClassificationTop.KEY_DATA_TYPE);
                    }
                    codeType = tmpType;
                }
                classificationTop.setCodeType(codeType);
            }
            final DfClassificationElement element = new DfClassificationElement();
            element.setClassificationName(classificationName);
            element.acceptBasicItemMap(map);
            classificationTop.addClassificationElement(element);
        }
    }

    protected DfClassificationTop createAllInOneTableClassificationTop(final String classificationName) {
        final DfClassificationTop tmpTop = new DfClassificationTop();
        tmpTop.setClassificationName(classificationName);
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        tmpTop.setCheckClassificationCode(prop.isPlainCheckClassificationCode());
        tmpTop.setUndefinedHandlingType(prop.getClassificationUndefinedHandlingType());
        tmpTop.setCheckImplicitSet(false); // unsupported
        tmpTop.setCheckSelectedClassification(prop.isCheckSelectedClassification());
        tmpTop.setForceClassificationSetting(prop.isForceClassificationSetting());
        return tmpTop;
    }

    // ===================================================================================
    //                                                                         SubItem Map
    //                                                                         ===========
    public boolean hasClassificationSubItemMap(String classificationName) {
        final List<DfClassificationElement> elementList = getClassificationElementList(classificationName);
        for (DfClassificationElement element : elementList) {
            final Map<String, Object> subItemMap = element.getSubItemMap();
            if (subItemMap != null && !subItemMap.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getClassificationSubItemList(Map<String, Object> classificationMap) {
        Object subItemObj = classificationMap.get(DfClassificationElement.KEY_SUB_ITEM_MAP);
        if (subItemObj == null) {
            return DfCollectionUtil.emptyList();
        }
        @SuppressWarnings("unchecked")
        Map<String, String> subItemMap = (Map<String, String>) subItemObj;
        return DfCollectionUtil.newArrayList(subItemMap.keySet());
    }

    // ===================================================================================
    //                                                              Derived Classification
    //                                                              ======================
    protected List<String> _classificationValidNameOnlyList;

    protected List<String> getClassificationValidNameOnlyList() {
        if (_classificationValidNameOnlyList != null) {
            return _classificationValidNameOnlyList;
        }
        _classificationValidNameOnlyList = new ArrayList<String>();
        final Map<String, DfClassificationTop> definitionMap = getClassificationTopMap();
        clsLoop: for (Entry<String, DfClassificationTop> entry : definitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = entry.getValue();
            final List<DfClassificationElement> elementList = classificationTop.getClassificationElementList();
            for (DfClassificationElement element : elementList) {
                final String code = element.getCode();
                final String name = element.getName();

                // IgnoreCase because codes and names may be filtered
                if (!code.equalsIgnoreCase(name)) {
                    _classificationValidNameOnlyList.add(classificationName);
                    continue clsLoop;
                }
            }
        }
        return _classificationValidNameOnlyList;
    }

    protected List<String> _classificationValidAliasOnlyList;

    protected List<String> getClassificationValidAliasOnlyList() {
        if (_classificationValidAliasOnlyList != null) {
            return _classificationValidAliasOnlyList;
        }
        _classificationValidAliasOnlyList = new ArrayList<String>();
        final Map<String, DfClassificationTop> definitionMap = getClassificationTopMap();
        clsLoop: for (Entry<String, DfClassificationTop> entry : definitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = entry.getValue();
            final List<DfClassificationElement> elementList = classificationTop.getClassificationElementList();
            for (DfClassificationElement element : elementList) {
                final String code = element.getCode();
                final String name = element.getName();
                final String alias = element.getAlias();

                // IgnoreCase because codes and names may be filtered
                if (!code.equalsIgnoreCase(alias) && !name.equalsIgnoreCase(alias)) {
                    _classificationValidAliasOnlyList.add(classificationName);
                    continue clsLoop;
                }
            }
        }
        return _classificationValidAliasOnlyList;
    }

    public List<DfClassificationElement> getClassificationElementList(String classificationName) {
        final DfClassificationTop classificationTop = getClassificationTopMap().get(classificationName);
        return classificationTop != null ? classificationTop.getClassificationElementList() : null;
    }

    public List<String> getClassificationElementCodeList(String classificationName) {
        final List<DfClassificationElement> elementList = getClassificationElementList(classificationName);
        final List<String> codeList = DfCollectionUtil.newArrayList();
        for (DfClassificationElement element : elementList) {
            codeList.add(element.getCode());
        }
        return codeList;
    }

    public String buildClassificationApplicationComment(DfClassificationElement classificationElement) {
        final StringBuilder sb = new StringBuilder();
        if (classificationElement.hasAlias()) {
            sb.append(classificationElement.getAlias());
        }
        if (classificationElement.hasCommentDisp()) {
            if (sb.length() > 0) {
                sb.append(": ");
            }
            final String comment = classificationElement.getCommentDisp();
            final String filtered = Srl.replace(comment, "\n", comment); // just in case (basically one line)
            sb.append(filtered);
        }
        return sb.toString();
    }

    public String buildClassificationApplicationCommentForJavaDoc(DfClassificationElement classificationElement) {
        final String comment = buildClassificationApplicationComment(classificationElement);
        return getDocumentProperties().resolveTextForJavaDoc(comment, "    "); // basically indent is unused
    }

    public String buildClassificationApplicationCommentForSchemaHtml(DfClassificationElement classificationElement) {
        final String comment = buildClassificationApplicationComment(classificationElement);
        return getDocumentProperties().resolveTextForSchemaHtml(comment);
    }

    public String buildClassificationCodeAliasVariables(DfClassificationElement classificationElement) {
        final StringBuilder sb = new StringBuilder();
        final String code = classificationElement.getCode();
        final String alias = classificationElement.getAlias();
        sb.append("\"").append(code).append("\"");
        if (alias != null && alias.trim().length() > 0) {
            sb.append(", \"").append(alias).append("\"");
        } else {
            sb.append(", null");
        }
        return sb.toString();
    }

    public String buildClassificationCodeAliasSisterCodeVariables(DfClassificationElement classificationElement) {
        final StringBuilder sb = new StringBuilder();
        final String codeAliasVariables = buildClassificationCodeAliasVariables(classificationElement);
        sb.append(codeAliasVariables);
        final String[] sisters = classificationElement.getSisters();
        sb.append(", ");
        if (sisters != null && sisters.length > 0) {
            sb.append("new String[] {");
            if (sisters != null && sisters.length > 0) {
                int index = 0;
                for (String sister : sisters) {
                    if (index > 0) {
                        sb.append(", ");
                    }
                    sb.append("\"").append(sister).append("\"");
                    ++index;
                }
            }
            sb.append("}");
        } else {
            sb.append("EMPTY_SISTERS");
        }
        return sb.toString();
    }

    public String buildClassificationCodeNameAliasVariables(DfClassificationElement classificationElement) {
        final StringBuilder sb = new StringBuilder();
        final String code = classificationElement.getCode();
        final String name = classificationElement.getName();
        final String alias = classificationElement.getAlias();
        sb.append("\"").append(code).append("\", ").append("\"").append(name).append("\", ");
        if (alias != null && alias.trim().length() > 0) {
            sb.append("\"").append(alias).append("\"");
        } else {
            sb.append("null");
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                           Classification Deployment
    //                                                           =========================
    public static final String KEY_classificationDeploymentMap = "classificationDeploymentMap";
    public static final String MARK_allColumnClassification = "$$ALL$$";
    protected Map<String, Map<String, String>> _classificationDeploymentMap;

    public Map<String, Map<String, String>> getClassificationDeploymentMap() {
        if (_classificationDeploymentMap != null) {
            return _classificationDeploymentMap;
        }
        initializeClassificationDefinition(); // precondition
        final Map<String, Object> map = mapProp("torque." + KEY_classificationDeploymentMap, DEFAULT_EMPTY_MAP);
        _classificationDeploymentMap = StringKeyMap.createAsFlexibleOrdered();
        final Set<String> deploymentMapkeySet = map.keySet();
        for (String tableName : deploymentMapkeySet) {
            final Object value = map.get(tableName);
            if (!(value instanceof Map<?, ?>)) {
                throwClassificationDeploymentIllegalMapTypeException(value);
            }
            @SuppressWarnings("unchecked")
            final Map<String, String> tmpMap = (Map<String, String>) value;
            final Set<String> tmpMapKeySet = tmpMap.keySet();

            // It's normal map because this column name key contains hint.
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            for (Object columnNameObj : tmpMapKeySet) {
                final String columnName = (String) columnNameObj;
                final String classificationName = (String) tmpMap.get(columnName);
                if (_documentOnlyClassificationSet.contains(classificationName)) {
                    continue;
                }
                columnClassificationMap.put(columnName, classificationName);
            }
            _classificationDeploymentMap.put(tableName, columnClassificationMap);
        }
        reflectClassificationResourceToDeployment(); // *Classification Resource Point!
        return _classificationDeploymentMap;
    }

    protected void throwClassificationDeploymentIllegalMapTypeException(Object value) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column-classification map was not map type.");
        br.addItem("Advice");
        br.addElement("The value should be column-classification map");
        br.addElement("on classificationDeploymentMap.dfprop.");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Illegal Value");
        if (value != null) {
            br.addElement(value.getClass());
            br.addElement(value);
        } else {
            br.addElement(null);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationIllegalPropertyTypeException(msg);
    }

    // -----------------------------------------------------
    //                                            Initialize
    //                                            ----------
    /**
     * Initialize classification deployment. <br />
     * Resolving all column classifications and table classifications. <br />
     * You can call this several times with other database objects. <br />
     * This method calls initializeClassificationDefinition() internally.
     * @param database The database object. (NotNull)
     */
    public void initializeClassificationDeployment(Database database) { // this should be called when the task start
        final Map<String, Map<String, String>> deploymentMap = getClassificationDeploymentMap();
        final Map<String, String> allColumnClassificationMap = getAllColumnClassificationMap();
        if (allColumnClassificationMap != null) {
            final List<Table> tableList = database.getTableList();
            for (Table table : tableList) {
                final Map<String, String> columnClsMap = getColumnClsMap(deploymentMap, table.getTableDbName());
                for (Entry<String, String> entry : allColumnClassificationMap.entrySet()) {
                    columnClsMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        initializeClassificationDefinition();
        for (Entry<String, DfClassificationElement> entry : _tableClassificationMap.entrySet()) {
            final DfClassificationElement element = entry.getValue();
            if (element.getClassificationTop().isSuppressAutoDeploy()) {
                continue;
            }
            final Map<String, String> columnClsMap = getColumnClsMap(deploymentMap, element.getTable());
            final String classificationName = element.getClassificationName();
            registerColumnClsIfNeeds(columnClsMap, element.getCode(), classificationName);
            final Table table = database.getTable(element.getTable());
            if (table == null || table.hasCompoundPrimaryKey()) {
                continue;
            }
            final Column column = table.getColumn(element.getCode());
            if (column == null || !column.isPrimaryKey()) {
                continue;
            }
            final List<ForeignKey> referrers = column.getReferrers();
            for (ForeignKey referrer : referrers) {
                if (!referrer.isSimpleKeyFK()) {
                    continue;
                }
                final Table referrerTable = referrer.getTable();
                final String referrerTableDbName = referrerTable.getTableDbName();
                final Map<String, String> referrerClsMap = getColumnClsMap(deploymentMap, referrerTableDbName);
                final Column localColumnAsOne = referrer.getLocalColumnAsOne();
                registerColumnClsIfNeeds(referrerClsMap, localColumnAsOne.getName(), classificationName);
            }
        }
        _classificationDeploymentMap = deploymentMap;
    }

    protected Map<String, String> getColumnClsMap(Map<String, Map<String, String>> deploymentMap, String tableName) {
        Map<String, String> columnClassificationMap = deploymentMap.get(tableName);
        if (columnClassificationMap == null) {
            // It's normal map because this column name key contains hint.
            columnClassificationMap = new LinkedHashMap<String, String>();
            deploymentMap.put(tableName, columnClassificationMap);
        }
        return columnClassificationMap;
    }

    protected void registerColumnClsIfNeeds(Map<String, String> columnClsMap, String columnName,
            String classificationName) {
        final String value = columnClsMap.get(columnName);
        if (value != null) {
            return;
        }
        columnClsMap.put(columnName, classificationName);
    }

    public String getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation() {
        final String property = stringProp("torque." + KEY_classificationDeploymentMap, DEFAULT_EMPTY_MAP_STRING);
        return filterDoubleQuotation(removeLineSeparator(property));
    }

    // ===================================================================================
    //                                                               Column Classification
    //                                                               =====================
    public boolean hasClassification(String tableName, String columnName) {
        return getClassificationName(tableName, columnName) != null;
    }

    public boolean hasTableClassification(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName);
        return classificationName != null && isTableClassification(classificationName);
    }

    public boolean hasImplicitClassification(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName);
        return classificationName != null && !isTableClassification(classificationName);
    }

    protected final Map<String, StringKeyMap<String>> _fkeyColumnClassificationMap = StringKeyMap.createAsFlexible();

    public String getClassificationName(String tableName, String columnName) {
        final Map<String, Map<String, String>> deploymentMap = getClassificationDeploymentMap();
        Map<String, String> plainMap = deploymentMap.get(tableName);
        if (plainMap == null) {
            final String allMark = MARK_allColumnClassification;
            if (deploymentMap.containsKey(allMark)) {
                // because the mark is unresolved when ReplaceSchema task
                plainMap = deploymentMap.get(allMark);
            } else {
                return null;
            }
        }
        final String foundClassificationName;
        {
            // because columnClassificationMap is not flexible map
            StringKeyMap<String> columnClassificationMap = _fkeyColumnClassificationMap.get(tableName);
            if (columnClassificationMap == null) {
                columnClassificationMap = StringKeyMap.createAsFlexible();
                columnClassificationMap.putAll(plainMap);
                _fkeyColumnClassificationMap.put(tableName, columnClassificationMap);
            }
            foundClassificationName = columnClassificationMap.get(columnName);
        }
        String classificationName = null;
        if (foundClassificationName != null) {
            classificationName = foundClassificationName;
        } else {
            for (String columnNameHint : plainMap.keySet()) {
                if (isHitByTheHint(columnName, columnNameHint)) {
                    classificationName = plainMap.get(columnNameHint);
                }
            }
        }
        return classificationName;
    }

    public boolean hasClassificationName(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName);
        if (classificationName == null) {
            return false;
        }
        return getClassificationValidNameOnlyList().contains(classificationName);
    }

    public boolean hasClassificationAlias(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName);
        if (classificationName == null) {
            return false;
        }
        return hasClassificationAlias(classificationName);
    }

    public boolean hasClassificationAlias(String classificationName) {
        return getClassificationValidAliasOnlyList().contains(classificationName);
    }

    // ===================================================================================
    //                                                           All Column Classification
    //                                                           =========================
    /**
     * Get the map of all column classification.
     * @return The map of all column classification. (NullAllowed: If the mark would be not found)
     */
    public Map<String, String> getAllColumnClassificationMap() {
        return (Map<String, String>) getClassificationDeploymentMap().get(MARK_allColumnClassification);
    }

    /**
     * Is the column target of all column classification?
     * @param columnName The name of column. (NotNull)
     * @return The determination, true or false. (If all table classification does not exist, it returns false.)
     */
    public boolean isAllClassificationColumn(String columnName) {
        return getAllClassificationName(columnName) != null;
    }

    protected Map<String, String> _fkeyAllColumnClassificationMap;

    /**
     * Get the name of classification for all column.
     * @param columnName The name of column. (NotNull)
     * @return The name of classification for all column. (NullAllowed: If NotFound)
     */
    public String getAllClassificationName(String columnName) {
        final Map<String, String> plainMap = getAllColumnClassificationMap();
        if (plainMap == null) {
            return null;
        }
        final String classificationName;
        {
            if (_fkeyAllColumnClassificationMap == null) {
                _fkeyAllColumnClassificationMap = StringKeyMap.createAsFlexible();
                _fkeyAllColumnClassificationMap.putAll(plainMap);
            }
            classificationName = _fkeyAllColumnClassificationMap.get(columnName);
        }
        if (classificationName != null) {
            return classificationName;
        }
        for (String columnNameHint : plainMap.keySet()) {
            if (isHitByTheHint(columnName, columnNameHint)) {
                return plainMap.get(columnNameHint);
            }
        }
        return null;
    }

    protected void setupAllColumnClassificationEmptyMapIfNeeds() { // for Using Classification Resource
        if (getAllColumnClassificationMap() != null) {
            return;
        }
        final Map<String, Map<String, String>> classificationDeploymentMap = getClassificationDeploymentMap();
        classificationDeploymentMap.put(MARK_allColumnClassification, new LinkedHashMap<String, String>());
    }

    // ===================================================================================
    //                                                             Classification Resource
    //                                                             =======================
    protected static final String NAME_CLASSIFICATION_RESOURCE = "classificationResource";
    protected List<DfClassificationTop> _classificationResourceList;

    protected List<DfClassificationTop> getClassificationResourceList() {
        if (_classificationResourceList != null) {
            return _classificationResourceList;
        }
        _classificationResourceList = extractClassificationResource();
        return _classificationResourceList;
    }

    protected List<DfClassificationTop> extractClassificationResource() {
        final DfClassificationResourceAnalyzer analyzer = new DfClassificationResourceAnalyzer();
        final String dirBaseName = "./dfprop";
        final String resource = NAME_CLASSIFICATION_RESOURCE;
        final String extension = "dfprop";
        if (isSpecifiedEnvironmentType()) {
            final String dirEnvName = dirBaseName + "/" + getEnvironmentType();
            final List<DfClassificationTop> ls = analyzer.analyze(dirEnvName, resource, extension);
            if (!ls.isEmpty()) {
                return ls;
            }
            return analyzer.analyze(dirBaseName, resource, extension);
        } else {
            return analyzer.analyze(dirBaseName, resource, extension);
        }
    }

    protected void reflectClassificationResourceToDefinition() {
        final Set<String> alreadySet = new HashSet<String>(_classificationTopMap.keySet());
        final List<DfClassificationTop> classificationTopList = getClassificationResourceList();
        for (DfClassificationTop classificationTop : classificationTopList) {
            final String classificationName = classificationTop.getClassificationName();
            if (alreadySet.contains(classificationName)) {
                throwClassificationAlreadyExistsInDfPropException(classificationName, "ClassificationResource");
            }
            // reflect to classification top definition
            _classificationTopMap.put(classificationName, classificationTop);
        }
    }

    protected void throwClassificationAlreadyExistsInDfPropException(String classificationName, String settingName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The classification already exists in dfprop settings.");
        br.addItem("Advice");
        br.addElement("Check the classification names in '" + settingName + "' settings.");
        br.addElement("The settings may contain classifications existing in dfprop.");
        br.addItem("Classification");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void reflectClassificationResourceToDeployment() {
        final List<DfClassificationTop> classificationTopList = getClassificationResourceList();
        for (DfClassificationTop classificationTop : classificationTopList) {
            final String classificationName = classificationTop.getClassificationName();
            final String relatedColumnName = classificationTop.getRelatedColumnName();
            if (relatedColumnName == null) {
                continue;
            }
            setupAllColumnClassificationEmptyMapIfNeeds();
            final Map<String, String> allColumnClassificationMap = getAllColumnClassificationMap();
            if (allColumnClassificationMap.containsKey(relatedColumnName)) {
                continue;
            }
            allColumnClassificationMap.put(relatedColumnName, classificationName);
        }
    }

    // ===================================================================================
    //                                                                    Check Properties
    //                                                                    ================
    // give up because all mark provides complex structure and also for Sql2Entity case
    // so cost-benefit performance is low (to suppress degrading is prior)
    //public void checkProperties(DfTableDeterminer determiner) {
    //    // check deployment only (table classification is naturally checked when select)
    //    final Map<String, Map<String, String>> deploymentMap = getClassificationDeploymentMap();
    //    final Map<String, DfClassificationTop> definitionMap = getClassificationDefinitionMap();
    //    final List<String> notFoundTableList = new ArrayList<String>();
    //    final List<String> notFoundColumnList = new ArrayList<String>();
    //    final Set<String> notFoundClsSet = new LinkedHashSet<String>(); // might be duplicate
    //    for (Entry<String, Map<String, String>> entry : deploymentMap.entrySet()) {
    //        final String tableName = entry.getKey();
    //        final boolean pureTableName = isPureTableName(tableName);
    //        boolean notFoundTable = false;
    //        if (pureTableName) {
    //            if (!determiner.hasTable(tableName)) {
    //                notFoundTableList.add(tableName);
    //                notFoundTable = true;
    //            }
    //        }
    //        final Map<String, String> columnClsMap = entry.getValue();
    //        for (Entry<String, String> columnEntry : columnClsMap.entrySet()) {
    //            final String columnName = columnEntry.getKey();
    //            final boolean pureColumnName = !columnName.contains(":"); // NOT e.g. prefix:_FLG
    //            if (pureTableName && !notFoundTable && pureColumnName) {
    //                if (!determiner.hasTableColumn(tableName, columnName)) {
    //                    notFoundColumnList.add(tableName + "." + columnName);
    //                }
    //            }
    //            final String classificationName = columnEntry.getValue();
    //            if (!definitionMap.containsKey(classificationName)) {
    //                notFoundClsSet.add(classificationName);
    //            }
    //        }
    //    }
    //    if (notFoundTableList.size() + notFoundColumnList.size() + notFoundClsSet.size() > 0) {
    //        throwClassificationDeploymentMapTableColumnNotFoundException(notFoundTableList, notFoundColumnList,
    //                notFoundClsSet);
    //    }
    //}
    //
    //protected boolean isPureTableName(String tableName) {
    //    // NOT all and NOT $sql: ...
    //    return !MARK_allColumnClassification.equalsIgnoreCase(tableName) && !tableName.contains(":");
    //}
    //
    //protected void throwClassificationDeploymentMapTableColumnNotFoundException(List<String> notFoundTableList,
    //        List<String> notFoundColumnList, Set<String> notFoundClsSet) {
    //    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //    br.addNotice("The table/column on the classification deployment was not found.");
    //    br.addItem("NotFound Table");
    //    br.addElement(notFoundTableList);
    //    br.addItem("NotFound Column");
    //    br.addElement(notFoundColumnList);
    //    br.addItem("NotFound Classification");
    //    br.addElement(notFoundClsSet);
    //    final String msg = br.buildExceptionMessage();
    //    throw new DfClassificationDeploymentMapTableColumnNotFoundException(msg);
    //}
    //
    //public static class DfClassificationDeploymentMapTableColumnNotFoundException extends RuntimeException {
    //    private static final long serialVersionUID = 1L;
    //
    //    public DfClassificationDeploymentMapTableColumnNotFoundException(String msg) {
    //        super(msg);
    //    }
    //}
}