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
package org.dbflute.logic.manage.freegen.mailflute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectAssist;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectBindNode;
import org.dbflute.logic.sql2entity.analyzer.DfParameterAutoDetectProcess;
import org.dbflute.twowaysql.node.IfNode;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfMailFluteTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = MAIL_FLUTE
    //     ; resourceFile = ../../../foo.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaMailPostcard.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; tableMap = map:{
    //     ; targetDir = $$baseDir$$/resources/mail
    //     ; targetExt = .dfmail
    //     ; targetKeyword = 
    //     ; exceptPathList = list:{ contain:/mail/common/ }
    // }
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, Map<String, Object> tableMap,
            Map<String, Map<String, String>> mappingMap) {
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));

        final String targetExt = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);
        final List<File> fileList = DfCollectionUtil.newArrayList();

        collectFile(fileList, targetExt, targetKeyword, exceptPathList, new File(targetDir));
        final Map<String, Map<String, Object>> schemaMap = DfCollectionUtil.newLinkedHashMap();
        for (File file : fileList) {
            final Map<String, Object> mailMap = DfCollectionUtil.newHashMap(); // 'table' on template
            final String fileName = file.getName();
            mailMap.put("fileName", fileName);
            final String className = Srl.camelize(Srl.substringLastFront(fileName, targetExt)) + "Postcard";
            mailMap.put("className", className);
            mailMap.put("camelizedName", className);

            final String domainPath = buildDomainPath(file, targetDir);
            mailMap.put("domainPath", domainPath); // e.g. /mail/member/member_registration.ml

            mailMap.put("defName", buildUpperSnakeName(domainPath));
            {
                final String dirPath = Srl.substringLastFront(domainPath, "/");
                final String snakeCase = buildPlainSnakeName(dirPath);
                final String camelizedName = Srl.camelize(snakeCase);
                mailMap.put("camelizedDir", camelizedName);
                mailMap.put("capCamelDir", Srl.initCap(camelizedName));
                mailMap.put("uncapCamelDir", Srl.initUncap(camelizedName));
            }
            {
                final String snakeCase = buildPlainSnakeName(fileName);
                final String camelizedName = Srl.camelize(snakeCase);
                mailMap.put("camelizedFile", camelizedName);
                mailMap.put("capCamelFile", Srl.initCap(camelizedName));
                mailMap.put("uncapCamelFile", Srl.initUncap(camelizedName));
            }
            final String fileText;
            try {
                fileText = new FileTextIO().encodeAsUTF8().read(new FileInputStream(file));
            } catch (FileNotFoundException e) { // no way, collected file
                throw new IllegalStateException("Not found the file: " + file);
            }
            final Map<String, String> propertyNameTypeMap = new LinkedHashMap<String, String>();
            final Map<String, String> propertyNameOptionMap = new LinkedHashMap<String, String>();
            final Set<String> autoDetectedPropertyNameSet = new LinkedHashSet<String>();
            processAutoDetect(fileText, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
            final List<Map<String, String>> propertyList = new ArrayList<Map<String, String>>();
            for (String propertyName : autoDetectedPropertyNameSet) {
                final Map<String, String> propertyMap = new LinkedHashMap<String, String>(); // 'property' on template
                propertyMap.put("propertyName", propertyName);
                propertyMap.put("capCalemName", Srl.initCap(propertyName));
                propertyMap.put("uncapCalemName", Srl.initUncap(propertyName));
                propertyMap.put("propertyType", propertyNameTypeMap.get(propertyName)); // exists
                propertyList.add(propertyMap);
            }
            mailMap.put("propertyList", propertyList);
            schemaMap.put(fileName, mailMap);
        }
        return new DfFreeGenTable(tableMap, schemaMap);
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

    protected void processAutoDetect(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
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
        process.processAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
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
        return replace(file.getPath(), "\\", "/");
    }

    protected String replace(String str, String fromStr, String toStr) {
        return Srl.replace(str, fromStr, toStr);
    }
}
