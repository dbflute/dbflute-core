/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.pmfile;

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
public class DfPmFileTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String META_DELIMITER = ">>>";
    public static final String COMMENT_BEGIN = "/*";
    public static final String COMMENT_END = "*/";
    public static final String TITLE_BEGIN = "[";
    public static final String TITLE_END = "]";
    public static final String OPTION_LABEL = "option:";
    public static final String PROPDEF_PREFIX = "-- !!";
    // option check is not here because it can be added in MailFlute
    //public static final Set<String> optionSet;
    //static {
    //    optionSet = Collections.unmodifiableSet(DfCollectionUtil.newLinkedHashSet("genAsIs"));
    //}
    public static final List<String> allowedPrefixList; // except first line (comment)
    static {
        allowedPrefixList = Arrays.asList(OPTION_LABEL, PROPDEF_PREFIX);
    }
    protected static final String LF = "\n";
    protected static final String CR = "\r";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final boolean docProcess;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPmFileTableLoader(boolean docProcess) {
        this.docProcess = docProcess;
    }

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = PM_FILE
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaPmTemplate.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; optionMap = map:{
    //     ; targetDir = $$baseDir$$/resources
    //     ; targetExt = .dfpm
    //     ; targetKeyword =
    //     ; exceptPathList = list:{ contain:/common/ }
    //     ; targetSuffix = Bean
    //     ; isConventionSuffix = false
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get(deriveTableMapKey("targetDir")));
        final String targetExt = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);
        final Map<String, Map<String, Object>> schemaMap = doLoad(targetDir, targetExt, targetKeyword, exceptPathList, tableMap);
        return DfFreeGenMetaData.asMultiple(tableMap, schemaMap);
    }

    protected Map<String, Map<String, Object>> doLoad(String targetDir, String targetExt, String targetKeyword,
            List<String> exceptPathList, Map<String, Object> tableMap) {
        final List<File> fileList = DfCollectionUtil.newArrayList();
        final File baseDir = new File(targetDir);
        collectFile(fileList, targetExt, targetKeyword, exceptPathList, baseDir);
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8().removeUTF8Bom().replaceCrLfToLf();
        for (File pmFile : fileList) {
            final Map<String, Object> table = DfCollectionUtil.newHashMap();
            final String fileName = pmFile.getName();
            table.put("fileName", fileName);
            final String fileText;
            try {
                fileText = textIO.read(new FileInputStream(pmFile));
            } catch (FileNotFoundException e) { // no way, collected file
                throw new IllegalStateException("Not found the pmc file: " + pmFile, e);
            }
            final String delimiter = META_DELIMITER;
            if (((String) tableMap.getOrDefault(deriveTableMapKey("isLastaTemplate"), "false")).equalsIgnoreCase("true")) {
                final String templatePath = toPath(pmFile);
                if (!fileText.contains(delimiter)) {
                    throwTemplateMetaNotFoundException(templatePath, fileText);
                }
                verifyFormat(templatePath, fileText, delimiter);
                final String headerComment = Srl.extractScopeFirst(fileText, COMMENT_BEGIN, COMMENT_END).getContent();
                final ScopeInfo titleScope = Srl.extractScopeFirst(headerComment, TITLE_BEGIN, TITLE_END);
                final String desc = Srl.substringFirstRear(headerComment, TITLE_END);
                table.put("headerComment", headerComment);
                table.put("title", titleScope.getContent());
                table.put("description", desc);
            }
            String option = null;
            if (fileText.contains(delimiter)) {
                final String bodyMeta = Srl.substringFirstFront(fileText, ">>>");
                if (bodyMeta.contains(OPTION_LABEL)) {
                    option = Srl.substringFirstFront(Srl.substringFirstRear(bodyMeta, OPTION_LABEL), LF);
                }
            }
            final boolean convention = !isGenAsIs(option);
            final StringBuilder classNameSb = new StringBuilder();
            classNameSb.append(Srl.camelize(Srl.substringLastFront(fileName, targetExt)));
            final String classSuffix = convention ? deriveClassSuffix(tableMap, baseDir, pmFile) : "";
            classNameSb.append(classSuffix);
            final String className = classNameSb.toString();
            table.put("className", className); // used as output file name
            table.put("camelizedName", className);

            final String domainPath = buildDomainPath(pmFile, targetDir);
            table.put("domainPath", domainPath); // e.g. /member/member_registration.dfpm
            final String resourcePath = Srl.ltrim(domainPath, "/");
            table.put("resourcePath", resourcePath); // e.g. member/member_registration.dfpm
            final String additionalPkg;
            final String basePkgConnector;
            if (Srl.is_NotNull_and_NotEmpty(resourcePath)) {
                if (resourcePath.contains("/")) {
                    additionalPkg = Srl.replace(Srl.substringLastFront(resourcePath, "/"), "/", ".");
                    basePkgConnector = ".";
                } else {
                    additionalPkg = "";
                    basePkgConnector = "";
                }
            } else {
                additionalPkg = "";
                basePkgConnector = "";
            }
            table.put("additionalPackage", convention ? "template" + basePkgConnector + additionalPkg : additionalPkg);

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
            final Map<String, String> propertyNameTypeMap = new LinkedHashMap<String, String>();
            final Map<String, String> propertyNameOptionMap = new LinkedHashMap<String, String>();
            final Set<String> propertyNameSet = new LinkedHashSet<String>();
            processAutoDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, propertyNameSet);
            processSpecifiedDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, propertyNameSet);
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

    protected boolean isGenAsIs(String option) {
        return option != null && option.contains("genAsIs");
    }

    // -----------------------------------------------------
    //                                      Extract Resource
    //                                      ----------------
    protected String deriveTableMapKey(String key) {
        return docProcess ? "template" + Srl.initCap(key) : key;
    }

    protected String extractTargetExt(Map<String, Object> tableMap) {
        final String targetExt = (String) tableMap.get(deriveTableMapKey("targetExt")); // not required
        if (targetExt != null && !targetExt.startsWith(".")) {
            return "." + targetExt;
        }
        return targetExt;
    }

    protected String extractTargetKeyword(Map<String, Object> tableMap) {
        return (String) tableMap.get(deriveTableMapKey("targetKeyword")); // not required
    }

    protected List<String> extractExceptPathList(Map<String, Object> tableMap) {
        @SuppressWarnings("unchecked")
        List<String> exceptPathList = (List<String>) tableMap.get(deriveTableMapKey("exceptPathList")); // not required
        if (exceptPathList == null) {
            exceptPathList = DfCollectionUtil.newArrayListSized(4);
        }
        exceptPathList.add("contain:.svn");
        return exceptPathList;
    }

    protected String deriveClassSuffix(Map<String, Object> tableMap, File baseDir, File pmFile) {
        if (((String) tableMap.getOrDefault(deriveTableMapKey("isConventionSuffix"), "true")).equalsIgnoreCase("true")) {
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
            String suffix = "";
            if (currentCano.startsWith(baseCano)) {
                final String relativePath = Srl.ltrim(Srl.substringFirstRear(currentCano, baseCano), "/");
                if (relativePath.contains("/")) {
                    suffix = Srl.initCap(Srl.substringFirstFront(relativePath, "/").toLowerCase());
                }
            }
            return suffix;
        } else {
            return (String) tableMap.getOrDefault(deriveTableMapKey("targetSuffix"), "");
        }
    }

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
    //                                                                       Verify Format
    //                                                                       =============
    protected void verifyFormat(String templatePath, String evaluated, String delimiter) {
        final String meta = Srl.substringFirstFront(evaluated, delimiter);
        if (!meta.endsWith(LF)) { // also CRLF checked
            throwBodyMetaNoIndependentDelimiterException(templatePath, evaluated);
        }
        final int rearIndex = evaluated.indexOf(delimiter) + delimiter.length();
        if (evaluated.length() > rearIndex) { // just in case (empty template possible?)
            final String rearFirstStr = evaluated.substring(rearIndex, rearIndex + 1);
            if (!Srl.equalsPlain(rearFirstStr, LF, CR)) { // e.g. >>> Hello, ...
                throwBodyMetaNoIndependentDelimiterException(templatePath, evaluated);
            }
        }
        if (!meta.startsWith(COMMENT_BEGIN)) { // also leading spaces not allowed
            throwTemplateMetaNotStartWithHeaderCommentException(templatePath, evaluated, meta);
        }
        if (!meta.contains(COMMENT_END)) {
            throwBodyMetaHeaderCommentEndMarkNotFoundException(templatePath, evaluated, meta);
        }
        final String headerComment = Srl.extractScopeFirst(evaluated, COMMENT_BEGIN, COMMENT_END).getContent();
        final ScopeInfo titleScope = Srl.extractScopeFirst(headerComment, TITLE_BEGIN, TITLE_END);
        if (titleScope == null) {
            throwBodyMetaTitleCommentNotFoundException(templatePath, evaluated);
        }
        final String desc = Srl.substringFirstRear(headerComment, TITLE_END);
        if (desc.isEmpty()) {
            throwBodyMetaDescriptionCommentNotFoundException(templatePath, evaluated);
        }
        final String rearMeta = Srl.substringFirstRear(meta, COMMENT_END);
        // no way because of already checked
        //if (!rearMeta.contains(LF)) {
        //}
        final List<String> splitList = Srl.splitList(rearMeta, LF);
        if (!splitList.get(0).trim().isEmpty()) { // after '*/'
            throwBodyMetaHeaderCommentEndMarkNoIndependentException(templatePath, evaluated);
        }
        final int nextIndex = 1;
        if (splitList.size() > nextIndex) { // after header comment
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
                    throwBodyMetaUnknownLineException(templatePath, evaluated, line);
                }
                // option check is not here because it can be added in MailFlute
                //if (line.startsWith(OPTION_LABEL)) {
                //    final String options = Srl.substringFirstRear(line, OPTION_LABEL);
                //    final List<String> optionList = Srl.splitListTrimmed(options, ".");
                //    for (String option : optionList) {
                //        if (!optionSet.contains(option)) {
                //            throwBodyMetaUnknownOptionException(templatePath, evaluated, option);
                //        }
                //    }
                //}
                ++index;
            }
        }
    }

    protected void throwBodyMetaNoIndependentDelimiterException(String templatePath, String evaluated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent delimter of template meta.");
        br.addItem("Advice");
        br.addElement("The delimter of template meta should be independent in line.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */ >>>                    // *NG");
        br.addElement("    ...your template body");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    >>> ...your template body // *NG");
        br.addElement("  (o)");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    >>>                       // OK");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwTemplateMetaNotStartWithHeaderCommentException(String templatePath, String evaluated, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not start with the header comment in the template meta.");
        br.addItem("Advice");
        br.addElement("The template meta should start with '/*' and should contain '*/'.");
        br.addElement("It means header comment of template file is required.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    subject: ...              // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("");
        br.addElement("  (o)");
        br.addElement("    /*                        // OK");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("");
        br.addElement("And example:");
        br.addElement("  /*");
        br.addElement("   [New Member's Registration]");
        br.addElement("   The memebr will be formalized after click.");
        br.addElement("   And the ...");
        br.addElement("  */");
        br.addElement("  >>>");
        br.addElement("  Hello, sea");
        br.addElement("  ...");
        setupTemplateFileInfo(br, templatePath, evaluated);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNotFoundException(String templatePath, String evaluated, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the header comment end mark in the template meta.");
        br.addItem("Advice");
        br.addElement("The template meta should start with '/*' and should contain '*/'.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...");
        br.addElement("    >>>");
        br.addElement("    */");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     ...");
        br.addElement("    */              // OK");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwBodyMetaTitleCommentNotFoundException(String templatePath, String evaluated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the title in the header comment of template meta.");
        br.addItem("Advice");
        br.addElement("The template meta should contain TITLE in the header comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...your template's description     // *NG");
        br.addElement("    */");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]         // OK");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwBodyMetaDescriptionCommentNotFoundException(String templatePath, String evaluated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the description in the header comment of template meta.");
        br.addItem("Advice");
        br.addElement("The template meta should contain DESCRIPTION");
        br.addElement("in the header comment like this:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("    */                                  // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description     // OK");
        br.addElement("    */");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNoIndependentException(String templatePath, String evaluated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent the header comment end mark in the template meta.");
        br.addItem("Advice");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */ option: ...         // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    option: ...            // OK");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void throwBodyMetaUnknownLineException(String templatePath, String evaluated, String line) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown line in the template meta.");
        br.addItem("Advice");
        br.addElement("The template meta should start with option:");
        br.addElement("or fixed style, e.g. '-- !!...!!'");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    maihama     // *NG: unknown meta definition");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("                // *NG: empty line not allowed");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        br.addItem("Unknown Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
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
    //    br.addElement("     [...your template's title]");
    //    br.addElement("     ...your template's description");
    //    br.addElement("    */");
    //    br.addElement("    option: maihama      // *NG: unknown option");
    //    br.addElement("    >>>");
    //    br.addElement("    ...your template body");
    //    br.addElement("  (o):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your template's title]");
    //    br.addElement("     ...your template's description");
    //    br.addElement("    */");
    //    br.addElement("    option: genAsIs      // OK");
    //    br.addElement("    >>>");
    //    br.addElement("    ...your template body");
    //    br.addItem("Body File");
    //    br.addElement(bodyFile);
    //    br.addItem("File Text");
    //    br.addElement(fileText);
    //    br.addItem("Unknown Option");
    //    br.addElement(option);
    //    final String msg = br.buildExceptionMessage();
    //    throw new TemplateFileParseFailureException(msg);
    //}

    protected void throwTemplateMetaNotFoundException(String templatePath, String evaluated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the delimiter for template meta.");
        br.addItem("Advice");
        br.addElement("The delimiter of template meta is '>>>'.");
        br.addElement("It should be defined.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    ...your template body    // *NG");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    >>>                      // OK");
        br.addElement("    ...your template body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your template's title]");
        br.addElement("     ...your template's description");
        br.addElement("    */");
        br.addElement("    option: ...options");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>                      // OK");
        br.addElement("    ...your template body");
        setupTemplateFileInfo(br, templatePath, evaluated);
        final String msg = br.buildExceptionMessage();
        throw new TemplateFileParseFailureException(msg);
    }

    protected void setupTemplateFileInfo(ExceptionMessageBuilder br, String templatePath, String evaluated) {
        br.addItem("Template File");
        br.addElement(templatePath);
        br.addItem("Evaluated");
        br.addElement(evaluated);
    }

    public static class TemplateFileParseFailureException extends RuntimeException { // for compatible

        private static final long serialVersionUID = 1L;

        public TemplateFileParseFailureException(String msg) {
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
