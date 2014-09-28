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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfBqpBehaviorFile {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfBehaviorQueryPathSetupper.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final File _bsbhvFile;
    protected boolean _analyzed;
    protected String _tableKeyName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfBqpBehaviorFile(File bsbhvFile) {
        _bsbhvFile = bsbhvFile;
    }

    protected void analyze() {
        if (!_analyzed) {
            _analyzed = true;
            doAnalyze();
        }
    }

    protected void doAnalyze() {
        String tableKeyName = _bsbhvFile.getName();
        final int extIndex = tableKeyName.lastIndexOf(".");
        if (extIndex >= 0) {
            tableKeyName = tableKeyName.substring(0, extIndex);
        }

        // ApplicationBehavior resolved here
        final DfBasicProperties basicProperties = getBasicProperties();
        final String bhvSuffix = getBhvApResolvedBehaviorSuffix();
        final String projectPrefix = getBhvApResolvedProjectPrefix();

        if (tableKeyName.endsWith(bhvSuffix)) {
            tableKeyName = tableKeyName.substring(0, tableKeyName.length() - bhvSuffix.length());
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(projectPrefix) && tableKeyName.startsWith(projectPrefix)) {
            tableKeyName = tableKeyName.substring(projectPrefix.length());
        }
        final String basePrefix = basicProperties.getBasePrefix();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(basePrefix) && tableKeyName.startsWith(basePrefix)) {
            tableKeyName = tableKeyName.substring(basePrefix.length(), tableKeyName.length());
        }
        _tableKeyName = tableKeyName;
    }

    // ===================================================================================
    //                                                                            Analyzed
    //                                                                            ========
    public String getTableKeyName() {
        analyze();
        return _tableKeyName;
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    /**
     * @param resourceElementMap The map of resource element. (NotNull) 
     */
    protected void writeBehaviorQueryPath(Map<String, Map<String, String>> resourceElementMap) {
        final String encoding = getBasicProperties().getSourceFileEncoding();
        final String lineSep = getBasicProperties().getSourceCodeLineSeparator();
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguageGrammar grammar = lang.getLanguageGrammar();
        final String behaviorQueryPathBeginMark = getBasicProperties().getBehaviorQueryPathBeginMark();
        final String behaviorQueryPathEndMark = getBasicProperties().getBehaviorQueryPathEndMark();
        final DfDocumentProperties docprop = getDocumentProperties();
        final StringBuilder sb = new StringBuilder();
        String lineString = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(_bsbhvFile), encoding));
            boolean targetArea = false;
            boolean done = false;
            while (true) {
                lineString = br.readLine();
                if (lineString == null) {
                    if (targetArea) {
                        String msg = "The end mark of behavior query path was not found:";
                        msg = msg + " bsbhvFile=" + _bsbhvFile;
                        throw new IllegalStateException(msg);
                    }
                    break;
                }
                if (targetArea) {
                    if (lineString.contains(behaviorQueryPathEndMark)) {
                        targetArea = false;
                    } else {
                        continue;
                    }
                }
                sb.append(lineString).append(lineSep);
                if (!done && lineString.contains(behaviorQueryPathBeginMark)) {
                    targetArea = true;
                    final String adjustedIndent = grammar.adjustClassElementIndent("    ");
                    for (Entry<String, Map<String, String>> entry : resourceElementMap.entrySet()) {
                        final String behaviorQueryPath = entry.getKey();
                        final Map<String, String> behaviorQueryElementMap = entry.getValue();
                        final StringBuilder defSb = new StringBuilder();

                        final String keyTitle = DfBehaviorQueryPathSetupper.KEY_TITLE;
                        final String title = behaviorQueryElementMap.get(keyTitle);
                        if (title != null && title.trim().length() > 0) {
                            final String comment = buildJavaDocComment(grammar, docprop, title, adjustedIndent);
                            defSb.append(comment).append(lineSep);
                        }

                        defSb.append(adjustedIndent);
                        defSb.append(grammar.getPublicStaticFinal());
                        final String keySubDirectoryPath = DfBehaviorQueryPathSetupper.KEY_SUB_DIRECTORY_PATH;
                        final String subDirectoryPath = behaviorQueryElementMap.get(keySubDirectoryPath);
                        final String pathJavaNativeType = "String";
                        defSb.append(" ");
                        if (Srl.is_NotNull_and_NotTrimmedEmpty(subDirectoryPath)) {
                            final String subDirectoryName = Srl.replace(subDirectoryPath, "/", "_");
                            final String subDirectoryValue = Srl.replace(subDirectoryPath, "/", ":");
                            String variable = "PATH_" + subDirectoryName + "_" + behaviorQueryPath;
                            defSb.append(grammar.buildVariableSimpleDefinition(pathJavaNativeType, variable));
                            defSb.append(" = \"");
                            defSb.append(subDirectoryValue).append(":").append(behaviorQueryPath);
                            defSb.append("\";");
                        } else {
                            String variable = "PATH_" + behaviorQueryPath;
                            defSb.append(grammar.buildVariableSimpleDefinition(pathJavaNativeType, variable));
                            defSb.append(" = \"").append(behaviorQueryPath).append("\";");
                        }

                        defSb.append(lineSep);
                        sb.append(defSb);
                    }
                    done = true;
                }
            }
            if (!done) {
                _log.warn("*The mark of behavior query path was not found: " + _bsbhvFile);
            }
        } catch (IOException e) {
            String msg = "BufferedReader.readLine() threw the exception: current line=" + lineString;
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                br.close();
            } catch (IOException ignored) {
                _log.warn(ignored.getMessage());
            }
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_bsbhvFile), encoding));
            bw.write(sb.toString());
            bw.flush();
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of base behavior was not found: bsbhvFile=" + _bsbhvFile;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "BufferedWriter.write() threw the exception: bsbhvFile=" + _bsbhvFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                    _log.warn(ignored.getMessage());
                }
            }
        }
    }

    private String buildJavaDocComment(DfLanguageGrammar grammar, DfDocumentProperties docprop, String title,
            String adjustedIndent) {
        final String resolvedTitle = docprop.resolveTextForJavaDocIndentDirectly(title, adjustedIndent);
        return grammar.buildJavaDocCommentWithTitleIndentDirectly(resolvedTitle, adjustedIndent);
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