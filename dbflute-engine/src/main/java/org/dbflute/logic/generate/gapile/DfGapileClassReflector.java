/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.generate.gapile;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.2 (2016/08/25 Thursday)
 */
public class DfGapileClassReflector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The _log instance for this class. (NotNull) */
    protected static final Logger _log = LoggerFactory.getLogger(DfGapileClassReflector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _outputDirectory;
    protected final String _packageBase;
    protected final DfLanguageClassPackage _classPackage;
    protected final DfLanguageGrammar _grammar;
    protected final String _gapileDirectory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfGapileClassReflector(String outputDirectory, String packageBase, DfLanguageClassPackage classPackage,
            DfLanguageGrammar grammar, String gapileDirectory) {
        _outputDirectory = outputDirectory;
        _packageBase = packageBase;
        _classPackage = classPackage;
        _grammar = grammar;
        _gapileDirectory = gapileDirectory;
    }

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    public void reflect() {
        show("/===========================================================================");
        show("...Reflecting GenerationGapile classes: " + _gapileDirectory);
        final String outputDirectory = _outputDirectory;
        final String packageBase = _packageBase;
        final String gapileDirectory = _gapileDirectory;
        final String mainBasePath = outputDirectory + "/" + Srl.replace(packageBase, ".", "/");
        final String gapileBasePath = gapileDirectory + "/" + Srl.replace(packageBase, ".", "/");
        final File gapileBaseDir = new File(gapileBasePath);
        if (!gapileBaseDir.exists()) {
            gapileBaseDir.mkdirs();
        }

        moveBsClassToGapile(mainBasePath, gapileBasePath, _classPackage.getBaseCommonPackage());
        moveBsClassToGapile(mainBasePath, gapileBasePath, _classPackage.getBaseBehaviorPackage());
        moveBsClassToGapile(mainBasePath, gapileBasePath, _classPackage.getBaseEntityPackage());
        final String cbPkg = _classPackage.getConditionBeanPackage();
        moveBsClassToGapile(mainBasePath, gapileBasePath, cbPkg + "/bs"); // #hope resolve by language
        moveBsClassToGapile(mainBasePath, gapileBasePath, cbPkg + "/cq/bs");
        moveBsClassToGapile(mainBasePath, gapileBasePath, cbPkg + "/cq/ciq");
        moveBsClassToGapile(mainBasePath, gapileBasePath, cbPkg + "/nss");
        moveExClassToGapile(mainBasePath, gapileBasePath, cbPkg);
        moveExClassToGapile(mainBasePath, gapileBasePath, _classPackage.getExtendedBehaviorPackage());
        moveExClassToGapile(mainBasePath, gapileBasePath, _classPackage.getExtendedEntityPackage());
        show("==========/");
    }

    // ===================================================================================
    //                                                                     Move Base Class
    //                                                                     ===============
    protected void moveBsClassToGapile(String mainBasePath, String gapileBasePath, String resourcePath) {
        final File currentDir = new File(mainBasePath + "/" + resourcePath);
        doMoveBsClassToGapile(gapileBasePath, resourcePath, currentDir);
    }

    protected void doMoveBsClassToGapile(String gapileBasePath, String resourcePath, File currentDir) {
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);
        if (currentDir.exists()) { // exists but no files when e.g. sql2entity
            if (hasProgramFile(currentDir)) {
                completelyDeleteDirIfExists(gapileDir);
                final File parentDir = gapileDir.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                show("...Moving " + resourcePath + " to gapile directory.");
                final boolean renamed = currentDir.renameTo(gapileDir);
                if (!renamed) {
                    show("*Failed to move " + resourcePath + " to gapile directory: " + gapileDir.getPath());
                }
            } else { // e.g. sql2entity, bsbhv/pmbean
                final File[] subDirs = currentDir.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
                if (subDirs != null) {
                    for (File subDir : subDirs) {
                        final String subResourcePath = resourcePath + "/" + subDir.getName();
                        doMoveBsClassToGapile(gapileBasePath, subResourcePath, subDir); // recursive call
                    }
                }
                // give up:
                // lonely empty bsbhv directory for now
                // cannot be reflected all deleted outsideSqls for now (but reflected when generate)
            }
        }
    }

    protected boolean hasProgramFile(File dir) {
        final String ext = "." + _grammar.getClassFileExtension();
        final File[] programFile = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(ext);
            }
        });
        return programFile != null && programFile.length > 0;
    }

    // ===================================================================================
    //                                                                 Move Extended Class
    //                                                                 ===================
    protected void moveExClassToGapile(String mainBasePath, String gapileBasePath, String resourceDir) {
        final File currentDir = new File(mainBasePath + "/" + resourceDir);
        doMoveToGapile(gapileBasePath, resourceDir, currentDir);
    }

    protected void doMoveToGapile(String gapileBasePath, String resourcePath, File currentDir) {
        if (!currentDir.exists()) { // e.g. cbean/bs, cbean/cq/bs (already moved here)
            return;
        }
        final File[] subDirs = currentDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (subDirs != null) {
            for (File subDir : subDirs) {
                final String subResourcePath = resourcePath + "/" + subDir.getName();
                doMoveToGapile(gapileBasePath, subResourcePath, subDir); // recursive call
            }
        }
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);
        final File[] mainClassFiles = gatherClassFiles(currentDir);
        if (mainClassFiles.length > 0) {
            gapileDir.mkdirs();
        }

        final Set<String> existingClassNameSet = new HashSet<>();
        for (File mainClassFile : mainClassFiles) {
            existingClassNameSet.add(mainClassFile.getName());
        }

        final Map<File, File> copiedFileMap = new LinkedHashMap<File, File>();
        for (File mainClassFile : mainClassFiles) {
            final File gapileClassFile = new File(gapileDir.getPath() + "/" + mainClassFile.getName());
            if (!gapileClassFile.exists()) {
                copiedFileMap.put(mainClassFile, gapileClassFile);
            }
        }
        final File[] deletedFiles = gatherClassFile(gapileDir, existingClassNameSet);

        show(buildExNewOldShowMessage(resourcePath, copiedFileMap, deletedFiles));
        for (Entry<File, File> entry : copiedFileMap.entrySet()) {
            copyText(entry.getKey(), entry.getValue()); // new table
        }
        for (File deletedResourceFile : deletedFiles) {
            deletedResourceFile.delete(); // old table
        }
    }

    protected String buildExNewOldShowMessage(String resourcePath, Map<File, File> copiedFileMap, File[] deletedFiles) {
        final String newFilesExp = toExNewOldFilesExp(copiedFileMap.keySet());
        final String oldFilesExp = toExNewOldFilesExp(Arrays.asList(deletedFiles));
        final StringBuilder msgSb = new StringBuilder();
        msgSb.append("...Moving ").append(resourcePath).append(" to gapile directory");
        msgSb.append(": new=").append(copiedFileMap.size()).append(newFilesExp);
        msgSb.append(", old=").append(deletedFiles.length).append(oldFilesExp);
        return msgSb.toString();
    }

    protected String toExNewOldFilesExp(Collection<File> files) {
        if (files.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (File copiedFile : files) {
            if (index >= 2) { // limit
                sb.append(", ...");
                break;
            }
            final String withoutExt = Srl.substringLastFront(copiedFile.getName(), ".");
            sb.append(index > 0 ? ", " : "").append(withoutExt);
            ++index;
        }
        return " (" + sb.toString() + ")";
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected void copyText(File mainClassFile, File gapileClassFile) {
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        String text;
        try {
            text = textIO.read(new FileInputStream(mainClassFile));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to read the main class text: " + mainClassFile, e);
        }
        try {
            textIO.write(new FileOutputStream(gapileClassFile), text);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to write the gapile class text: " + gapileClassFile, e);
        }
    }

    protected File[] gatherClassFiles(File mainDir) {
        final File[] files = mainDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        });
        return files != null ? files : new File[] {};
    }

    protected File[] gatherClassFile(File gapileDir, Set<String> existingClassNameSet) {
        final File[] files = gapileDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java") && !existingClassNameSet.contains(name);
            }
        });
        return files != null ? files : new File[] {};
    }

    protected void completelyDeleteDirIfExists(File dir) {
        if (dir.exists()) { // to override
            final File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        completelyDeleteDirIfExists(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    protected void show(String msg) {
        _log.info(msg);
    }
}
