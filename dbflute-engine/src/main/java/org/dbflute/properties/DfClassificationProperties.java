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
package org.dbflute.properties;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.torque.engine.database.model.Database;
import org.dbflute.exception.DfClassificationIllegalPropertyTypeException;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.coins.DfClassificationJdbcCloser;
import org.dbflute.properties.assistant.classification.column.DfClsColumnClsNameArranger;
import org.dbflute.properties.assistant.classification.column.DfClsColumnDolloarAllClsNameArranger;
import org.dbflute.properties.assistant.classification.deployment.DfClsDeploymentInitializer;
import org.dbflute.properties.assistant.classification.element.proploading.DfClsElementLiteralArranger;
import org.dbflute.properties.assistant.classification.element.proploading.DfClsTableAllInOneArranger;
import org.dbflute.properties.assistant.classification.element.proploading.DfClsTableClassificationArranger;
import org.dbflute.properties.assistant.classification.resource.DfClassificationResourceDefinitionReflector;
import org.dbflute.properties.assistant.classification.resource.DfClassificationResourceDeploymentReflector;
import org.dbflute.properties.assistant.classification.resource.DfClassificationResourceExtractor;
import org.dbflute.properties.assistant.classification.top.proploading.DfClsTopLiteralArranger;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The properties for classification.
 * @author jflute
 */
