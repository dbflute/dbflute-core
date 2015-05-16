/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.mailflute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.exception.DfMailFluteBodyMetaParseFailureException;
import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectAssist;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectBindNode;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectProcess;
import org.dbflute.logic.sql2entity.analyzer.DfSql2EntityMark;
import org.dbflute.logic.sql2entity.analyzer.DfSql2EntityMarkAnalyzer;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfMailFluteTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String META_DELIMITER = ">>>";
    protected static final String SUBJECT_LABEL = "subject:";
    protected static final String OPTION_LABEL = "option:";
    protected static final String PLUS_HTML_OPTION = "+html";
    protected static final String PROPDEF_PREFIX = "-- !!";
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = MAIL_FLUTE
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaMailBean.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; tableMap = map:{
    //     ; targetDir = $$baseDir$$/resources/mail
    //     ; targetExt = .dfmail
    //     ; targetKeyword = 
    //     ; exceptPathList = list:{ contain:/mail/common/ }
    // }
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getTableMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));
        final String targetExt = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);

        final Map<String, Map<String, Object>> schemaMap = doLoad(targetDir, targetExt, targetKeyword, exceptPathList);
        return new DfFreeGenTable(tableMap, schemaMap);
    }

    protected Map<String, Map<String, Object>> doLoad(String targetDir, String targetExt, String targetKeyword, List<String> exceptPathList) {
        final List<File> fileList = DfCollectionUtil.newArrayList();
        collectFile(fileList, targetExt, targetKeyword, exceptPathList, new File(targetDir));
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8().removeUTF8Bom().replaceCrLfToLf();
        for (File file : fileList) {
            final Map<String, Object> table = DfCollectionUtil.newHashMap();
            final String fileName = file.getName();
            table.put("fileName", fileName);
            final String className = Srl.camelize(Srl.substringLastFront(fileName, targetExt)) + "Postcard";
            table.put("className", className); // used as output file name
            table.put("camelizedName", className);

            final String domainPath = buildDomainPath(file, targetDir);
            table.put("domainPath", domainPath); // e.g. /member/member_registration.ml

            table.put("defName", buildUpperSnakeName(domainPath));
            {
                final String dirPath = Srl.substringLastFront(domainPath, "/");
                final String snakeCase = buildPlainSnakeName(dirPath);
                final String camelizedName = Srl.camelize(snakeCase);
                table.put("camelizedDir", camelizedName);
                table.put("capCamelDir", Srl.initCap(camelizedName));
                table.put("uncapCamelDir", Srl.initUncap(camelizedName));
            }
            {
                final String snakeCase = buildPlainSnakeName(fileName);
                final String camelizedName = Srl.camelize(snakeCase);
                table.put("camelizedFile", camelizedName);
                table.put("capCamelFile", Srl.initCap(camelizedName));
                table.put("uncapCamelFile", Srl.initUncap(camelizedName));
            }
            final String fileText;
            try {
                fileText = textIO.read(new FileInputStream(file));
            } catch (FileNotFoundException e) { // no way, collected file
                throw new IllegalStateException("Not found the file: " + file, e);
            }
            checkBodyMetaFormat(file, fileText);
            final Map<String, String> propertyNameTypeMap = new LinkedHashMap<String, String>();
            final Map<String, String> propertyNameOptionMap = new LinkedHashMap<String, String>();
            final Set<String> autoDetectedPropertyNameSet = new LinkedHashSet<String>();
            processAutoDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
            processSpecifiedDetect(fileText, propertyNameTypeMap, propertyNameOptionMap);
            final List<Map<String, String>> propertyList = new ArrayList<Map<String, String>>();
            final StringBuilder commaSb = new StringBuilder();
            for (String propertyName : autoDetectedPropertyNameSet) {
                final Map<String, String> property = new LinkedHashMap<String, String>();
                property.put("propertyName", propertyName);
                property.put("capCalemName", Srl.initCap(propertyName));
                property.put("uncapCalemName", Srl.initUncap(propertyName));
                property.put("propertyType", propertyNameTypeMap.get(propertyName)); // exists
                propertyList.add(property);
                if (commaSb.length() > 0) {
                    commaSb.append(", ");
                }
                commaSb.append("\"").append(propertyName).append("\"");
            }
            table.put("propertyList", propertyList);
            table.put("propertyNameCommaString", commaSb.toString());
            schemaMap.put(fileName, table);
        }
        return schemaMap;
    }

    protected String extractTargetExt(Map<String, Object> tableMap) {
        final String targetExt = (String) tableMap.get("targetExt"); // not required
        if (targetExt != null && !targetExt.startsWith(".")) {
            return "." + targetExt;
        }
        return targetExt;
    }

    protected String extractTargetKeyword(Map<String, Object> tableMap) {
        return (String) tableMap.get("targetKeyword"); // not required
    }

    protected List<String> extractExceptPathList(Map<String, Object> tableMap) {
        @SuppressWarnings("unchecked")
        List<String> exceptPathList = (List<String>) tableMap.get("exceptPathList"); // not required
        if (exceptPathList == null) {
            exceptPathList = DfCollectionUtil.newArrayListSized(4);
        }
        exceptPathList.add("contain:.svn");
        return exceptPathList;
    }

    protected void processAutoDetect(String fileText, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> autoDetectedPropertyNameSet) {
        final DfParameterAutoDetectProcess process = new DfParameterAutoDetectProcess() {
            @Override
            protected DfParameterAutoDetectBindNode newParameterAutoDetectBindNode(DfParameterAutoDetectAssist assist) {
                return super.newParameterAutoDetectBindNode(assist).unuseTestValue("String");
            }

            @Override
            protected void processAlternateBooleanMethodIfNode(String sql, IfNode ifNode) {
                // unsupported
            }
        };
        process.processAutoDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
    }

    protected void processSpecifiedDetect(String fileText, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap) {
        final List<DfSql2EntityMark> propertyTypeList = new DfSql2EntityMarkAnalyzer().getParameterBeanPropertyTypeList(fileText);
        for (DfSql2EntityMark mark : propertyTypeList) {
            final String content = mark.getContent();
            final String propertyType = Srl.substringFirstFront(content, " ").trim();
            final String propertyName = Srl.substringFirstRear(content, " ").trim();
            propertyNameTypeMap.put(propertyName, propertyType);
        }
    }

    // ===================================================================================
    //                                                                        Collect File
    //                                                                        ============
    /**
     * @param fileList The list of saved list. (NotNull)
     * @param targetExt The extension of target path. (NullAllowed)
     * @param targetKeyword The keyword of target path. (NullAllowed)
     * @param exceptPathList The list of except path. (NotNull)
     * @param baseFile The base file. (NotNull)
     */
    protected void collectFile(final List<File> fileList, final String targetExt, final String targetKeyword,
            final List<String> exceptPathList, final File baseFile) {
        final FileHierarchyTracer tracer = new FileHierarchyTracer();
        tracer.trace(baseFile, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                if (currentFile.isDirectory()) {
                    return true;
                }
                return isCollectFile(targetExt, targetKeyword, exceptPathList, currentFile);
            }

            public void handleFile(File currentFile) {
                fileList.add(currentFile);
            }
        });
    }

    protected boolean isCollectFile(String targetExt, String targetKeyword, List<String> exceptPathList, File currentFile) {
        return !isExceptFile(exceptPathList, currentFile) && isHitByTargetExt(toPath(currentFile), targetExt, targetKeyword);
    }

    protected boolean isExceptFile(List<String> exceptPathList, File baseFile) {
        if (baseFile.isDirectory()) {
            return false;
        }
        final String baseFilePath = toPath(baseFile);
        final List<String> targetDummyList = DfCollectionUtil.emptyList();
        return !DfNameHintUtil.isTargetByHint(baseFilePath, targetDummyList, exceptPathList);
    }

    protected boolean isHitByTargetExt(String path, String targetExt, String targetKeyword) {
        final boolean validExt = Srl.is_NotNull_and_NotTrimmedEmpty(targetExt);
        final boolean validKeyword = Srl.is_NotNull_and_NotTrimmedEmpty(targetKeyword);
        final boolean isTargetByExt = validExt ? path.endsWith(targetExt) : false;
        final boolean isTargetByKeyword = validKeyword ? path.contains(targetKeyword) : false;
        final boolean result;
        if (validExt && validKeyword) { // both specified
            result = isTargetByExt || isTargetByKeyword;
        } else if (validExt) { // extension only
            result = isTargetByExt;
        } else if (validKeyword) { // keyword only
            result = isTargetByKeyword;
        } else { // both no specified
            result = true;
        }
        return result;
    }

    // ===================================================================================
    //                                                                        Check Format
    //                                                                        ============
    protected void checkBodyMetaFormat(File bodyFile, String fileText) {
        final String delimiter = META_DELIMITER;
        if (fileText.contains(delimiter)) {
            final String meta = Srl.substringFirstFront(fileText, delimiter);
            final List<String> splitList = Srl.splitList(meta, LF);
            if (!splitList.get(0).startsWith(SUBJECT_LABEL)) {
                throwMailFluteBodyMetaSubjectNotFoundException(bodyFile, fileText);
            }
            if (splitList.size() > 1) {
                final List<String> nextList = splitList.subList(1, splitList.size());
                final int nextSize = nextList.size();
                int index = 0;
                int lineNumber = 2;
                for (String line : nextList) {
                    if (index == nextSize - 1) { // last loop
                        if (line.isEmpty()) { // empty line only allowed in last loop
                            break;
                        }
                    }
                    if (!line.startsWith(OPTION_LABEL) && !line.startsWith(PROPDEF_PREFIX)) {
                        throwMailFluteBodyMetaUnknownLineException(bodyFile, fileText, line, lineNumber);
                    }
                    // option check is not here because it can be added in MailFlute
                    ++lineNumber;
                    ++index;
                }
            }
        } else { // no delimiter
            // required as generate
            throwMailFluteBodyMetaNotFoundException(bodyFile, fileText);
        }
    }

    protected void throwMailFluteBodyMetaSubjectNotFoundException(File bodyFile, String fileText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the subject in the MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The MailFlute body meta should start with subject.");
        br.addElement("For example:");
        br.addElement("  subject: ...(mail subject)");
        br.addElement("  >>>");
        br.addElement("  ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile.getPath());
        br.addItem("File Text");
        br.addElement(fileText);
        final String msg = br.buildExceptionMessage();
        throw new DfMailFluteBodyMetaParseFailureException(msg);
    }

    protected void throwMailFluteBodyMetaUnknownLineException(File bodyFile, String fileText, String line, int lineNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown line in the MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The MailFlute body meta should start with subject:");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: ...(options)");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    maihama // *NG: unknown meta definition");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("        // *NG: empty line not allowed");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile.getPath());
        br.addItem("File Text");
        br.addElement(fileText);
        br.addItem("Unknown Line");
        br.addElement("Line Number: " + lineNumber);
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new DfMailFluteBodyMetaParseFailureException(msg);
    }

    protected void throwMailFluteBodyMetaNotFoundException(File bodyFile, String fileText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the delimiter for MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The delimiter of MailFlute body meta is '>>>'.");
        br.addElement("It should be defined like this:");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: ...(options)");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    Hello, sea...");
        br.addItem("Body File");
        br.addElement(bodyFile.getPath());
        br.addItem("File Text");
        br.addElement(fileText);
        final String msg = br.buildExceptionMessage();
        throw new DfMailFluteBodyMetaParseFailureException(msg);
    }

    // ===================================================================================
    //                                                                        Build String
    //                                                                        ============
    protected String buildDomainPath(File file, String targetDir) {
        return Srl.substringFirstRear(toPath(file), targetDir);
    }

    protected String buildUpperSnakeName(String domainPath) {
        return buildPlainSnakeName(domainPath).toUpperCase();
    }

    protected String buildPlainSnakeName(String domainPath) {
        final String dlm = "_";
        String tmp = domainPath;
        tmp = replace(replace(replace(replace(tmp, ".", dlm), "-", dlm), "/", dlm), "__", dlm);
        return Srl.trim(tmp, dlm);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String toPath(File file) {
        return replace(file.getPath(), "\\", "/");
    }

    protected String replace(String str, String fromStr, String toStr) {
        return Srl.replace(str, fromStr, toStr);
    }
}
