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
package org.dbflute.logic.manage.freegen.table.filepath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfFilePathTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = FILE_PATH
    // }
    // ; outputMap = map:{
    //     ; templateFile = JspPath.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = JspPath
    // }
    // ; optionMap = map:{
    //     ; targetDir = $$baseDir$$/webapp/WEB-INF/view
    //     ; targetExt = .jsp
    //     ; targetKeyword =
    //     ; exceptPathList = list:{ contain:/view/common/ }
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));

        final String[] targetExts = extractTargetExt(tableMap);
        final String targetKeyword = extractTargetKeyword(tableMap);
        final List<String> exceptPathList = extractExceptPathList(tableMap);
        final List<File> fileList = DfCollectionUtil.newArrayList();

        collectFile(fileList, targetExts, targetKeyword, exceptPathList, new File(targetDir));
        final List<Map<String, Object>> columnList = DfCollectionUtil.newArrayList();
        for (File file : fileList) {
            final Map<String, Object> columnMap = DfCollectionUtil.newHashMap();
            final String fileName = file.getName();
            columnMap.put("fileName", fileName);

            final String domainPath = buildDomainPath(file, targetDir);
            columnMap.put("domainPath", domainPath); // e.g. /view/member/index.jsp

            columnMap.put("defName", buildUpperSnakeName(domainPath));
            {
                final String dirPath = Srl.substringLastFront(domainPath, "/");
                final String snakeCase = buildPlainSnakeName(dirPath);
                final String camelizedName = Srl.camelize(snakeCase);
                columnMap.put("camelizedDir", camelizedName);
                columnMap.put("capCamelDir", Srl.initCap(camelizedName));
                columnMap.put("uncapCamelDir", Srl.initUncap(camelizedName));
            }
            {
                final String snakeCase = buildPlainSnakeName(fileName);
                final String camelizedName = Srl.camelize(snakeCase);
                columnMap.put("camelizedFile", camelizedName);
                columnMap.put("capCamelFile", Srl.initCap(camelizedName));
                columnMap.put("uncapCamelFile", Srl.initUncap(camelizedName));
            }
            columnList.add(columnMap);
        }
        return DfFreeGenMetaData.asOnlyOne(tableMap, "unused", columnList);
    }

    protected String[] extractTargetExt(Map<String, Object> tableMap) {
        final String targetExt = (String) tableMap.get("targetExt"); // not required
        if (targetExt == null) {
            return new String[] {};
        }
        final List<String> extList = Srl.splitList(targetExt, "|");
        final List<String> filteredList = new ArrayList<String>(extList.size());
        for (String ext : extList) {
            filteredList.add(!ext.startsWith(".") ? ("." + ext) : ext);
        }
        return filteredList.toArray(new String[filteredList.size()]);
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

    // ===================================================================================
    //                                                                        Collect File
    //                                                                        ============
    /**
     * @param fileList The list of saved list. (NotNull)
     * @param targetExts The extensions of target path. (NullAllowed)
     * @param targetKeyword The keyword of target path. (NullAllowed)
     * @param exceptPathList The list of except path. (NotNull)
     * @param baseFile The base file. (NotNull)
     */
    protected void collectFile(List<File> fileList, String[] targetExts, String targetKeyword, List<String> exceptPathList, File baseFile) {
        final FileHierarchyTracer tracer = new FileHierarchyTracer();
        tracer.trace(baseFile, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                if (currentFile.isDirectory()) {
                    return true;
                }
                return isCollectFile(targetExts, targetKeyword, exceptPathList, currentFile);
            }

            public void handleFile(File currentFile) {
                fileList.add(currentFile);
            }
        });
    }

    protected boolean isCollectFile(String[] targetExts, String targetKeyword, List<String> exceptPathList, File currentFile) {
        if (isExceptFile(exceptPathList, currentFile)) {
            return false;
        }
        for (String ext : targetExts) {
            if (isHitByTargetExt(toPath(currentFile), ext, targetKeyword)) {
                return true;
            }
        }
        return false;
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