public final class DfClassificationProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfClassificationProperties.class);

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
        final DfClsElementLiteralArranger literalArranger = new DfClsElementLiteralArranger();
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
                final DfClassificationTop classificationTop = new DfClassificationTop(classificationName);
                _classificationTopMap.put(classificationName, classificationTop);

                // handle classification elements
                if (!(objValue instanceof List<?>)) {
                    throwClassificationMapValueIllegalListTypeException(objValue);
                }
                final List<?> plainList = (List<?>) objValue;
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
                        arrangeTableClassification(classificationTop, elementMap, table, elementList, conn);
                        continue;
                    }

                    // from literal
                    if (isElementMapClassificationTop(elementMap)) { // top definition
                        arrangeClassificationTopFromLiteral(classificationTop, elementMap);
                    } else {
                        literalArranger.arrange(classificationName, elementMap);
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
                arrangeAllInOneTableClassification(conn, allInOneSql);
            }

            reflectClassificationResourceToDefinition(); // *Classification Resource Point!
            filterUseDocumentOnly();
            verifyClassificationConstraintsIfNeeds();
            prepareSuppressedDBAccessClassTableSet();
        } finally {
            new DfClassificationJdbcCloser().closeConnection(conn);
        }
        return _classificationTopMap;
    }

    protected void verifyClassificationConstraintsIfNeeds() {
        if (DfDBFluteTaskStatus.getInstance().isReplaceSchema()) {
            // SchemaPolicyCheck may use classification in ReplaceSchema by dfprop option,
            // but no data for table classification yet so no check here #for_now by jflute (2017/01/12)
            return;
        }
        for (DfClassificationTop classificationTop : _classificationTopMap.values()) {
            // only check one that is not compile-safe
            // (e.g. groupingMap gives us compile error if no-existence element)
            classificationTop.verifyDeprecatedElementExistence();
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
    //                                                                 Classification Name
    //                                                                 ===================
    public boolean hasClassificationName(String classificationName) {
        return getClassificationValidNameOnlyList().contains(classificationName);
    }

    // -----------------------------------------------------
    //                                       Valid Name Only
    //                                       ---------------
    // except cls whose both code and name are same... why? forgot... (2021/07/11)
    protected List<String> _classificationValidNameOnlyList; // small cache, lazy-loaded

    protected List<String> getClassificationValidNameOnlyList() {
        if (_classificationValidNameOnlyList != null) {
            return _classificationValidNameOnlyList;
        }
        _classificationValidNameOnlyList = new ArrayList<String>();
        final Map<String, DfClassificationTop> definitionMap = getClassificationTopMap();
        clsLoop: for (Entry<String, DfClassificationTop> entry : definitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            for (DfClassificationElement element : entry.getValue().getClassificationElementList()) {
                if (!element.getCode().equalsIgnoreCase(element.getName())) { // ignoreCase because codes and names may be filtered
                    _classificationValidNameOnlyList.add(classificationName);
                    continue clsLoop;
                }
            }
        }
        return _classificationValidNameOnlyList;
    }

    // -----------------------------------------------------
    //                                      Valid Alias Only
    //                                      ----------------
    // except cls whose both alias and code/name are same... why? forgot... (2021/07/11)
    public boolean hasClassificationAlias(String classificationName) {
        return getClassificationValidAliasOnlyList().contains(classificationName);
    }

    protected List<String> _classificationValidAliasOnlyList; // small cache, lazy-loaded

    protected List<String> getClassificationValidAliasOnlyList() {
        if (_classificationValidAliasOnlyList != null) {
            return _classificationValidAliasOnlyList;
        }
        _classificationValidAliasOnlyList = new ArrayList<String>();
        final Map<String, DfClassificationTop> definitionMap = getClassificationTopMap();
        clsLoop: for (Entry<String, DfClassificationTop> entry : definitionMap.entrySet()) {
            final String classificationName = entry.getKey();
            for (DfClassificationElement element : entry.getValue().getClassificationElementList()) {
                final String alias = element.getAlias();
                if (!element.getCode().equalsIgnoreCase(alias) && !element.getName().equalsIgnoreCase(alias)) { // ignoreCase me too
                    _classificationValidAliasOnlyList.add(classificationName);
                    continue clsLoop;
                }
            }
        }
        return _classificationValidAliasOnlyList;
    }

    // ===================================================================================
    //                                                                  Classification Top
    //                                                                  ==================
    protected void arrangeClassificationTopFromLiteral(DfClassificationTop classificationTop, Map<?, ?> elementMap) {
        new DfClsTopLiteralArranger(_propertyValueHandler).arrangeClassificationTopFromLiteral(classificationTop, elementMap);
    }

    // #hope jflute move these methods to formal place (2021/07/04)
    // -----------------------------------------------------
    //                                             Code Type
    //                                             ---------
    // classification interface
    public boolean isCodeTypeNeedsQuoted(String classificationName) {
        final DfClassificationTop classificationTop = getClassificationTop(classificationName);
        return classificationTop != null && classificationTop.isCodeTypeNeedsQuoted();
    }

    // -----------------------------------------------------
    //                          ReplaceSchema Implicit Check
    //                          ----------------------------
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
    //                                      Mapping Settings
    //                                      ----------------
    // #for_now jflute for FreeGen appcls loader, move this to small compoment (2021/07/10)
    public Map<String, Map<String, Object>> getElementMapGroupingMap(Map<?, ?> elementMap) {
        return new DfClsTopLiteralArranger(_propertyValueHandler).getElementMapGroupingMap(elementMap);
    }

    public Map<String, String> getElementMapDeprecatedMap(Map<?, ?> elementMap) {
        return new DfClsTopLiteralArranger(_propertyValueHandler).getElementMapDeprecatedMap(elementMap);
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

    protected void arrangeTableClassification(DfClassificationTop classificationTop, Map<String, Object> elementMap, String table,
            List<DfClassificationElement> elementList, Connection conn) {
        final DfClsTableClassificationArranger arranger = new DfClsTableClassificationArranger(_propertyValueHandler);
        arranger.arrangeTableClassification(_tableClassificationMap, classificationTop, elementMap, table, elementList, conn);
    }

    // -----------------------------------------------------
    //                       All-in-One Table Classification
    //                       -------------------------------
    protected void arrangeAllInOneTableClassification(Connection conn, String sql) {
        final DfClsTableAllInOneArranger arranger = new DfClsTableAllInOneArranger();
        arranger.arrangeAllInOneTableClassification(_classificationTopMap, conn, sql);
    }

    // ===================================================================================
    //                                                              Classification Element
    //                                                              ======================
    public List<DfClassificationElement> getClassificationElementList(String classificationName) {
        final DfClassificationTop classificationTop = getClassificationTopMap().get(classificationName);
        return classificationTop != null ? classificationTop.getClassificationElementList() : null;
    }

    public List<String> getClassificationElementCodeList(String classificationName) {
        final List<String> codeList = DfCollectionUtil.newArrayList();
        for (DfClassificationElement element : getClassificationElementList(classificationName)) {
            codeList.add(element.getCode());
        }
        return codeList;
    }

    // -----------------------------------------------------
    //                                   Application Comment
    //                                   -------------------
    public String buildClassificationApplicationCommentForJavaDoc(DfClassificationElement classificationElement) {
        return classificationElement.buildClassificationApplicationCommentForJavaDoc();
    }

    public String buildClassificationApplicationCommentForSchemaHtml(DfClassificationElement classificationElement) {
        return classificationElement.buildClassificationApplicationCommentForSchemaHtml();
    }

    public String buildClassificationCodeAliasVariables(DfClassificationElement classificationElement) {
        return classificationElement.buildClassificationCodeAliasVariables();
    }

    public String buildClassificationCodeAliasSisterCodeVariables(DfClassificationElement classificationElement) {
        return classificationElement.buildClassificationCodeAliasSisterCodeVariables();
    }

    // -----------------------------------------------------
    //                                  Variables Expression
    //                                  --------------------
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

    // -----------------------------------------------------
    //                                           SubItem Map
    //                                           -----------
    public boolean hasClassificationSubItemMap(String classificationName) {
        return getClassificationTop(classificationName).hasSubItem();
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
     * Initialize classification deployment. <br>
     * Resolving all column classifications and table classifications. <br>
     * You can call this several times with other database objects. <br>
     * This method calls initializeClassificationDefinition() internally.
     * @param database The database object. (NotNull)
     */
    public void initializeClassificationDeployment(Database database) {
        final Map<String, Map<String, String>> existingDeploymentMap = getClassificationDeploymentMap();
        final Map<String, String> allColumnClassificationMap = getAllColumnClassificationMap();
        final DfClsDeploymentInitializer initializer = new DfClsDeploymentInitializer(existingDeploymentMap, _tableClassificationMap,
                allColumnClassificationMap, () -> initializeClassificationDefinition());
        _classificationDeploymentMap = initializer.initializeClassificationDeployment(database);
    }

    // unused? commented it out by jflute (2021/07/10)
    //public String getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation() {
    //    final String property = stringProp("torque." + KEY_classificationDeploymentMap, DEFAULT_EMPTY_MAP_STRING);
    //    return filterDoubleQuotation(removeLineSeparator(property));
    //}

    // ===================================================================================
    //                                                               Column Classification
    //                                                               =====================
    // -----------------------------------------------------
    //                          Classification Determination
    //                          ----------------------------
    public boolean hasClassification(String tableName, String columnName) {
        return getClassificationName(tableName, columnName) != null;
    }

    public boolean hasTableClassification(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName);
        return classificationName != null && isTableClassification(classificationName);
    }

    public boolean hasImplicitClassification(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName); // null allowed
        return classificationName != null && !isTableClassification(classificationName);
    }

    // -----------------------------------------------------
    //                          Classification Name Handling
    //                          ----------------------------
    // small cache, lazy-loaded
    protected final Map<String, StringKeyMap<String>> _fkeyColumnClassificationMap = StringKeyMap.createAsFlexible();

    public String getClassificationName(String tableName, String columnName) { // null allowed
        final DfClsColumnClsNameArranger arranger = new DfClsColumnClsNameArranger(getClassificationDeploymentMap());
        return arranger.findClassificationName(tableName, columnName, _fkeyColumnClassificationMap); // lozy-loading
    }

    public boolean hasClassificationName(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName); // null allowed
        return classificationName != null && hasClassificationName(classificationName);
    }

    // -----------------------------------------------------
    //                         Classification Alias Handling
    //                         -----------------------------
    public boolean hasClassificationAlias(String tableName, String columnName) {
        final String classificationName = getClassificationName(tableName, columnName); // null allowed
        return classificationName != null && hasClassificationAlias(classificationName);
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

    // small cache, lazy-loaded
    protected final Map<String, String> _fkeyAllColumnClassificationMap = StringKeyMap.createAsFlexible();

    /**
     * Get the name of classification for all column.
     * @param columnName The name of column. (NotNull)
     * @return The name of classification for all column. (NullAllowed: If NotFound)
     */
    public String getAllClassificationName(String columnName) {
        final DfClsColumnDolloarAllClsNameArranger arranger = new DfClsColumnDolloarAllClsNameArranger(getAllColumnClassificationMap());
        return arranger.findAllClassificationName(columnName, _fkeyAllColumnClassificationMap);
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
    protected List<DfClassificationTop> _classificationResourceList;

    protected List<DfClassificationTop> getClassificationResourceList() {
        if (_classificationResourceList != null) {
            return _classificationResourceList;
        }
        _classificationResourceList = extractClassificationResource();
        return _classificationResourceList;
    }

    protected List<DfClassificationTop> extractClassificationResource() {
        final DfClassificationResourceExtractor extractor =
                new DfClassificationResourceExtractor(() -> isSpecifiedEnvironmentType(), () -> getEnvironmentType());
        return extractor.extractClassificationResource();
    }

    // -----------------------------------------------------
    //                                 Reflect to Definition
    //                                 ---------------------
    protected void reflectClassificationResourceToDefinition() { // called by definition initialization
        final List<DfClassificationTop> resourceList = getClassificationResourceList();
        final DfClassificationResourceDefinitionReflector reflector = new DfClassificationResourceDefinitionReflector(resourceList);
        reflector.reflectClassificationResourceToDefinition(_classificationTopMap);
    }

    // -----------------------------------------------------
    //                                 Reflect to Deployment
    //                                 ---------------------
    protected void reflectClassificationResourceToDeployment() { // called by deployment initialization
        final List<DfClassificationTop> resourceList = getClassificationResourceList();
        final DfClassificationResourceDeploymentReflector reflector =
                new DfClassificationResourceDeploymentReflector(resourceList, () -> setupAllColumnClassificationEmptyMapIfNeeds());
        reflector.reflectClassificationResourceToDeployment(() -> getAllColumnClassificationMap());
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