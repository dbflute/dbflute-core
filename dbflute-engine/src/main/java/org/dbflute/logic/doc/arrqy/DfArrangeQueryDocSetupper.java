/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.doc.arrqy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.9 (2018/12/31 Monday)
 */
public class DfArrangeQueryDocSetupper {

    protected static final String METHOD_PREFIX = "arrange";

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfArrangeQueryDocSetupper.class);

    public static final String KEY_PATH = "path";
    public static final String KEY_SUB_DIRECTORY_PATH = "subDirectoryPath";
    public static final String KEY_ENTITY_NAME = "entityName";
    public static final String KEY_BEHAVIOR_NAME = "behaviorName";
    public static final String KEY_BEHAVIOR_QUERY_PATH = "behaviorQueryPath";
    public static final String KEY_SQLAP = "sqlAp";
    public static final String KEY_SQLAP_PROJECT_NAME = "sqlApProjectName";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_SQL = "sql";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfArrangeQueryDocSetupper() {
    }

    // ===================================================================================
    //                                                                              Set up 
    //                                                                              ======
    public Map<String, DfArrangeQueryTable> extractArrangeQuery(List<Table> tableList) {
        if (getDocumentProperties().isSuppressSchemaHtmlArrangeQuery()) {
            _log.info("*ArrangeQuery Doc is suppressed!");
            return Collections.emptyMap();
        }
        try {
            return doExtractArrangeQuery(tableList);
        } catch (RuntimeException e) { // just in case for now
            _log.warn("Failed to extract arrange query meta.", e);
            return Collections.emptyMap();
        }
    }

    protected Map<String, DfArrangeQueryTable> doExtractArrangeQuery(List<Table> tableList) {
        final Map<String, DfArrangeQueryTable> resultMap = new LinkedHashMap<String, DfArrangeQueryTable>();
        for (Table table : tableList) {
            final String tableDbName = table.getTableDbName();
            String beanClassName = table.getExtendedConditionBeanClassName();
            String queryClassName = table.getExtendedConditionQueryClassName();
            final DfArrangeQueryTable queryTable = new DfArrangeQueryTable(tableDbName, beanClassName, queryClassName);
            {
                final String cbeanPath = prepareConditionBeanPath(table);
                final List<DfArrangeQueryMethod> cbeanMethodList = searchArrangeQueryMethodList(tableDbName, cbeanPath);
                for (DfArrangeQueryMethod method : cbeanMethodList) {
                    queryTable.addBeanMethod(method);
                }
            }
            {
                final String cqueryPath = prepareConditionQueryPath(table);
                final List<DfArrangeQueryMethod> cqueryMethodList = searchArrangeQueryMethodList(tableDbName, cqueryPath);
                for (DfArrangeQueryMethod method : cqueryMethodList) {
                    queryTable.addQueryMethod(method);
                }
            }
            if (!queryTable.getBeanMethodList().isEmpty() || !queryTable.getQueryMethodList().isEmpty()) {
                resultMap.put(tableDbName, queryTable);
            }
        }
        return resultMap;
    }

    // ===================================================================================
    //                                                                          Class File
    //                                                                          ==========
    protected String prepareConditionBeanPath(Table table) {
        final String pkg = getBasicProperties().getLanguageDependency().getLanguageClassPackage().getConditionBeanPackage();
        final String className = table.getExtendedConditionBeanClassName();
        return doPrepareConditionAnyPath(pkg, className);
    }

    protected String prepareConditionQueryPath(Table table) {
        final String pkg = getBasicProperties().getLanguageDependency().getLanguageClassPackage().getConditionQueryPackage();
        final String className = table.getExtendedConditionQueryClassName();
        return doPrepareConditionAnyPath(pkg, className);
    }

    protected String doPrepareConditionAnyPath(String pkg, String className) {
        final DfBasicProperties basicProp = getBasicProperties();
        final String generateOutputDirectory = basicProp.getGenerateOutputDirectory();
        final String packageBase = basicProp.getPackageBase();
        final String packagePath = Srl.replace(packageBase + "." + pkg, ".", "/");
        final String classFileExtension = basicProp.getLanguageDependency().getLanguageGrammar().getClassFileExtension();
        return generateOutputDirectory + "/" + packagePath + "/" + className + "." + classFileExtension;
    }

    protected FileTextIO prepareSourceFileTextIO() {
        final FileTextIO textIO = new FileTextIO();
        textIO.setEncoding(getBasicProperties().getSourceFileEncoding());
        return textIO;
    }

    // ===================================================================================
    //                                                                      Analyze Method
    //                                                                      ==============
    protected boolean isArrangeQueryMethodFirstLine(String line) {
        return Srl.ltrim(line).startsWith("public ") && Srl.containsAll(line, METHOD_PREFIX, "(");
    }

    protected List<DfArrangeQueryMethod> searchArrangeQueryMethodList(String tableDbName, String classFilePath) {
        final FileTextIO textIO = prepareSourceFileTextIO();
        final String cqText = textIO.read(classFilePath);
        final List<String> lineList = Srl.splitList(cqText, "\n");
        return analyzeArrangeQueryLineList(tableDbName, lineList);
    }

    protected List<DfArrangeQueryMethod> analyzeArrangeQueryLineList(String tableDbName, List<String> lineList) {
        // #hope jflute should use Java parser? (2018/12/31)
        final List<DfArrangeQueryMethod> methodList = new ArrayList<DfArrangeQueryMethod>();
        String javadocTitle = null;
        boolean inJavaDoc = false;
        for (String line : lineList) {
            if (Srl.equalsPlain(Srl.trim(line), "/**")) {
                inJavaDoc = true;
                javadocTitle = null;
            } else if (Srl.contains(line, "*/")) {
                inJavaDoc = false;
            } else if (inJavaDoc && Srl.startsWith(Srl.ltrim(line), "*")) {
                if (javadocTitle == null) { // to use first line
                    javadocTitle = Srl.substringFirstRear(line, "*").trim();
                }
            } else if (Srl.trim(line).isEmpty()) { // empty line
                javadocTitle = null; // may be different
            } else if (Srl.ltrim(line).startsWith("//")) { // line comment line
                javadocTitle = null; // may be different
            } else if (Srl.containsAny(line, "public", "protected", "private") && !Srl.contains(line, METHOD_PREFIX)) { // other field or method
                javadocTitle = null;
            }
            if (isArrangeQueryMethodFirstLine(line)) {
                final ScopeInfo scopeFirst = Srl.extractScopeFirst(line, METHOD_PREFIX, "(");
                if (scopeFirst == null) { // basically no way, but just in case
                    continue;
                }
                final String businessName = scopeFirst.getContent();
                if (businessName.isEmpty()) { // e.g. arrange(), may be in TemplateClass
                    continue;
                }
                final String methodName = METHOD_PREFIX + businessName;
                final DfArrangeQueryMethod method = new DfArrangeQueryMethod(tableDbName, methodName);
                if (javadocTitle != null) {
                    method.setTitle(javadocTitle);
                }
                methodList.add(method);
            }
        }
        return methodList;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected boolean isApplicationBehaviorProject() {
        return getBasicProperties().isApplicationBehaviorProject();
    }

    protected String getLibraryProjectPrefix() {
        return getBasicProperties().getLibraryProjectPrefix();
    }

    protected String getApplicationBehaviorAdditionalSuffix() {
        return getBasicProperties().getApplicationBehaviorAdditionalSuffix();
    }

    protected String getBhvApResolvedProjectPrefix() {
        return getBasicProperties().getBhvApResolvedProjectPrefix();
    }

    protected String getBhvApResolvedBehaviorSuffix() {
        return getBasicProperties().getBhvApResolvedBehaviorSuffix();
    }
}