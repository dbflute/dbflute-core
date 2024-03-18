/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.lastaflute;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.logic.manage.freegen.table.appcls.DfAppClsTableLoader;
import org.dbflute.logic.manage.freegen.table.appcls.DfWebClsTableLoader;
import org.dbflute.logic.manage.freegen.table.json.DfJsonFreeAgent;
import org.dbflute.logic.manage.freegen.table.mailflute.DfMailFluteTableLoader;
import org.dbflute.logic.manage.freegen.table.pmfile.DfPmFileTableLoader;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfLastaFluteProperties;
import org.dbflute.properties.assistant.lastaflute.DfLastaDocContentsMap.DfLastaDocContentsActionMap;
import org.dbflute.task.manage.DfFreeGenTask;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author p1us2er0
 */
public class DfLastaDocTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLastaDocTableLoader.class);

    /**
     * Is maven test for document already executed in the DBFlute process? <br>
     * Maven project of LastaFlute has base project so only one execution is needed.
     * So it needs to suppress duplicate execution. <br>
     * (DBFlute can treat one project of LastaFlute at one execution so static is no problem)
     */
    private static boolean _mvnTestDocumentExecuted;

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = LASTA_DOC
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaDocHtml.vm
    //     ; outputDirectory = $$baseDir$$/../test/resources
    //     ; package = doc
    //     ; className = lastadoc-dockside
    //     ; fileExt = html
    // }
    // ; optionMap = map:{
    //     ; targetDir = $$baseDir$$/java
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> optionMap = mapProp.getOptionMap();
        final String targetDir = resource.resolveBaseDir((String) optionMap.get("targetDir"));
        final File rootDir = new File(targetDir);
        if (!rootDir.exists()) {
            throw new IllegalStateException("Not found the targetDir: " + targetDir);
        }
        final DfLastaFlutePhysicalHolder physicalHolder = preparePhysicalHolder(rootDir);
        final List<Map<String, Object>> columnList = prepareColumnList(physicalHolder);
        executeTestDocument(optionMap);
        final Path lastaDocFile = acceptLastaDocFile(optionMap);
        if (Files.exists(lastaDocFile)) {
            optionMap.putAll(decodeJsonMap(lastaDocFile));
        }
        optionMap.put("appList", findAppList(mapProp));
        if (optionMap.get("mailPackage") != null) {
            optionMap.put("mailList", new DfMailFluteTableLoader(true).loadTable(requestName, resource, mapProp).getTableList());
        }
        if (optionMap.get("templatePackage") != null) {
            optionMap.put("templateList", new DfPmFileTableLoader(true).loadTable(requestName, resource, mapProp).getTableList());
        }
        if (optionMap.get("namedclsList") != null) { // should be before appcls, webcls for refCls
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> namedclsList = (List<Map<String, Object>>) optionMap.get("namedclsList");
            if (!namedclsList.isEmpty()) {
                final List<Map<String, Object>> loadedList = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> namedclsMap : namedclsList) {
                    final String clsTheme = (String) namedclsMap.get("clsTheme");
                    final DfFreeGenResource docResource = createDocResource(resource, namedclsMap, clsTheme);
                    final DfFreeGenMapProp docMapProp = createDocMapProp(mapProp, namedclsMap);
                    loadedList.add(new DfAppClsTableLoader().loadTable(requestName, docResource, docMapProp).getOptionMap());
                }
                optionMap.put("namedclsList", loadedList); // override, convert to loaded list
            }
        }
        if (optionMap.get("appclsPackage") != null) { // should be after namedcls for refCls
            final String clsTheme = "appcls";
            final DfFreeGenResource docResource = createDocResource(resource, optionMap, clsTheme);
            final DfFreeGenMapProp docMapProp = createDocMapProp(mapProp, mapProp.getOptionMap());
            optionMap.put("appclsMap", new DfAppClsTableLoader().loadTable(requestName, docResource, docMapProp).getOptionMap());
        }
        if (optionMap.get("webclsPackage") != null) { // should be after appcls for refCls
            final String clsTheme = "webcls";
            final DfFreeGenResource docResource = createDocResource(resource, optionMap, clsTheme);
            final DfFreeGenMapProp docMapProp = createDocMapProp(mapProp, mapProp.getOptionMap());
            optionMap.put("webclsMap", new DfWebClsTableLoader().loadTable(requestName, docResource, docMapProp).getOptionMap());
        }
        setupContentsAdjustment(optionMap);
        return DfFreeGenMetaData.asOnlyOne(optionMap, "unused", columnList);
    }

    protected DfLastaFlutePhysicalHolder preparePhysicalHolder(File rootDir) {
        final DfLastaFlutePhysicalHolder physicalHolder = new DfLastaFlutePhysicalHolder();
        final FileHierarchyTracer tracer = new FileHierarchyTracer();
        tracer.trace(rootDir, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                return true;
            }

            public void handleFile(File currentFile) throws IOException {
                final String path = toPath(currentFile);
                if (path.contains("/app/web/") && path.endsWith("Action.java")) {
                    physicalHolder.addAction(currentFile);
                }
            }
        });
        return physicalHolder;
    }

    protected DfFreeGenResource createDocResource(DfFreeGenResource resource, final Map<String, Object> optionMap, final String clsTheme) {
        final String resourceFile = (String) optionMap.get(clsTheme + "ResourceFile");
        if (resourceFile == null) { // no way
            throw new IllegalStateException("Not found the resource file for clsTheme: " + clsTheme + ", " + optionMap.keySet());
        }
        return new DfFreeGenResource(resource.getBaseDir(), resource.getResourceType(), resourceFile, resource.getEncoding());
    }

    protected DfFreeGenMapProp createDocMapProp(DfFreeGenMapProp mapProp, Map<String, Object> optionMap) {
        return new DfFreeGenMapProp(DfCollectionUtil.newLinkedHashMap(optionMap), mapProp.getMappingMap(), mapProp.getRequestMap());
    }

    protected List<Map<String, Object>> prepareColumnList(DfLastaFlutePhysicalHolder physicalHolder) {
        final List<Map<String, Object>> columnList = new ArrayList<Map<String, Object>>();
        final List<File> actionList = physicalHolder.getActionList();
        for (File action : actionList) {
            final Map<String, Object> columnMap = new LinkedHashMap<String, Object>();
            final String className = Srl.substringLastFront(action.getName(), ".");
            final String url = calculateUrl(className);
            columnMap.put("className", className);
            columnMap.put("url", url);
            columnList.add(columnMap);
        }
        return columnList;
    }

    protected String calculateUrl(String className) {
        if ("RootAction".equals(className)) {
            return "/";
        } else {
            return "/" + Srl.decamelize(Srl.removeSuffix(className, "Action"), "/").toLowerCase() + "/";
        }
    }

    protected Map<? extends String, ? extends Object> decodeJsonMap(final Path lastaDocFile) {
        return new DfJsonFreeAgent().decodeJsonMap("lastadoc", lastaDocFile.toFile().getPath());
    }

    protected List<Map<String, String>> findAppList(DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        List<Map<String, String>> appList;
        try {
            final String outputDirectory = getLastaFluteProperties().getLastaDocOutputDirectory();
            appList = Files.list(Paths.get(outputDirectory)).filter(entry -> {
                return entry.getFileName().toString().matches(".*lastadoc-.*\\.html");
            }).map(file -> {
                Map<String, String> appMap = DfCollectionUtil.newLinkedHashMap();
                appMap.put("appName", file.toFile().getName().replaceAll("(lastadoc-|\\.html)", ""));
                appMap.put("lastadocPath", file.toFile().getName());
                return appMap;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("can't read directory", e);
        }
        Map<String, String> appMap = DfCollectionUtil.newLinkedHashMap();
        appMap.put("appName", (String) tableMap.get("appName"));
        appMap.put("lastadocPath", "lastadoc-" + tableMap.get("appName") + ".html");
        appList = appList.stream().distinct().sorted((app, app2) -> {
            return app.get("appName").compareTo(app2.get("appName"));
        }).collect(Collectors.toList());
        return appList;
    }

    // -----------------------------------------------------
    //                                 Execute Test Document
    //                                 ---------------------
    protected void executeTestDocument(Map<String, Object> tableMap) {
        try {
            if (getLastaFluteProperties().isLastaDocMavenGeared()) {
                executeMvnTestDocument(tableMap);
            }
            if (getLastaFluteProperties().isLastaDocGradleGeared()) {
                executeGradleTestDocument(tableMap);
            }
        } catch (RuntimeException continued) {
            _log.info("Failed to execute maven or gradle test, but continue...", continued);
        }
    }

    // -----------------------------------------------------
    //                                            Maven Test
    //                                            ----------
    protected void executeMvnTestDocument(Map<String, Object> tableMap) {
        if (_mvnTestDocumentExecuted) { // see the field comment for the detail
            return;
        }
        final String path = (String) tableMap.get("path");
        if (Files.exists(Paths.get(path, "pom.xml"))) {
            _mvnTestDocumentExecuted = true;
            DfFreeGenTask.regsiterLazyCall(() -> {
                new Thread(() -> doExecuteMvnTestDocument(path)).start();
            });
        }
    }

    protected void doExecuteMvnTestDocument(String path) { // path may be ".." (if common project)
        // "test_*" means that it executes all tests on [App]LastaDocTest.java.
        // so it also contains test_swagger() since 2022/01/19
        final String dtestExp = "-Dtest=*LastaDocTest#test_*";
        final ProcessBuilder processBuilder = createProcessBuilder("mvn", "test", "-DfailIfNoTests=false", dtestExp);
        final File directory = prepareMvnTestProcessDirectory(path); // basically base project
        processBuilder.directory(directory);
        _log.info("...Executing mvn test: " + directory);
        executeCommand(processBuilder);
        _log.info("*Done mvn test: " + directory);
    }

    protected File prepareMvnTestProcessDirectory(String path) { // e.g. ../../maihama-dockside
        final String appName = extractAppNameFromPath(path); // e.g. maihama
        final Path basePath = Paths.get(path, "../" + appName + "-base"); // e.g. ../../maihama-dockside/../maihama-base
        return Files.exists(basePath) ? basePath.toFile() : new File(path);
    }

    protected String extractAppNameFromPath(String path) {
        final String currentProjectName;
        try {
            // the path may be ".." e.g. common project
            // so it uses canonical path to get project name certainly
            final String canonicalPath = new File(path).getCanonicalPath();
            currentProjectName = Srl.substringLastRear(canonicalPath, "/"); // e.g. maihama-dockside, maihama-common
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get canonical path from the path: " + path, e);
        }
        return DfStringUtil.substringLastFront(currentProjectName, "-"); // e.g. maihama
    }

    // -----------------------------------------------------
    //                                           Gradle Test
    //                                           -----------
    protected void executeGradleTestDocument(Map<String, Object> tableMap) {
        // gradle executes each application so no static boolean like Maven
        if (Files.exists(Paths.get((String) tableMap.get("path"), "gradlew"))) {
            DfFreeGenTask.regsiterLazyCall(() -> {
                new Thread(() -> doExecuteGradleTestDocument(tableMap)).start();
            });
        }
    }

    protected void doExecuteGradleTestDocument(Map<String, Object> tableMap) {
        // same as MvnTest, see the method for the detail
        final String testsExp = "*LastaDocTest.test*";
        final ProcessBuilder processBuilder = createProcessBuilder("./gradlew", "cleanTest", "test", "--tests", testsExp);
        final File directory = Paths.get((String) tableMap.get("path")).toFile();
        processBuilder.directory(directory);
        _log.info("...Executing gradle test: " + directory);
        executeCommand(processBuilder);
        _log.info("*Done gradle test: " + directory);
    }

    // -----------------------------------------------------
    //                                      Process Handling
    //                                      ----------------
    protected ProcessBuilder createProcessBuilder(String... command) {
        final List<String> list = DfCollectionUtil.newArrayList();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            list.add("cmd");
            list.add("/c");
        }
        list.addAll(Arrays.asList(command));
        return new ProcessBuilder(list);
    }

    protected int executeCommand(ProcessBuilder processBuilder) {
        processBuilder.redirectErrorStream(true);
        try {
            final Process process = processBuilder.start();
            try (InputStream ins = process.getInputStream();
                    InputStreamReader reader = new InputStreamReader(ins);
                    BufferedReader br = new BufferedReader(reader)) {
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    _log.debug(line);
                }
            }
            process.waitFor();
            return process.exitValue();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    // -----------------------------------------------------
    //                                         LastaDoc File
    //                                         -------------
    protected Path acceptLastaDocFile(Map<String, Object> tableMap) {
        final List<Path> candidateList = DfCollectionUtil.newArrayList();
        final String path = (String) tableMap.get("path");
        candidateList.add(Paths.get(path, "target/lastadoc/analyzed-lastadoc.json"));
        candidateList.add(Paths.get(path, "build/lastadoc/analyzed-lastadoc.json"));
        candidateList.add(Paths.get(path, "target/lastadoc/lastadoc.json")); // for compatible
        candidateList.add(Paths.get(path, "build/lastadoc/lastadoc.json")); // for compatible
        final Path lastaDocFile = Paths.get(String.format("./schema/project-lastadoc-%s.json", tableMap.get("appName")));
        candidateList.forEach(candidate -> {
            if (!Files.exists(candidate)) {
                return;
            }
            try {
                // compare last modified time
                if (Files.exists(lastaDocFile)
                        && Files.getLastModifiedTime(lastaDocFile).compareTo(Files.getLastModifiedTime(candidate)) > 0) {
                    return;
                }
                Files.copy(candidate, lastaDocFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                String msg = "IO exception when copy lastaDocFile.";
                msg += " source: " + candidate + ", target: " + lastaDocFile;
                throw new IllegalStateException(msg, e);
            }
        });
        return lastaDocFile;
    }

    // -----------------------------------------------------
    //                                   Contents Adjustment
    //                                   --------------------
    protected void setupContentsAdjustment(Map<String, Object> optionMap) {
        doSetupContentsHeader(optionMap);
        doSetupContentsAction(optionMap);
    }

    protected void doSetupContentsHeader(Map<String, Object> optionMap) {
        final DfLastaFluteProperties prop = getLastaFluteProperties();
        final boolean hasSchemaHtml;
        final String schemaHtmlPath;
        if (prop.isSuppressLastaDocSchemaHtmlLink()) {
            hasSchemaHtml = false;
            schemaHtmlPath = null;
        } else {
            final String outputDirectory = prop.getLastaDocOutputDirectory();
            final String schemaHtmlFileName = getDocumentProperties().getSchemaHtmlFileName(getBasicProperties().getProjectName());
            final File schemaHtmlFile = new File(outputDirectory + "/" + schemaHtmlFileName);
            hasSchemaHtml = schemaHtmlFile.exists();
            schemaHtmlPath = "./" + schemaHtmlFileName; // current directory only supported
        }
        optionMap.put("hasSchemaHtml", hasSchemaHtml);
        if (hasSchemaHtml) {
            optionMap.put("schemaHtmlPath", schemaHtmlPath);
        }
    }

    protected void doSetupContentsAction(Map<String, Object> optionMap) {
        final DfLastaFluteProperties prop = getLastaFluteProperties();
        final DfLastaDocContentsActionMap actionMap = prop.getLastaDocContentsMap().getActionMap();
        optionMap.put("isSuppressDescriptionInList", actionMap.isSuppressDescriptionInList());
        optionMap.put("isSuppressAuthorInList", actionMap.isSuppressAuthorInList());
    }

    // -----------------------------------------------------
    //                                            Lasta Info
    //                                            ----------
    public static class DfLastaFlutePhysicalHolder {

        protected final List<File> actionList = new ArrayList<File>();

        public List<File> getActionList() {
            return actionList;
        }

        public void addAction(File action) {
            actionList.add(action);
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return DfBuildProperties.getInstance().getDocumentProperties();
    }

    protected DfLastaFluteProperties getLastaFluteProperties() {
        return DfBuildProperties.getInstance().getLastaFluteProperties();
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
