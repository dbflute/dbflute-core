/*
 * Copyright 2014-2017 the original author or authors.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectAssist;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectBindNode;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectProcess;
import org.dbflute.logic.sql2entity.analyzer.DfSql2EntityMark;
import org.dbflute.logic.sql2entity.analyzer.DfSql2EntityMarkAnalyzer;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 */
public class DfMailFluteTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // *very similar logic also exists on MailFlute
    public static final String META_DELIMITER = ">>>";
    public static final String COMMENT_BEGIN = "/*";
    public static final String COMMENT_END = "*/";
    public static final String TITLE_BEGIN = "[";
    public static final String TITLE_END = "]";
    public static final String SUBJECT_LABEL = "subject:";
    public static final String OPTION_LABEL = "option:";
    public static final String PLUS_HTML_OPTION = "+html";
    public static final String PROPDEF_PREFIX = "-- !!";
    // option check is not here because it can be added in MailFlute
    //public static final Set<String> optionSet;
    //static {
    //    optionSet = Collections.unmodifiableSet(DfCollectionUtil.newLinkedHashSet(PLUS_HTML_OPTION));
    //}
    public static final List<String> allowedPrefixList; // except first line (comment)
    static {
        allowedPrefixList = Arrays.asList(OPTION_LABEL, PROPDEF_PREFIX);
    }
    protected static final String LF = "\n";
    protected static final String CR = "\r";
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
    //     ; isConventionSuffix = false
    // }
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));
        final String targetExt = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);

        final Map<String, Map<String, Object>> schemaMap = doLoad(targetDir, targetExt, targetKeyword, exceptPathList, tableMap);
        return new DfFreeGenMetaData(tableMap, schemaMap);
    }

    protected Map<String, Map<String, Object>> doLoad(String targetDir, String targetExt, String targetKeyword,
            List<String> exceptPathList, Map<String, Object> tableMap) {
        final List<File> fileList = DfCollectionUtil.newArrayList();
        final File baseDir = new File(targetDir);
        collectFile(fileList, targetExt, targetKeyword, exceptPathList, baseDir);
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8().removeUTF8Bom().replaceCrLfToLf();
        for (File bodyFile : fileList) {
            final Map<String, Object> table = DfCollectionUtil.newHashMap();
            final String fileName = bodyFile.getName();
            table.put("fileName", fileName);
            final String className = Srl.camelize(Srl.substringLastFront(fileName, targetExt)) + "Postcard";
            table.put("className", className); // used as output file name
            table.put("camelizedName", className);
            final String addedPkg = deriveAdditionalPackage(tableMap, baseDir, bodyFile);
            if (Srl.is_NotNull_and_NotEmpty(addedPkg)) {
                table.put("additionalPackage", addedPkg);
            }

            final String domainPath = buildDomainPath(bodyFile, targetDir);
            table.put("domainPath", domainPath); // e.g. /member/member_registration.dfmail
            table.put("resourcePath", Srl.ltrim(domainPath, "/")); // e.g. member/member_registration.dfmail

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
            final String plainText = readText(textIO, toPath(bodyFile));
            final String delimiter = META_DELIMITER;
            if (!plainText.contains(delimiter)) {
                throwBodyMetaNotFoundException(toPath(bodyFile), plainText);
            }
            verifyFormat(toPath(bodyFile), plainText, delimiter);
            final String bodyMeta = Srl.substringFirstFront(plainText, delimiter);
            final boolean hasOptionPlusHtml = hasOptionPlusHtml(bodyMeta, delimiter);
            final String htmlFilePath = deriveHtmlFilePath(toPath(bodyFile));
            if (new File(htmlFilePath).exists()) {
                if (!hasOptionPlusHtml) {
                    throwNoPlusHtmlButHtmlTemplateExistsException(toPath(bodyFile), htmlFilePath, bodyMeta);
                }
                verifyMailHtmlTemplateTextFormat(htmlFilePath, readText(textIO, htmlFilePath));
            } else {
                if (hasOptionPlusHtml) {
                    throwNoHtmlTemplateButPlusHtmlExistsException(toPath(bodyFile), htmlFilePath, bodyMeta);
                }
            }

            final Map<String, String> propertyNameTypeMap = new LinkedHashMap<String, String>();
            final Map<String, String> propertyNameOptionMap = new LinkedHashMap<String, String>();
            final Set<String> propertyNameSet = new LinkedHashSet<String>();
            processAutoDetect(plainText, propertyNameTypeMap, propertyNameOptionMap, propertyNameSet);
            processSpecifiedDetect(plainText, propertyNameTypeMap, propertyNameOptionMap, propertyNameSet);
            final List<Map<String, String>> propertyList = new ArrayList<Map<String, String>>();
            final StringBuilder commaSb = new StringBuilder();
            for (String propertyName : propertyNameSet) {
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

    // -----------------------------------------------------
    //                                      Extract Resource
    //                                      ----------------
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

    // -----------------------------------------------------
    //                                    Additional Package
    //                                    ------------------
    protected String deriveAdditionalPackage(Map<String, Object> tableMap, File baseDir, File pmFile) {
        if (((String) tableMap.getOrDefault("isConventionSuffix", "true")).equalsIgnoreCase("true")) {
            final String baseCano;
            try {
                baseCano = toPath(baseDir.getCanonicalPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to get canonical path: " + pmFile, e);
            }
            final String currentCano;
            try {
                currentCano = toPath(pmFile.getCanonicalPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to get canonical path: " + pmFile, e);
            }
            String pkg = null;
            if (currentCano.startsWith(baseCano)) {
                final String relativePath = Srl.ltrim(Srl.substringFirstRear(currentCano, baseCano), "/");
                if (relativePath.contains("/")) {
                    pkg = Srl.substringFirstFront(relativePath, "/").toLowerCase();
                }
            }
            return pkg;
        } else {
            return null;
        }
    }

    // -----------------------------------------------------
    //                                            AutoDetect
    //                                            ----------
    protected void processAutoDetect(String fileText, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> propertyNameSet) {
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
        process.processAutoDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, propertyNameSet);
    }

    protected void processSpecifiedDetect(String fileText, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, Set<String> propertyNameSet) {
        final List<DfSql2EntityMark> propertyTypeList = new DfSql2EntityMarkAnalyzer().getParameterBeanPropertyTypeList(fileText);
        for (DfSql2EntityMark mark : propertyTypeList) {
            final String content = mark.getContent();
            final String propertyType = Srl.substringFirstFront(content, " ").trim();
            final String propertyName = Srl.substringFirstRear(content, " ").trim();
            propertyNameTypeMap.put(propertyName, resolvePackageName(propertyType));
            propertyNameSet.add(propertyName);
        }
    }

    protected String resolvePackageName(String typeName) {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguagePropertyPackageResolver resolver = lang.getLanguagePropertyPackageResolver();
        return resolver.resolvePackageName(typeName);
    }

    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
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

    protected boolean isExceptFile(List<String> exceptPathList, File currentFile) {
        if (currentFile.isDirectory()) {
            return false;
        }
        final String fileName = currentFile.getName();
        if (Srl.count(fileName, ".") > 1) { // e.g. sea.ja.dfmail
            return true; // locale file
        }
        if (fileName.contains(".") && Srl.substringFirstFront(fileName, ".").endsWith("_html")) { // e.g. sea_html.dfmail
            return true; // html file
        }
        final String baseFilePath = toPath(currentFile);
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

    // *very similar logic also exists on MailFlute
    // ===================================================================================
    //                                                                       Verify Format
    //                                                                       =============
    protected void verifyFormat(String bodyFile, String plainText, String delimiter) {
        final String meta = Srl.substringFirstFront(plainText, delimiter);
        if (!meta.endsWith(LF)) { // also CRLF checked
            throwBodyMetaNoIndependentDelimiterException(bodyFile, plainText);
        }
        final int rearIndex = plainText.indexOf(delimiter) + delimiter.length();
        if (plainText.length() > rearIndex) { // just in case (empty mail possible?)
            final String rearFirstStr = plainText.substring(rearIndex, rearIndex + 1);
            if (!Srl.equalsPlain(rearFirstStr, LF, CR)) { // e.g. >>> Hello, ...
                throwBodyMetaNoIndependentDelimiterException(bodyFile, plainText);
            }
        }
        if (!meta.startsWith(COMMENT_BEGIN)) { // also leading spaces not allowed
            throwBodyMetaNotStartWithHeaderCommentException(bodyFile, plainText, meta);
        }
        if (!meta.contains(COMMENT_END)) {
            throwBodyMetaHeaderCommentEndMarkNotFoundException(bodyFile, plainText, meta);
        }
        final String headerComment = Srl.extractScopeFirst(plainText, COMMENT_BEGIN, COMMENT_END).getContent();
        final ScopeInfo titleScope = Srl.extractScopeFirst(headerComment, TITLE_BEGIN, TITLE_END);
        if (titleScope == null) {
            throwBodyMetaTitleCommentNotFoundException(bodyFile, plainText);
        }
        final String desc = Srl.substringFirstRear(headerComment, TITLE_END);
        if (desc.isEmpty()) {
            throwBodyMetaDescriptionCommentNotFoundException(bodyFile, plainText);
        }
        final String rearMeta = Srl.substringFirstRear(meta, COMMENT_END);
        // no way because of already checked
        //if (!rearMeta.contains(LF)) {
        //}
        final List<String> splitList = Srl.splitList(rearMeta, LF);
        if (!splitList.get(0).trim().isEmpty()) { // after '*/'
            throwBodyMetaHeaderCommentEndMarkNoIndependentException(bodyFile, plainText);
        }
        if (!splitList.get(1).startsWith(SUBJECT_LABEL)) { // also leading spaces not allowed
            throwBodyMetaSubjectNotFoundException(bodyFile, plainText);
        }
        final int nextIndex = 2;
        if (splitList.size() > nextIndex) { // after subject
            final List<String> nextList = splitList.subList(nextIndex, splitList.size());
            final int nextSize = nextList.size();
            int index = 0;
            for (String line : nextList) {
                if (index == nextSize - 1) { // last loop
                    if (line.isEmpty()) { // empty line only allowed in last loop
                        break;
                    }
                }
                if (!allowedPrefixList.stream().anyMatch(prefix -> line.startsWith(prefix))) {
                    throwBodyMetaUnknownLineException(bodyFile, plainText, line);
                }
                // option check is not here because it can be added in MailFlute
                //if (line.startsWith(OPTION_LABEL)) {
                //    final String options = Srl.substringFirstRear(line, OPTION_LABEL);
                //    final List<String> optionList = Srl.splitListTrimmed(options, ".");
                //    for (String option : optionList) {
                //        if (!optionSet.contains(option)) {
                //            throwBodyMetaUnknownOptionException(bodyFile, plainText, option);
                //        }
                //    }
                //}
                ++index;
            }
        }
    }

    protected void throwBodyMetaNoIndependentDelimiterException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent delimter of mail body meta.");
        br.addItem("Advice");
        br.addElement("The delimter of mail body meta should be independent in line.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */ subject: ... >>>   // *NG");
        br.addElement("    ...your mail body");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>> ...your mail body // *NG");
        br.addElement("  (o)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>                   // OK");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaNotStartWithHeaderCommentException(String bodyFile, String plainText, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not start with the header comment in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with '/*' and should contain '*/'.");
        br.addElement("It means header comment of template file is required.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    subject: ...              // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("");
        br.addElement("  (o)");
        br.addElement("    /*                        // OK");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("");
        br.addElement("And example:");
        br.addElement("  /*");
        br.addElement("   [New Member's Registration]");
        br.addElement("   The memebr will be formalized after click.");
        br.addElement("   And the ...");
        br.addElement("  */");
        br.addElement("  subject: Welcome to your sign up, /*pmb.memberName*/");
        br.addElement("  >>>");
        br.addElement("  Hello, sea");
        br.addElement("  ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNotFoundException(String bodyFile, String plainText, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the header comment end mark in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with '/*' and should contain '*/'.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...             // *NG: not found");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    >>>");
        br.addElement("    */              // *NG: after delimiter");
        br.addElement("    subject: ...");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */              // OK");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaTitleCommentNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the title in the header comment of mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should contain TITLE in the header comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...your mail's description     // *NG");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]         // OK");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaDescriptionCommentNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the description in the header comment of mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should contain DESCRIPTION");
        br.addElement("in the header comment like this:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("    */                              // *NG");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description     // OK");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNoIndependentException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent the header comment end mark in the mail body meta.");
        br.addItem("Advice");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */ subject: ...        // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...           // OK");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaSubjectNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the subject in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should have subject.");
        br.addElement("And should be defined immediately after header comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    >>>                    // *NG");
        br.addElement("    ...your mail body");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    option: ...");
        br.addElement("    subject: ...           // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...           // OK");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaUnknownLineException(String bodyFile, String plainText, String line) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown line in the template meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with option:");
        br.addElement("or fixed style, e.g. '-- !!...!!'");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    maihama     // *NG: unknown meta definition");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("                // *NG: empty line not allowed");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Unknown Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // option check is not here because it can be added in MailFlute
    //protected void throwBodyMetaUnknownOptionException(String bodyFile, String fileText, String option) {
    //    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //    br.addNotice("Unknown option for MailFlute body meta.");
    //    br.addItem("Advice");
    //    br.addElement("You can specify the following option:");
    //    br.addElement(optionSet);
    //    br.addElement("For example:");
    //    br.addElement("  (x):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your mail's title]");
    //    br.addElement("     ...your mail's description");
    //    br.addElement("    */");
    //    br.addElement("    subject: ...");
    //    br.addElement("    option: maihama      // *NG: unknown option");
    //    br.addElement("    >>>");
    //    br.addElement("    ...");
    //    br.addElement("  (o):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your mail's title]");
    //    br.addElement("     ...your mail's description");
    //    br.addElement("    */");
    //    br.addElement("    subject: ...");
    //    br.addElement("    option: genAsIs      // OK");
    //    br.addElement("    >>>");
    //    br.addElement("    ...");
    //    br.addItem("Body File");
    //    br.addElement(bodyFile);
    //    br.addItem("File Text");
    //    br.addElement(fileText);
    //    br.addItem("Unknown Option");
    //    br.addElement(option);
    //    final String msg = br.buildExceptionMessage();
    //    throw new SMailBodyMetaParseFailureException(msg);
    //}

    protected void throwBodyMetaNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the delimiter for mail body meta.");
        br.addItem("Advice");
        br.addElement("The delimiter of mail body meta is '>>>'.");
        br.addElement("It should be defined.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    ...your mail body        // *NG");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>                      // OK");
        br.addElement("    ...your mail body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    option: ...options");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>                      // OK");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void setupBodyFileInfo(ExceptionMessageBuilder br, String bodyFile, String plainText) {
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("Plain Text");
        br.addElement(plainText);
    }

    // ===================================================================================
    //                                                                       HTML Template
    //                                                                       =============
    // *very similar logic also exists on MailFlute
    protected String deriveHtmlFilePath(String bodyFile) {
        final String dirBase = bodyFile.contains("/") ? Srl.substringLastFront(bodyFile, "/") + "/" : "";
        final String pureFileName = Srl.substringLastRear(bodyFile, "/"); // same if no delimiter
        final String front = Srl.substringFirstFront(pureFileName, "."); // e.g. member_registration
        final String rear = Srl.substringFirstRear(pureFileName, "."); // e.g. dfmail or ja.dfmail
        return dirBase + front + "_html." + rear; // e.g. member_registration_html.dfmail
    }

    protected void verifyMailHtmlTemplateTextFormat(String htmlFilePath, String readHtml) {
        if (readHtml.contains(META_DELIMITER)) {
            throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(htmlFilePath, readHtml);
        }
    }

    protected void throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(String htmlFilePath, String readHtml) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("HTML template cannot contain meta delimiter '>>>'.");
        br.addItem("Advice");
        br.addElement("Body meta delimiter '>>>' can be used by plain text template.");
        br.addElement("HTML template has only its body.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...");
        br.addElement("    */");
        br.addElement("    >>>        // *NG");
        br.addElement("    <html>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    <html>     // OK");
        br.addElement("    ...");
        br.addItem("HTML Template");
        br.addElement(htmlFilePath);
        br.addItem("Read HTML");
        br.addElement(readHtml);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    // *check when only generate (when runtime, no check for performance)
    // ===================================================================================
    //                                                                           Plus HTML
    //                                                                           =========
    protected boolean hasOptionPlusHtml(String bodyMeta, String delimiter) {
        if (bodyMeta.contains(OPTION_LABEL)) {
            final String option = Srl.substringFirstFront(Srl.substringFirstRear(bodyMeta, OPTION_LABEL), LF, META_DELIMITER);
            return option.contains(PLUS_HTML_OPTION);
        } else {
            return false;
        }
    }

    protected void throwNoPlusHtmlButHtmlTemplateExistsException(String plainTemplate, String htmlTemplate, String bodyMeta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No option: +html, but HTML template exists.");
        br.addItem("Advice");
        br.addElement("Add option: +html to body meta in plain temlate.");
        br.addElement("Or remove HTML template if unneeded.");
        br.addItem("Plain Template");
        br.addElement(plainTemplate);
        br.addItem("Html Template");
        br.addElement(htmlTemplate);
        br.addItem("Body Meta (in Plain Template)");
        br.addElement(bodyMeta);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwNoHtmlTemplateButPlusHtmlExistsException(String plainTemplate, String htmlTemplate, String bodyMeta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No HTML template, but option: +html exists.");
        br.addItem("Advice");
        br.addElement("Make HTML template at convention path.");
        br.addElement("Or remove option: +html if unneeded.");
        br.addItem("Plain Template");
        br.addElement(plainTemplate);
        br.addItem("Html Template");
        br.addElement(htmlTemplate);
        br.addItem("Body Meta (in Plain Template)");
        br.addElement(bodyMeta);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    public static class SMailBodyMetaParseFailureException extends RuntimeException { // for compatible

        private static final long serialVersionUID = 1L;

        public SMailBodyMetaParseFailureException(String msg) {
            super(msg);
        }
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
    protected String readText(final FileTextIO textIO, String filePath) {
        final String plainText;
        try {
            plainText = textIO.read(new FileInputStream(filePath));
        } catch (FileNotFoundException e) { // no way, collected file
            throw new IllegalStateException("Not found the file: " + filePath, e);
        }
        return plainText;
    }

    protected String toPath(File file) {
        return toPath(file.getPath());
    }

    protected String toPath(String path) {
        return replace(path, "\\", "/");
    }

    protected String replace(String str, String fromStr, String toStr) {
        return Srl.replace(str, fromStr, toStr);
    }
}
