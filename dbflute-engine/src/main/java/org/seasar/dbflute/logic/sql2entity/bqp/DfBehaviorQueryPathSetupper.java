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
package org.seasar.dbflute.logic.sql2entity.bqp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfBehaviorNotFoundException;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlFile;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfSql2EntityMarkAnalyzer;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfBehaviorQueryPathSetupper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfBehaviorQueryPathSetupper.class);

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
    public DfBehaviorQueryPathSetupper() {
    }

    // ===================================================================================
    //                                                                              Set up 
    //                                                                              ======
    /**
     * @param sqlFileList The list of SQL file. (NotNull)
     */
    public void setupBehaviorQueryPath(DfOutsideSqlPack sqlFileList) {
        if (getOutsideSqlProperties().isSuppressBehaviorQueryPath()) {
            _log.info("*Behavior Query Path is suppressed!");
            return;
        }
        if (sqlFileList.isEmpty()) {
            return;
        }
        final Map<String, Map<String, String>> behaviorQueryPathMap = doExtractBehaviorQueryPathMap(sqlFileList);
        reflectBehaviorQueryPath(behaviorQueryPathMap);
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    /**
     * Extract the basic map of behavior query path
     * @param outsideSqlPack The pack object for outside-SQL file. (NotNull)
     * @return The basic map of behavior query path. The key is slash-path. (NotNull, EmptyAllowd: means not found)
     */
    public Map<String, Map<String, String>> extractBasicBqpMap(DfOutsideSqlPack outsideSqlPack) {
        return doExtractBehaviorQueryPathMap(outsideSqlPack);
    }

    /**
     * Extract the case insensitive map of table behavior query path.
     * <pre>
     * map:{
     *     [tablePropertyName] = map:{
     *         [behaviorQueryPath] = map:{
     *             ; path = [value]
     *             ; behaviorName = [value]
     *             ; entityName = [value]
     *             ; subDirectoryPath = [value]
     *             ; behaviorQueryPath = [value]
     *         }
     *     } 
     * }
     * </pre>
     * @param outsideSqlPack The pack object for outside-SQL file. (NotNull)
     * @return The case insensitive map of behavior query path per table. The key is table name. (NotNull, EmptyAllowd: means not found)
     */
    public Map<String, Map<String, Map<String, String>>> extractTableBqpMap(DfOutsideSqlPack outsideSqlPack) {
        final Map<String, Map<String, Map<String, String>>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        if (outsideSqlPack.isEmpty()) {
            return resultMap;
        }
        final Map<String, Map<String, String>> bqpMap = doExtractBehaviorQueryPathMap(outsideSqlPack);
        final Map<File, Map<String, Map<String, String>>> resourceMap = createTableResourceMap(bqpMap);
        final Set<Entry<File, Map<String, Map<String, String>>>> entrySet = resourceMap.entrySet();
        for (Entry<File, Map<String, Map<String, String>>> entry : entrySet) {
            final File bsbhvFile = entry.getKey();
            final DfBqpBehaviorFile bqpBehaviorFile = new DfBqpBehaviorFile(bsbhvFile);
            final String tableKeyName = bqpBehaviorFile.getTableKeyName();
            resultMap.put(tableKeyName, entry.getValue());
        }
        return resultMap;
    }

    // ===================================================================================
    //                                                                        Main Process
    //                                                                        ============
    /**
     * @param outsideSqlPack The pack object for outside-SQL file. (NotNull)
     * @return The map of behavior query path. (NotNull)
     */
    protected Map<String, Map<String, String>> doExtractBehaviorQueryPathMap(DfOutsideSqlPack outsideSqlPack) {
        final Map<String, Map<String, String>> behaviorQueryPathMap = new LinkedHashMap<String, Map<String, String>>();
        gatherBehaviorQueryPathInfo(behaviorQueryPathMap, outsideSqlPack);
        return behaviorQueryPathMap;
    }

    /**
     * @param behaviorQueryPathMap The empty map of behavior query path. (NotNull)
     * @param outsideSqlPack The pack object for outside-SQL file. (NotNull)
     */
    protected void gatherBehaviorQueryPathInfo(Map<String, Map<String, String>> behaviorQueryPathMap,
            DfOutsideSqlPack outsideSqlPack) {
        for (DfOutsideSqlFile outsideSqlFile : outsideSqlPack.getOutsideSqlFileList()) {
            final DfBqpOutsideSqlFile bqpOutsideSqlFile = new DfBqpOutsideSqlFile(outsideSqlFile);
            if (!bqpOutsideSqlFile.isBqp()) {
                continue;
            }
            final Map<String, String> behaviorQueryElement = new LinkedHashMap<String, String>();
            final String path = bqpOutsideSqlFile.getFilePath();
            behaviorQueryElement.put(KEY_PATH, path);
            behaviorQueryElement.put(KEY_SUB_DIRECTORY_PATH, bqpOutsideSqlFile.getSubDirectoryPath());
            behaviorQueryElement.put(KEY_ENTITY_NAME, bqpOutsideSqlFile.getEntityName());
            behaviorQueryElement.put(KEY_BEHAVIOR_NAME, bqpOutsideSqlFile.getBehaviorName());
            behaviorQueryElement.put(KEY_BEHAVIOR_QUERY_PATH, bqpOutsideSqlFile.getBehaviorQueryPath());
            if (outsideSqlFile.isSqlAp()) {
                behaviorQueryElement.put(KEY_SQLAP, "true");
                behaviorQueryElement.put(KEY_SQLAP_PROJECT_NAME, outsideSqlFile.getProjectName());
            }
            behaviorQueryPathMap.put(path, behaviorQueryElement);

            // setup informations in the SQL file
            setupInfoInSqlFile(outsideSqlFile, behaviorQueryElement);
        }
    }

    protected void setupInfoInSqlFile(DfOutsideSqlFile outsideSqlFile, Map<String, String> elementMap) {
        final DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        final BufferedReader reader = new BufferedReader(newInputStreamReader(outsideSqlFile));
        final StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append(ln());
            }
        } catch (IOException e) {
            String msg = "Failed to read the SQL: " + outsideSqlFile;
            throw new IllegalStateException(msg, e);
        }
        final String sql = sb.toString();
        final String customizeEntity = analyzer.getCustomizeEntityName(sql);
        final String parameterBean = analyzer.getParameterBeanName(sql);
        elementMap.put("customizeEntity", customizeEntity);
        elementMap.put("parameterBean", parameterBean);
        elementMap.put("cursor", analyzer.isCursor(sql) ? "cursor" : null);
        elementMap.put(KEY_TITLE, analyzer.getTitle(sql));
        elementMap.put(KEY_DESCRIPTION, analyzer.getDescription(sql));
        elementMap.put(KEY_SQL, sql);
    }

    protected InputStreamReader newInputStreamReader(DfOutsideSqlFile sqlFile) {
        final String encoding = getProperties().getOutsideSqlProperties().getSqlFileEncoding();
        try {
            return new InputStreamReader(new FileInputStream(sqlFile.getPhysicalFile()), encoding);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The file does not exist: " + sqlFile, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The encoding is unsupported: " + encoding, e);
        }
    }

    /**
     * @param behaviorQueryPathMap The map of behavior query path. (NotNull)
     */
    protected void reflectBehaviorQueryPath(Map<String, Map<String, String>> behaviorQueryPathMap) {
        final Map<File, Map<String, Map<String, String>>> reflectResourceMap = createReflectResourceMap(behaviorQueryPathMap);
        if (reflectResourceMap.isEmpty()) {
            return;
        }
        handleReflectResource(reflectResourceMap);
    }

    /**
     * @param behaviorQueryPathMap The map of behavior query path. (NotNull)
     * @return The map of table resource. (NotNull)
     * @throws DfBehaviorNotFoundException When the behavior is not found.
     */
    protected Map<File, Map<String, Map<String, String>>> createTableResourceMap(
            Map<String, Map<String, String>> behaviorQueryPathMap) {
        return doCreateBhvFileResourceMap(behaviorQueryPathMap, false);
    }

    /**
     * @param behaviorQueryPathMap The map of behavior query path. (NotNull)
     * @return The map of reflect resource. (NotNull)
     * @throws DfBehaviorNotFoundException When the behavior is not found.
     */
    protected Map<File, Map<String, Map<String, String>>> createReflectResourceMap(
            Map<String, Map<String, String>> behaviorQueryPathMap) {
        return doCreateBhvFileResourceMap(behaviorQueryPathMap, true);
    }

    /**
     * @param behaviorQueryPathMap The map of behavior query path. (NotNull)
     * @return The map of base behavior resource. (NotNull)
     * @throws DfBehaviorNotFoundException When the behavior is not found.
     */
    protected Map<File, Map<String, Map<String, String>>> doCreateBhvFileResourceMap(
            Map<String, Map<String, String>> behaviorQueryPathMap, boolean reflectOnly) {
        if (behaviorQueryPathMap.isEmpty()) {
            return new HashMap<File, Map<String, Map<String, String>>>();
        }
        final String outputDir;
        {
            String tmp = getBasicProperties().getGenerateOutputDirectory();
            if (tmp.endsWith("/")) {
                tmp = tmp.substring(0, tmp.length() - "/".length());
            }
            outputDir = tmp;
        }
        final String bsbhvPackage = getBasicProperties().getBaseBehaviorPackage();
        final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(getBasicProperties());
        packagePathHandler.setFileSeparatorSlash(true);
        final String bsbhvPathBase = outputDir + "/" + packagePathHandler.getPackageAsPath(bsbhvPackage);

        final File bsbhvDir = new File(bsbhvPathBase);
        if (!bsbhvDir.exists()) {
            _log.warn("The base behavior directory was not found: bsbhvDir=" + bsbhvDir);
            return new HashMap<File, Map<String, Map<String, String>>>();
        }
        final Map<String, File> bsbhvFileMap = createBsBhvFileMap(bsbhvDir);

        final Map<File, Map<String, Map<String, String>>> reflectResourceMap = new HashMap<File, Map<String, Map<String, String>>>();
        for (Entry<String, Map<String, String>> entry : behaviorQueryPathMap.entrySet()) {
            final Map<String, String> behaviorQueryElementMap = entry.getValue();
            final String behaviorName = behaviorQueryElementMap.get(KEY_BEHAVIOR_NAME); // on SQL file
            final String behaviorQueryPath = behaviorQueryElementMap.get(KEY_BEHAVIOR_QUERY_PATH);
            final String sqlApExp = behaviorQueryElementMap.get(KEY_SQLAP);
            if (reflectOnly && sqlApExp != null && "true".equalsIgnoreCase(sqlApExp)) {
                continue; // out of target for ApplicationOutsideSql if reflect-only
            }

            // relation point between SQL file and BsBhv
            File bsbhvFile = bsbhvFileMap.get(behaviorName);
            if (bsbhvFile == null) {
                if (isApplicationBehaviorProject()) {
                    final String projectPrefixLib = getLibraryProjectPrefix();
                    String retryName = behaviorName;
                    if (retryName.startsWith(projectPrefixLib)) { // e.g. LbFooBhv --> FooBhv
                        retryName.substring(projectPrefixLib.length());
                    }
                    final String projectPrefixAp = getBasicProperties().getProjectPrefix();
                    retryName = projectPrefixAp + retryName; // e.g. FooBhv --> BpFooBhv
                    final String additionalSuffix = getApplicationBehaviorAdditionalSuffix();
                    retryName = retryName + additionalSuffix; // e.g. BpFooBhv --> BpFooBhvAp
                    bsbhvFile = bsbhvFileMap.get(retryName);
                }
                if (bsbhvFile == null) {
                    throwBehaviorNotFoundException(bsbhvFileMap, behaviorQueryElementMap, bsbhvPathBase);
                }
            }

            Map<String, Map<String, String>> resourceElementMap = reflectResourceMap.get(bsbhvFile);
            if (resourceElementMap == null) {
                resourceElementMap = new LinkedHashMap<String, Map<String, String>>();
                reflectResourceMap.put(bsbhvFile, resourceElementMap);
            }
            if (!resourceElementMap.containsKey(behaviorQueryPath)) {
                resourceElementMap.put(behaviorQueryPath, behaviorQueryElementMap);
            }
        }
        return reflectResourceMap;
    }

    protected Map<String, File> createBsBhvFileMap(File bsbhvDir) {
        final String classFileExtension = getBasicProperties().getLanguageDependency().getLanguageGrammar()
                .getClassFileExtension();
        final FileFilter filefilter = new FileFilter() {
            public boolean accept(File file) {
                final String path = file.getPath();
                if (isApplicationBehaviorProject()) {
                    final String additionalSuffix = getApplicationBehaviorAdditionalSuffix();
                    final String bhvSuffix = "Bhv" + additionalSuffix;
                    return path.endsWith(bhvSuffix + "." + classFileExtension);
                } else {
                    return path.endsWith("Bhv." + classFileExtension);
                }
            }
        };
        final List<File> bsbhvFileList = Arrays.asList(bsbhvDir.listFiles(filefilter));
        final Map<String, File> bsbhvFileMap = new HashMap<String, File>();
        for (File bsbhvFile : bsbhvFileList) {
            String path = getSlashPath(bsbhvFile);
            path = path.substring(0, path.lastIndexOf("." + classFileExtension));
            final String bsbhvSimpleName;
            if (path.contains("/")) {
                bsbhvSimpleName = path.substring(path.lastIndexOf("/") + "/".length());
            } else {
                bsbhvSimpleName = path;
            }
            final String behaviorName = removeBasePrefix(bsbhvSimpleName);
            bsbhvFileMap.put(behaviorName, bsbhvFile);
        }
        return bsbhvFileMap;
    }

    protected void throwBehaviorNotFoundException(Map<String, File> bsbhvFileMap,
            Map<String, String> behaviorQueryElementMap, String bsbhvPathBase) {
        final String path = behaviorQueryElementMap.get(KEY_PATH);
        final String behaviorName = behaviorQueryElementMap.get(KEY_BEHAVIOR_NAME);
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The behavior was Not Found!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm the existence of the behavior." + ln();
        msg = msg + "And confirm your SQL file name." + ln();
        msg = msg + ln();
        msg = msg + "[Your SQL File]" + ln() + path + ln();
        msg = msg + ln();
        msg = msg + "[Not Found Behavior]" + ln() + behaviorName + ln();
        msg = msg + ln();
        msg = msg + "[Behavior Directory]" + ln() + bsbhvPathBase + ln();
        msg = msg + ln();
        msg = msg + "[Behavior List]" + ln() + bsbhvFileMap.keySet() + ln();
        msg = msg + "* * * * * * * * * */" + ln();
        throw new DfBehaviorNotFoundException(msg);
    }

    /**
     * @param reflectResourceMap The map of reflect resource. (NotNull)
     */
    protected void handleReflectResource(Map<File, Map<String, Map<String, String>>> reflectResourceMap) {
        final Set<Entry<File, Map<String, Map<String, String>>>> entrySet = reflectResourceMap.entrySet();
        for (Entry<File, Map<String, Map<String, String>>> entry : entrySet) {
            final File bsbhvFile = entry.getKey();
            final Map<String, Map<String, String>> resourceElementMap = entry.getValue();
            writeBehaviorQueryPath(bsbhvFile, resourceElementMap);
        }
    }

    /**
     * @param bsbhvFile The file of base behavior. (NotNull)
     * @param resourceElementMap The map of resource element. (NotNull) 
     */
    protected void writeBehaviorQueryPath(File bsbhvFile, Map<String, Map<String, String>> resourceElementMap) {
        final DfBqpBehaviorFile bqpBehaviorFile = new DfBqpBehaviorFile(bsbhvFile);
        bqpBehaviorFile.writeBehaviorQueryPath(resourceElementMap);
    }

    protected String removeBasePrefix(String bsbhvSimpleName) {
        final String projectPrefix = getBasicProperties().getProjectPrefix();
        final String basePrefix = getBasicProperties().getBasePrefix();
        final String prefix = projectPrefix + basePrefix;
        if (!bsbhvSimpleName.startsWith(prefix)) {
            return bsbhvSimpleName;
        }
        final int prefixLength = prefix.length();
        final String pureName = bsbhvSimpleName.substring(prefixLength);
        final char pureInitChar = pureName.charAt(0);
        if (pureInitChar <= 0x7F && !Character.isUpperCase(pureInitChar)) { // just in case
            return bsbhvSimpleName;
        }
        if (bsbhvSimpleName.length() <= prefixLength) { // just in case
            return bsbhvSimpleName;
        }
        return projectPrefix + pureName;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    public String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    public String getSlashPath(File file) {
        return replaceString(file.getPath(), getFileSeparator(), "/");
    }

    public String getFileSeparator() {
        return File.separator;
    }

    public String ln() {
        return "\n";
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

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
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