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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfCraftDiffCraftTitleNotFoundException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/20 Monday)
 */
public final class DfDocumentProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // here fixed line separator (simplified)
    protected static final String BASIC_LINE_SEPARATOR = "\n";
    protected static final String SPECIAL_LINE_SEPARATOR = "&#xa;";

    protected static final String STYLE_SHEET_EMBEDDED_MARK = "$";
    protected static final String JAVA_SCRIPT_EMBEDDED_MARK = "$";

    protected static final String SCHEMA_SYNC_CHECK_SCHEMA_XML = "./schema/project-sync-schema.xml";
    protected static final String SCHEMA_SYNC_CHECK_DIFF_MAP_FILE = "./schema/project-sync-check.diffmap";
    protected static final String SCHEMA_SYNC_CHECK_RESULT_FILE_NAME = "sync-check-result.html";

    protected static final String BASIC_CRAFT_DIFF_DIR = "./schema/craftdiff";
    protected static final String CORE_CRAFT_META_DIR = BASIC_CRAFT_DIFF_DIR; // same as basic

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDocumentProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                               documentDefinitionMap
    //                                                               =====================
    public static final String KEY_documentDefinitionMap = "documentDefinitionMap";
    protected Map<String, Object> _documentDefinitionMap;

    protected Map<String, Object> getDocumentDefinitionMap() {
        if (_documentDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_documentDefinitionMap, DEFAULT_EMPTY_MAP);
            _documentDefinitionMap = newLinkedHashMap();
            _documentDefinitionMap.putAll(map);
        }
        return _documentDefinitionMap;
    }

    // ===================================================================================
    //                                                                    Output Directory
    //                                                                    ================
    public String getDocumentOutputDirectory() {
        final String defaultValue = "./output/doc";
        return getProperty("documentOutputDirectory", defaultValue, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                                     Alias DbComment
    //                                                                     ===============
    public boolean isAliasDelimiterInDbCommentValid() {
        final String delimiter = getAliasDelimiterInDbComment();
        return delimiter != null && !delimiter.trim().equalsIgnoreCase("null");
    }

    public String getAliasDelimiterInDbComment() {
        String delimiter = (String) getDocumentDefinitionMap().get("aliasDelimiterInDbComment");
        if (delimiter != null && delimiter.trim().length() > 0) {
            // basically colon but it might be real tab and line (attention on trimming)
            return resolveControlCharacter(delimiter);
        }
        return null;
    }

    protected String resolveControlCharacter(String value) { // based on convertValueMap.dataprop's logic
        if (value == null) {
            return null;
        }
        final String tmp = "${df:temporaryVariable}";
        value = Srl.replace(value, "\\\\", tmp); // "\\" to "\" later

        // e.g. pure string "\n" to (real) line separator
        value = Srl.replace(value, "\\r", "\r");
        value = Srl.replace(value, "\\n", "\n");
        value = Srl.replace(value, "\\t", "\t");

        value = Srl.replace(value, tmp, "\\");
        return value;
    }

    public String extractAliasFromDbComment(String comment) { // alias is trimmed
        if (isAliasHandling(comment)) {
            if (hasAliasDelimiter(comment)) {
                final String delimiter = getAliasDelimiterInDbComment();
                return comment.substring(0, comment.indexOf(delimiter)).trim();
            } else {
                if (isDbCommentOnAliasBasis()) {
                    // because the comment is for alias
                    return comment != null ? comment.trim() : null;
                }
            }
        }
        // alias does not exist everywhere
        // if alias handling is not valid
        return null;
    }

    public String extractCommentFromDbComment(String comment) { // comment is trimmed
        if (isAliasHandling(comment)) {
            if (hasAliasDelimiter(comment)) {
                final String delimiter = getAliasDelimiterInDbComment();
                return comment.substring(comment.indexOf(delimiter) + delimiter.length()).trim();
            } else {
                if (isDbCommentOnAliasBasis()) {
                    // because the comment is for alias
                    return null;
                }
            }
        }
        return comment != null ? comment.trim() : null;
    }

    protected boolean isAliasHandling(String comment) {
        if (comment == null || comment.trim().length() == 0) {
            return false;
        }
        return isAliasDelimiterInDbCommentValid();
    }

    protected boolean hasAliasDelimiter(String comment) {
        final String delimiter = getAliasDelimiterInDbComment();
        return comment.contains(delimiter);
    }

    public boolean isDbCommentOnAliasBasis() {
        return isProperty("isDbCommentOnAliasBasis", false, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                            Entity JavaDoc DbComment
    //                                                            ========================
    public boolean isEntityJavaDocDbCommentValid() { // default true since 1.0.4D
        return isProperty("isEntityJavaDocDbCommentValid", true, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                             Entity DBMeta DbComment
    //                                                             =======================
    public boolean isEntityDBMetaDbCommentValid() {
        return isProperty("isEntityDBMetaDbCommentValid", false, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                                     Escape Resolver
    //                                                                     ===============
    // these are utilities (needs to refactor)
    public String resolveTextForSchemaHtml(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        text = Srl.replace(text, " ", "&nbsp;");

        // line separator
        text = removeCR(text);
        final String htmlLineSeparator = "<br />";
        if (text.contains(BASIC_LINE_SEPARATOR)) {
            text = text.replaceAll(BASIC_LINE_SEPARATOR, htmlLineSeparator);
        }
        if (text.contains(SPECIAL_LINE_SEPARATOR)) {
            text = text.replaceAll(SPECIAL_LINE_SEPARATOR, htmlLineSeparator);
        }
        return text;
    }

    public String resolveAttributeForSchemaHtml(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        text = Srl.replace(text, "\"", "&quot;");

        // line separator
        text = removeCR(text);
        return text;
    }

    public String resolvePreTextForSchemaHtml(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");

        // line separator
        text = removeCR(text);
        return text;
    }

    public String resolveTextForJavaDoc(String comment, String indent) {
        return doResolveTextForJavaDoc(comment, indent, false);
    }

    public String resolveTextForJavaDocIndentDirectly(String comment, String indent) {
        return doResolveTextForJavaDoc(comment, indent, true);
    }

    protected String doResolveTextForJavaDoc(String comment, String indent, boolean directIndent) {
        if (comment == null || comment.trim().length() == 0) {
            return null;
        }
        String work = comment;
        final DfLanguageGrammar grammar = getBasicProperties().getLanguageDependency().getLanguageGrammar();
        work = grammar.escapeJavaDocString(work);
        work = removeCR(work);
        final String sourceCodeLineSeparator = getBasicProperties().getSourceCodeLineSeparator();
        final String javaDocLineSeparator;
        if (directIndent) {
            javaDocLineSeparator = grammar.buildJavaDocLineAndIndentDirectly(sourceCodeLineSeparator, indent);
        } else {
            javaDocLineSeparator = grammar.buildJavaDocLineAndIndent(sourceCodeLineSeparator, indent);
        }
        if (work.contains(BASIC_LINE_SEPARATOR)) {
            work = work.replaceAll(BASIC_LINE_SEPARATOR, javaDocLineSeparator);
        }
        if (work.contains(SPECIAL_LINE_SEPARATOR)) {
            work = work.replaceAll(SPECIAL_LINE_SEPARATOR, javaDocLineSeparator);
        }
        return work;
    }

    public String resolveTextForSimpleLineHtml(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        // escape
        text = Srl.replace(text, "<", "&lt;");
        text = Srl.replace(text, ">", "&gt;");
        return text;
    }

    public String resolveTextForDBMeta(String text) { // C# same as Java
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        text = removeCR(text);
        text = Srl.replace(text, "\"", "\\\""); // escape double quotation

        final String literalLineSeparator = "\\\\n";
        if (text.contains(BASIC_LINE_SEPARATOR)) {
            text = text.replaceAll(BASIC_LINE_SEPARATOR, literalLineSeparator);
        }
        if (text.contains(SPECIAL_LINE_SEPARATOR)) {
            text = text.replaceAll(SPECIAL_LINE_SEPARATOR, literalLineSeparator);
        }
        return text;
    }

    // ===================================================================================
    //                                                                          SchemaHtml
    //                                                                          ==========
    public String getSchemaHtmlFileName(String projectName) {
        final String defaultName = "schema-" + projectName + ".html";
        return getProperty("schemaHtmlFileName", defaultName, getDocumentDefinitionMap());
    }

    public boolean isSuppressSchemaHtmlOutsideSql() {
        return isProperty("isSuppressSchemaHtmlOutsideSql", false, getDocumentDefinitionMap());
    }

    public boolean isSuppressSchemaHtmlProcedure() {
        return isProperty("isSuppressSchemaHtmlProcedure", false, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                           Style Sheet
    //                                           -----------
    public boolean isSchemaHtmlStyleSheetEmbedded() {
        final String styleSheet = getSchemaHtmlStyleSheet();
        return styleSheet != null && hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    public boolean isSchemaHtmlStyleSheetLink() {
        final String styleSheet = getSchemaHtmlStyleSheet();
        return styleSheet != null && !hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    protected boolean hasSchemaHtmlStyleSheetEmbeddedMark(String styleSheet) {
        return styleSheet.startsWith(STYLE_SHEET_EMBEDDED_MARK);
    }

    public String getSchemaHtmlStyleSheetEmbedded() {
        return readSchemaHtmlStyleSheetEmbedded(getSchemaHtmlStyleSheet());
    }

    protected String readSchemaHtmlStyleSheetEmbedded(String styleSheet) {
        final String purePath = Srl.substringFirstRear(styleSheet, STYLE_SHEET_EMBEDDED_MARK);
        final File cssFile = new File(purePath);
        BufferedReader br = null;
        try {
            final String encoding = getBasicProperties().getTemplateFileEncoding();
            final String separator = getBasicProperties().getSourceCodeLineSeparator();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(cssFile), encoding));
            final StringBuilder sb = new StringBuilder();
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append(separator);
            }
            return sb.toString();
        } catch (IOException e) {
            String msg = "Failed to read the CSS file: " + cssFile;
            throw new IllegalStateException(msg, e);
        }
    }

    public String getSchemaHtmlStyleSheetLink() {
        return buildSchemaHtmlStyleSheetLink(getSchemaHtmlStyleSheet());
    }

    protected String buildSchemaHtmlStyleSheetLink(String styleSheet) {
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + styleSheet + "\" />";
    }

    protected String getSchemaHtmlStyleSheet() { // closet
        return getProperty("schemaHtmlStyleSheet", null, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                            JavaScript
    //                                            ----------
    public boolean isSchemaHtmlJavaScriptEmbedded() {
        final String javaScript = getSchemaHtmlJavaScript();
        return javaScript != null && hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    public boolean isSchemaHtmlJavaScriptLink() {
        final String javaScript = getSchemaHtmlJavaScript();
        return javaScript != null && !hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    protected boolean hasSchemaHtmlJavaScriptEmbeddedMark(String javaScript) {
        return javaScript.startsWith(JAVA_SCRIPT_EMBEDDED_MARK);
    }

    public String getSchemaHtmlJavaScriptEmbedded() {
        return readSchemaHtmlJavaScriptEmbedded(getSchemaHtmlJavaScript());
    }

    protected String readSchemaHtmlJavaScriptEmbedded(String javaScript) {
        final String purePath = Srl.substringFirstRear(javaScript, JAVA_SCRIPT_EMBEDDED_MARK);
        final File cssFile = new File(purePath);
        BufferedReader br = null;
        try {
            final String encoding = getBasicProperties().getTemplateFileEncoding();
            final String separator = getBasicProperties().getSourceCodeLineSeparator();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(cssFile), encoding));
            final StringBuilder sb = new StringBuilder();
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append(separator);
            }
            return sb.toString();
        } catch (IOException e) {
            String msg = "Failed to read the CSS file: " + cssFile;
            throw new IllegalStateException(msg, e);
        }
    }

    public String getSchemaHtmlJavaScriptLink() {
        return buildSchemaHtmlJavaScriptLink(getSchemaHtmlJavaScript());
    }

    protected String buildSchemaHtmlJavaScriptLink(String javaScript) {
        return "<script type=\"text/javascript\" src=\"" + javaScript + "\"></script>";
    }

    protected String getSchemaHtmlJavaScript() { // closet
        return getProperty("schemaHtmlJavaScript", null, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                           Sister Link
    //                                           -----------
    public boolean isSuppressSchemaHtmlToSisterLink() { // closet
        return isProperty("isSuppressSchemaHtmlToSisterLink", false, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                                         HistoryHtml
    //                                                                         ===========
    public String getHistoryHtmlFileName(String projectName) {
        final String defaultName = "history-" + projectName + ".html";
        return getProperty("historyHtmlFileName", defaultName, getDocumentDefinitionMap());
    }

    // options below are related to HistoryHTML, SchemaSyncCheck, AlterCheck, ...

    public boolean isCheckColumnDefOrderDiff() {
        return isProperty("isCheckColumnDefOrderDiff", false, getDocumentDefinitionMap());
    }

    public boolean isCheckDbCommentDiff() {
        return isProperty("isCheckDbCommentDiff", false, getDocumentDefinitionMap());
    }

    public boolean isCheckProcedureDiff() {
        return isProperty("isCheckProcedureDiff", false, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                             CraftDiff
    //                                             ---------
    public boolean isCheckCraftDiff() { // closet
        return isProperty("isCheckCraftDiff", true, getDocumentDefinitionMap());
    }

    public List<File> getCraftSqlFileList() {
        if (!isCheckCraftDiff()) {
            return DfCollectionUtil.emptyList();
        }
        final String targetDir = getBasicCraftSqlDir();
        return findSchemaResourceFileList(targetDir, "craft-schema", ".sql");
    }

    protected String getBasicCraftSqlDir() { // closet
        if (!isCheckCraftDiff()) {
            return null;
        }
        return getProperty("basicCraftSqlDir", BASIC_CRAFT_DIFF_DIR, getDocumentDefinitionMap());
    }

    public String getCoreCraftMetaDir() {
        if (!isCheckCraftDiff()) {
            return null;
        }
        final String defaultDir = CORE_CRAFT_META_DIR;
        final String property = getProperty("coreCraftMetaDirPath", defaultDir, getDocumentDefinitionMap());
        return Srl.replace(property, "$$DEFAULT$$", defaultDir);
    }

    protected String getCraftMetaFilePrefix() {
        return "craft-meta";
    }

    protected String getCraftMetaFileExt() {
        return ".tsv";
    }

    public List<File> getCraftMetaFileList(String craftMetaDir) {
        if (!isCheckCraftDiff()) {
            return DfCollectionUtil.emptyList();
        }
        final String prefix = getCraftMetaFilePrefix();
        final String ext = getCraftMetaFileExt();
        return findSchemaResourceFileList(craftMetaDir, prefix, ext);
    }

    public String buildCraftMetaFileName(String craftTitle, boolean next) {
        final String prefix = getCraftMetaFilePrefix();
        final String ext = getCraftMetaFileExt();
        return prefix + "-" + craftTitle + "-" + (next ? "next" : "previous") + ext;
    }

    public String extractCraftTitle(File metaFile) {
        final String resourceName = extractCraftResourceNameFromMetaFile(metaFile);
        return Srl.substringLastFront(resourceName, "-");
    }

    public boolean isCraftDirectionNext(File metaFile) {
        final String resourceName = extractCraftResourceNameFromMetaFile(metaFile);
        final String direction = Srl.substringLastRear(resourceName, "-");
        if ("next".equals(direction)) {
            return true;
        } else if ("previous".equals(direction)) {
            return false;
        } else {
            throwCraftDiffIllegalCraftMetaFileNameException(metaFile, "Craft Direction", direction);
            return false; // unreachable
        }
    }

    protected String extractCraftResourceNameFromMetaFile(File metaFile) {
        // craft-meta-Trigger-next.tsv -> Trigger
        final String name = metaFile.getName();
        final String prefix = getCraftMetaFilePrefix();
        final String ext = getCraftMetaFileExt();
        final String resourceName = Srl.extractScopeWide(name, prefix, ext).getContent();
        if (!resourceName.contains("-")) {
            throwCraftDiffIllegalCraftMetaFileNameException(metaFile, "Resource Name", resourceName);
        }
        return resourceName.startsWith("-") ? Srl.substringFirstRear(resourceName, "-") : resourceName;
    }

    protected void throwCraftDiffIllegalCraftMetaFileNameException(File metaFile, String attrName, String attrValue) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal craft direction was found in the file name.");
        br.addItem("Advice");
        br.addElement("The file name of craft meta should be named like this:");
        br.addElement("  craft-meta-[craft-title]-[next-or-previous].tsv");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  (o): craft-meta-Trigger-next.tsv");
        br.addElement("  (o): craft-meta-Trigger-previous.tsv");
        br.addElement("  (x): craft-meta-Trigger-foo.tsv");
        br.addElement("  (x): craft-meta-Trigger.tsv");
        br.addItem("Meta File");
        br.addElement(metaFile.getPath());
        br.addItem(attrName);
        br.addElement(attrValue);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffCraftTitleNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                           Style Sheet
    //                                           -----------
    public boolean isHistoryHtmlStyleSheetEmbedded() {
        final String styleSheet = getHistoryHtmlStyleSheet();
        return styleSheet != null && hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    public boolean isHistoryHtmlStyleSheetLink() {
        final String styleSheet = getHistoryHtmlStyleSheet();
        return styleSheet != null && !hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    public String getHistoryHtmlStyleSheetEmbedded() {
        return readSchemaHtmlStyleSheetEmbedded(getHistoryHtmlStyleSheet());
    }

    public String getHistoryHtmlStyleSheetLink() {
        return buildSchemaHtmlStyleSheetLink(getHistoryHtmlStyleSheet());
    }

    protected String getHistoryHtmlStyleSheet() { // closet
        return getProperty("historyHtmlStyleSheet", null, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                            JavaScript
    //                                            ----------
    public boolean isHistoryHtmlJavaScriptEmbedded() {
        final String javaScript = getHistoryHtmlJavaScript();
        return javaScript != null && hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    public boolean isHistoryHtmlJavaScriptLink() {
        final String javaScript = getHistoryHtmlJavaScript();
        return javaScript != null && !hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    public String getHistoryHtmlJavaScriptEmbedded() {
        return readSchemaHtmlJavaScriptEmbedded(getHistoryHtmlJavaScript());
    }

    public String getHistoryHtmlJavaScriptLink() {
        return buildSchemaHtmlJavaScriptLink(getHistoryHtmlJavaScript());
    }

    protected String getHistoryHtmlJavaScript() { // closet
        return getProperty("historyHtmlJavaScript", null, getDocumentDefinitionMap());
    }

    // -----------------------------------------------------
    //                                           Sister Link
    //                                           -----------
    public boolean isSuppressHistoryHtmlToSisterLink() { // closet
        return isProperty("isSuppressHistoryHtmlToSisterLink", false, getDocumentDefinitionMap());
    }

    // ===================================================================================
    //                                                                     LoadDataReverse
    //                                                                     ===============
    protected Map<String, Object> _loadDataReverseMap;

    protected Map<String, Object> getLoadDataReverseMap() {
        if (_loadDataReverseMap != null) {
            return _loadDataReverseMap;
        }
        final String key = "loadDataReverseMap";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) getDocumentDefinitionMap().get(key);
        if (map != null) {
            _loadDataReverseMap = map;
        } else {
            _loadDataReverseMap = DfCollectionUtil.emptyMap();
        }
        return _loadDataReverseMap;
    }

    public boolean isLoadDataReverseValid() {
        return getLoadDataReverseRecordLimit() != null;
    }

    // -----------------------------------------------------
    //                                         File Resource
    //                                         -------------
    public String getLoadDataReverseXlsDataDir() {
        if (isLoadDataReverseReplaceSchemaDirectUse()) {
            return getReplaceSchemaProperties().getMainCurrentLoadTypeReverseXlsDataDir();
        } else {
            final String outputDirectory = getDocumentOutputDirectory();
            return outputDirectory + "/data";
        }
    }

    public String getLoadDataReverseDelimiterDataDir() { // for big data
        if (isLoadDataReverseReplaceSchemaDirectUse()) {
            return getReplaceSchemaProperties().getMainCurrentLoadTypeReverseTsvUTF8DataDir();
        } else {
            final String templateDir = getLoadDataReverseXlsDataDir();
            return templateDir + "/big-data";
        }
    }

    public String getLoadDataReverseFileTitle() {
        return isLoadDataReverseReplaceSchemaDirectUse() ? "cyclic-data" : "reverse-data";
    }

    public String getLoadDataReverseSchemaXml() {
        final String projectName = getBasicProperties().getProjectName();
        return "./schema/lreverse-schema-" + projectName + ".xml";
    }

    // -----------------------------------------------------
    //                                          Record Limit
    //                                          ------------
    public Integer getLoadDataReverseRecordLimit() {
        final Map<String, Object> loadDataReverseMap = getLoadDataReverseMap();
        String limitExp = null;
        if (!loadDataReverseMap.isEmpty()) {
            limitExp = (String) loadDataReverseMap.get("recordLimit");
        }
        if (limitExp == null) {
            return null;
        }
        try {
            return Integer.valueOf(limitExp);
        } catch (NumberFormatException e) {
            String msg = "The property 'recordLimit' of loadDataReverse in " + KEY_documentDefinitionMap;
            msg = msg + " should be number but: value=" + limitExp;
            throw new DfIllegalPropertyTypeException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    public boolean isLoadDataReverseReplaceSchemaDirectUse() {
        final boolean defaultValue = isLoadDataReverseOutputToPlaySql(); // for compatible
        return isProperty("isReplaceSchemaDirectUse", defaultValue, getLoadDataReverseMap());
    }

    public boolean isLoadDataReverseOverrideExistingDataFile() {
        return isProperty("isOverrideExistingDataFile", false, getLoadDataReverseMap());
    }

    public boolean isLoadDataReverseSynchronizeOriginDate() {
        return isProperty("isSynchronizeOriginDate", false, getLoadDataReverseMap());
    }

    protected boolean isLoadDataReverseOutputToPlaySql() { // old style
        return isProperty("isOutputToPlaySql", false, getLoadDataReverseMap());
    }

    public boolean isLoadDataReverseContainsCommonColumn() { // closet
        // long long time ago, default was false but common column value also should be reversed...
        return isProperty("isContainsCommonColumn", true, getLoadDataReverseMap());
    }

    // -----------------------------------------------------
    //                                             XLS Limit
    //                                             ---------
    public Integer getLoadDataReverseXlsLimit() {
        final Map<String, Object> loadDataReverseMap = getLoadDataReverseMap();
        String limitExp = null;
        if (!loadDataReverseMap.isEmpty()) {
            limitExp = (String) loadDataReverseMap.get("xlsLimit");
        }
        if (limitExp == null) {
            return null; // if null, default limit
        }
        try {
            return Integer.valueOf(limitExp);
        } catch (NumberFormatException e) {
            String msg = "The property 'xlsLimit' of loadDataReverse in " + KEY_documentDefinitionMap;
            msg = msg + " should be number but: value=" + limitExp;
            throw new DfIllegalPropertyTypeException(msg, e);
        }
    }

    public boolean isLoadDataReverseSuppressLargeDataHandling() {
        return isProperty("isSuppressLargeDataHandling", false, getLoadDataReverseMap());
    }

    public boolean isLoadDataReverseSuppressQuoteEmptyString() {
        return isProperty("isSuppressQuoteEmptyString", false, getLoadDataReverseMap());
    }

    public Integer getLoadDataReverseCellLengthLimit() {
        final Map<String, Object> loadDataReverseMap = getLoadDataReverseMap();
        String limitExp = null;
        if (!loadDataReverseMap.isEmpty()) {
            limitExp = (String) loadDataReverseMap.get("cellLengthLimit");
        }
        if (limitExp == null) {
            return null; // if null, default limit
        }
        try {
            return Integer.valueOf(limitExp);
        } catch (NumberFormatException e) {
            String msg = "The property 'cellLengthLimit' of loadDataReverse in " + KEY_documentDefinitionMap;
            msg = msg + " should be number but: value=" + limitExp;
            throw new DfIllegalPropertyTypeException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                     Table Except List
    //                                     -----------------
    protected List<String> _tableExceptList;

    @SuppressWarnings("unchecked")
    protected List<String> getLoadDataReverseTableExceptList() { // for main schema
        if (_tableExceptList != null) {
            return _tableExceptList;
        }
        final Object tableExceptObj = getLoadDataReverseMap().get("tableExceptList");
        if (tableExceptObj != null && !(tableExceptObj instanceof List<?>)) {
            String msg = "loadDataReverseMap.tableExceptList should be list: " + tableExceptObj;
            throw new DfIllegalPropertyTypeException(msg);
        }
        final List<String> tableExceptList;
        if (tableExceptObj != null) {
            tableExceptList = (List<String>) tableExceptObj;
        } else {
            tableExceptList = DfCollectionUtil.emptyList();
        }
        _tableExceptList = tableExceptList;
        return _tableExceptList;
    }

    public boolean isLoadDataReverseTableTarget(String name) {
        final List<String> targetList = DfCollectionUtil.emptyList();
        return isTargetByHint(name, targetList, getLoadDataReverseTableExceptList());
    }

    public boolean isTargetByHint(String name, List<String> targetList, List<String> exceptList) {
        return DfNameHintUtil.isTargetByHint(name, targetList, exceptList);
    }

    // ===================================================================================
    //                                                                     SchemaSyncCheck
    //                                                                     ===============
    protected Map<String, String> _schemaSyncCheckMap;

    protected Map<String, String> getSchemaSyncCheckMap() {
        if (_schemaSyncCheckMap != null) {
            return _schemaSyncCheckMap;
        }
        final String key = "schemaSyncCheckMap";
        @SuppressWarnings("unchecked")
        final Map<String, String> map = (Map<String, String>) getDocumentDefinitionMap().get(key);
        if (map != null) {
            _schemaSyncCheckMap = map;
        } else {
            _schemaSyncCheckMap = DfCollectionUtil.emptyMap();
        }
        return _schemaSyncCheckMap;
    }

    public boolean isSchemaSyncCheckValid() {
        return getSchemaSyncCheckDatabaseUser() != null;
    }

    public String getSchemaSyncCheckDatabaseUrl() {
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String url = schemaSyncCheckMap.get("url");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(url)) {
            // dispatch variable is supported
            // but connecting after semicolon is unsupported for now
            // (rare case especially here, and also we can already use escape)
            final String propTitle = "documentDefinitionMap#schemaSyncCheckMap$url";
            return resolveDispatchVariable(propTitle, url);
        } else {
            return getDatabaseProperties().getDatabaseUrl();
        }
    }

    public String getSchemaSyncCheckDatabaseCatalog() {
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String catalog = schemaSyncCheckMap.get("catalog");
        final String url = getSchemaSyncCheckDatabaseUrl();
        return getDatabaseProperties().prepareMainCatalog(catalog, url);
    }

    public UnifiedSchema getSchemaSyncCheckDatabaseSchema() {
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String schema = schemaSyncCheckMap.get("schema");
        final String catalog = getSchemaSyncCheckDatabaseCatalog();
        return getDatabaseProperties().prepareMainUnifiedSchema(catalog, schema);
    }

    public String getSchemaSyncCheckDatabaseUser() {
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String propTitle = "documentDefinitionMap#schemaSyncCheckMap$url";
        return resolveDispatchVariable(propTitle, schemaSyncCheckMap.get("user"));
    }

    public String getSchemaSyncCheckDatabasePassword() {
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String propTitle = "documentDefinitionMap#schemaSyncCheckMap$password";
        final String user = getSchemaSyncCheckDatabaseUser();
        return resolvePasswordVariable(propTitle, user, schemaSyncCheckMap.get("password"));
    }

    public String getSchemaSyncCheckSchemaXml() {
        return SCHEMA_SYNC_CHECK_SCHEMA_XML;
    }

    public String getSchemaSyncCheckDiffMapFile() {
        return SCHEMA_SYNC_CHECK_DIFF_MAP_FILE;
    }

    public boolean isSchemaSyncCheckSuppressCraftDiff() { // closet
        return isProperty("isSuppressCraftDiff", false, getSchemaSyncCheckMap());
    }

    public String getSchemaSyncCheckResultFileName() { // closet
        final Map<String, String> schemaSyncCheckMap = getSchemaSyncCheckMap();
        final String fileName = schemaSyncCheckMap.get("resultHtmlFileName");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(fileName)) {
            return fileName;
        }
        return SCHEMA_SYNC_CHECK_RESULT_FILE_NAME;
    }

    public String getSchemaSyncCheckResultFilePath() {
        final String outputDirectory = getDocumentOutputDirectory();
        return outputDirectory + "/" + getSchemaSyncCheckResultFileName();
    }

    public String getSchemaSyncCheckCraftMetaDir() { // closet
        if (!isCheckCraftDiff()) {
            return null;
        }
        final String defaultDir = getDocumentOutputDirectory() + "/craftdiff";
        final String property = getProperty("craftMetaDirPath", defaultDir, getSchemaSyncCheckMap());
        return Srl.replace(property, "$$DEFAULT$$", defaultDir);
    }

    // ===================================================================================
    //                                                              Table Display Order By
    //                                                              ======================
    public Comparator<Table> getTableDisplayOrderBy() {
        return new Comparator<Table>() {
            public int compare(Table table1, Table table2) {
                // = = = =
                // Schema
                // = = = =
                // The main schema has priority
                {
                    final boolean mainSchema1 = table1.isMainSchema();
                    final boolean mainSchema2 = table2.isMainSchema();
                    if (mainSchema1 != mainSchema2) {
                        if (mainSchema1) {
                            return -1;
                        }
                        if (mainSchema2) {
                            return 1;
                        }
                        // unreachable
                    }
                    final String schema1 = table1.getDocumentSchema();
                    final String schema2 = table2.getDocumentSchema();
                    if (schema1 != null && schema2 != null && !schema1.equals(schema2)) {
                        return schema1.compareTo(schema2);
                    } else if (schema1 == null && schema2 != null) {
                        return 1; // nulls last
                    } else if (schema1 != null && schema2 == null) {
                        return -1; // nulls last
                    }
                    // passed: when both are NOT main and are same schema
                }

                // = = =
                // Type
                // = = =
                {
                    final String type1 = table1.getType();
                    final String type2 = table2.getType();
                    if (!type1.equals(type2)) {
                        // The table type has priority
                        if (table1.isTypeTable()) {
                            return -1;
                        }
                        if (table2.isTypeTable()) {
                            return 1;
                        }
                        return type1.compareTo(type2);
                    }
                }

                // = = =
                // Table
                // = = =
                final String name1 = table1.getName();
                final String name2 = table2.getName();
                return name1.compareTo(name2);
            }
        };
    }

    // ===================================================================================
    //                                                                      PropertiesHtml
    //                                                                      ==============
    // ; propertiesHtmlMap = map:{
    //     ; df:header = map:{
    //         ; title = Properties Overview
    //     }
    //     ; ApplicationProp = map:{
    //         ; baseDir = ../src
    //         ; rootFile = $$baseDir$$/main/resources/application_ja.properties
    //         ; environmentMap = map:{
    //             ; production = $$baseDir$$/production/resources
    //             ; integration = $$baseDir$$/integration/resources
    //         }
    //         ; diffIgnoredKeyList = list:{ errors.ignored.key }
    //         ; maskedKeyList = list:{ errors.masked.key }
    //         ; extendsPropRequest = CommonApplicationProp
    //         ; isCheckImplicitOverride = false
    //     }
    // }
    protected Map<String, Object> _propertiesHtmlHeaderMap;
    protected Map<String, Map<String, Object>> _propertiesHtmlMap;

    public Map<String, Map<String, Object>> getPropertiesHtmlMap() {
        if (_propertiesHtmlMap != null) {
            return _propertiesHtmlMap;
        }
        final String key = "propertiesHtmlMap";
        final Map<String, Object> definitionMap = getDocumentDefinitionMap();
        @SuppressWarnings("unchecked")
        final Map<String, Object> propertiesHtmlMap = (Map<String, Object>) definitionMap.get(key);
        if (propertiesHtmlMap != null) {
            _propertiesHtmlMap = resolvePropertiesHtmlMap(propertiesHtmlMap);
        } else {
            _propertiesHtmlMap = DfCollectionUtil.emptyMap();
        }
        return _propertiesHtmlMap;
    }

    protected Map<String, Map<String, Object>> resolvePropertiesHtmlMap(Map<String, Object> propertiesHtmlMap) {
        final String baseDirKey = "baseDir";
        final String baseDirVariable = "$$" + baseDirKey + "$$";
        final String rootFileKey = "rootFile";
        final String envMapKey = "environmentMap";
        final Map<String, Map<String, Object>> resolvedMap = newLinkedHashMap();
        for (Entry<String, Object> requestEntry : propertiesHtmlMap.entrySet()) {
            final String requestName = requestEntry.getKey();
            @SuppressWarnings("unchecked")
            final Map<String, Object> requestMap = (Map<String, Object>) requestEntry.getValue();
            if (requestName.equals("df:header")) {
                _propertiesHtmlHeaderMap = requestMap;
                continue;
            }
            final String baseDir = (String) requestMap.get(baseDirKey);
            if (baseDir != null) {
                final Map<String, Object> filteredRequestMap = newLinkedHashMap();
                filteredRequestMap.put(baseDirKey, baseDir);
                for (Entry<String, Object> elementEntry : requestMap.entrySet()) {
                    final String elementKey = elementEntry.getKey();
                    final Object elementValue = elementEntry.getValue();
                    if (rootFileKey.equals(elementKey)) {
                        final String replaced = Srl.replace((String) elementValue, baseDirVariable, baseDir);
                        filteredRequestMap.put(elementKey, replaced);
                    } else if (envMapKey.equals(elementKey)) {
                        final Map<String, String> filteredEnvMap = newLinkedHashMap();
                        @SuppressWarnings("unchecked")
                        final Map<String, String> environmentMap = (Map<String, String>) elementValue;
                        for (Entry<String, String> envEntry : environmentMap.entrySet()) {
                            final String envName = envEntry.getKey();
                            final String envValue = envEntry.getValue();
                            final String replaced = Srl.replace(envValue, baseDirVariable, baseDir);
                            filteredEnvMap.put(envName, replaced);
                        }
                        filteredRequestMap.put(elementKey, filteredEnvMap);
                    } else {
                        filteredRequestMap.put(elementKey, elementValue);
                    }
                }
                resolvedMap.put(requestName, filteredRequestMap);
            } else {
                resolvedMap.put(requestName, requestMap);
            }
        }
        if (_propertiesHtmlHeaderMap == null) {
            _propertiesHtmlHeaderMap = DfCollectionUtil.emptyMap();
        }
        return resolvedMap;
    }

    // -----------------------------------------------------
    //                                                Header
    //                                                ------
    protected Map<String, Object> getPropertiesHtmlHeaderMap() {
        getPropertiesHtmlMap(); // initialize
        return _propertiesHtmlHeaderMap;
    }

    public String getPropertiesHtmlHeaderTitle() {
        final String title = (String) getPropertiesHtmlHeaderMap().get("title");
        return title != null ? title : null;
    }

    protected String getPropertiesHtmlHeaderHtmlFileName() {
        final String fileName = (String) getPropertiesHtmlHeaderMap().get("htmlFileName");
        return fileName != null ? fileName : null;
    }

    protected String getPropertiesHtmlHeaderStyleSheet() {
        final String sheet = (String) getPropertiesHtmlHeaderMap().get("styleSheet");
        return sheet != null ? sheet : null;
    }

    protected String getPropertiesHtmlHeaderJavaScript() {
        final String js = (String) getPropertiesHtmlHeaderMap().get("javaScript");
        return js != null ? js : null;
    }

    public boolean isSuppressPropertiesHtmlToSisterLink() { // closet
        return isProperty("isSuppressToSisterLink", false, getPropertiesHtmlHeaderMap());
    }

    // -----------------------------------------------------
    //                                               Request
    //                                               -------
    public String getPropertiesHtmlRootFile(Map<String, Object> requestMap) {
        return (String) requestMap.get("rootFile");
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getPropertiesHtmlEnvironmentMap(Map<String, Object> requestMap) {
        final Map<String, String> environmentMap = (Map<String, String>) requestMap.get("environmentMap");
        if (environmentMap != null) {
            return environmentMap;
        }
        return DfCollectionUtil.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPropertiesHtmlDiffIgnoredKeyList(Map<String, Object> requestMap) {
        final List<String> ignoredKeyList = (List<String>) requestMap.get("diffIgnoredKeyList");
        if (ignoredKeyList != null) {
            return ignoredKeyList;
        }
        return DfCollectionUtil.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPropertiesHtmlMaskedKeyList(Map<String, Object> requestMap) {
        final List<String> maskedKeyList = (List<String>) requestMap.get("maskedKeyList");
        if (maskedKeyList != null) {
            return maskedKeyList;
        }
        return DfCollectionUtil.emptyList();
    }

    public boolean isPropertiesHtmlEnvOnlyFloatLeft(Map<String, Object> requestMap) {
        return isProperty("isEnvOnlyFloatLeft", false, requestMap);
    }

    public String getPropertiesHtmlExtendsPropRequest(Map<String, Object> requestMap) {
        return (String) requestMap.get("extendsPropRequest");
    }

    public boolean isPropertiesHtmlCheckImplicitOverride(Map<String, Object> requestMap) {
        return isProperty("isCheckImplicitOverride", false, requestMap);
    }

    // -----------------------------------------------------
    //                                             File Name
    //                                             ---------
    public String getPropertiesHtmlFileName(String projectName) { // closet
        final String defaultName = "properties-" + projectName + ".html";
        final String htmlFileName = getPropertiesHtmlHeaderHtmlFileName();
        return Srl.is_NotNull_and_NotTrimmedEmpty(htmlFileName) ? htmlFileName : defaultName;
    }

    // -----------------------------------------------------
    //                                           Style Sheet
    //                                           -----------
    public boolean isPropertiesHtmlStyleSheetEmbedded() {
        final String styleSheet = getPropertiesHtmlStyleSheet();
        return styleSheet != null && hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    public boolean isPropertiesHtmlStyleSheetLink() {
        final String styleSheet = getPropertiesHtmlStyleSheet();
        return styleSheet != null && !hasSchemaHtmlStyleSheetEmbeddedMark(styleSheet);
    }

    public String getPropertiesHtmlStyleSheetEmbedded() {
        return readSchemaHtmlStyleSheetEmbedded(getSchemaHtmlStyleSheet());
    }

    public String getPropertiesHtmlStyleSheetLink() {
        return buildSchemaHtmlStyleSheetLink(getSchemaHtmlStyleSheet());
    }

    protected String getPropertiesHtmlStyleSheet() { // closet
        return getPropertiesHtmlHeaderStyleSheet();
    }

    // -----------------------------------------------------
    //                                            JavaScript
    //                                            ----------
    public boolean isPropertiesHtmlJavaScriptEmbedded() {
        final String javaScript = getPropertiesHtmlJavaScript();
        return javaScript != null && hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    public boolean isPropertiesHtmlJavaScriptLink() {
        final String javaScript = getPropertiesHtmlJavaScript();
        return javaScript != null && !hasSchemaHtmlJavaScriptEmbeddedMark(javaScript);
    }

    public String getPropertiesHtmlJavaScriptEmbedded() {
        return readSchemaHtmlJavaScriptEmbedded(getPropertiesHtmlJavaScript());
    }

    public String getPropertiesHtmlJavaScriptLink() {
        return buildSchemaHtmlJavaScriptLink(getSchemaHtmlJavaScript());
    }

    protected String getPropertiesHtmlJavaScript() { // closet
        return getPropertiesHtmlHeaderJavaScript();
    }
}