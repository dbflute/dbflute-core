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
package org.dbflute.logic.manage.freegen.table.pmfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
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

/**
 * @author jflute
 */
public class DfPmFileTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PROPDEF_PREFIX = "-- !!";
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

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
    // ; tableMap = map:{
    //     ; targetDir = $$baseDir$$/resources
    //     ; targetExt = .dfpm
    //     ; targetKeyword = 
    //     ; exceptPathList = list:{ contain:/common/ }
    //     ; targetSuffix = Bean
    //     ; isConventionSuffix = false
    // }
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getTableMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));
        final String targetExt = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);
        final Map<String, Map<String, Object>> schemaMap = doLoad(targetDir, targetExt, targetKeyword, exceptPathList, tableMap);
        return new DfFreeGenTable(tableMap, schemaMap);
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
            final StringBuilder classNameSb = new StringBuilder();
            classNameSb.append(Srl.camelize(Srl.substringLastFront(fileName, targetExt)));
            final String classSuffix = deriveClassSuffix(tableMap, baseDir, pmFile);
            classNameSb.append(classSuffix);
            final String className = classNameSb.toString();
            table.put("className", className); // used as output file name
            table.put("camelizedName", className);
            if (Srl.is_NotNull_and_NotEmpty(classSuffix)) {
                table.put("additionalPackage", classSuffix.toLowerCase());
            }

            final String domainPath = buildDomainPath(pmFile, targetDir);
            table.put("domainPath", domainPath); // e.g. /member/member_registration.dfpm
            table.put("resourcePath", Srl.ltrim(domainPath, "/")); // e.g. member/member_registration.dfpm

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
                fileText = textIO.read(new FileInputStream(pmFile));
            } catch (FileNotFoundException e) { // no way, collected file
                throw new IllegalStateException("Not found the pmc file: " + pmFile, e);
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

    protected String deriveClassSuffix(Map<String, Object> tableMap, File baseDir, File pmFile) {
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
            String suffix = "";
            if (currentCano.startsWith(baseCano)) {
                final String relativePath = Srl.ltrim(Srl.substringFirstRear(currentCano, baseCano), "/");
                if (relativePath.contains("/")) {
                    suffix = Srl.initCap(Srl.substringFirstFront(relativePath, "/").toLowerCase());
                }
            }
            return suffix;
        } else {
            return (String) tableMap.getOrDefault("targetSuffix", "");
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
