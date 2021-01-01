/*
 * Copyright 2014-2021 the original author or authors.
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
import java.util.LinkedHashSet;
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
    protected static final File[] EMPTY_FILES = new File[] {};

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
        final Set<String> cbeanBaseSubSet = prepareCBeanBaseSubSet();
        for (String cbeanBaseSub : cbeanBaseSubSet) {
            moveBsClassToGapile(mainBasePath, gapileBasePath, cbPkg + "/" + cbeanBaseSub);
        }

        final Set<String> baseSubSet = cbeanBaseSubSet; // only cbean has both base and extended classes
        moveExClassToGapile(mainBasePath, gapileBasePath, cbPkg, baseSubSet);
        moveExClassToGapile(mainBasePath, gapileBasePath, _classPackage.getExtendedBehaviorPackage(), baseSubSet);
        moveExClassToGapile(mainBasePath, gapileBasePath, _classPackage.getExtendedEntityPackage(), baseSubSet);
        show("==========/");
    }

    protected Set<String> prepareCBeanBaseSubSet() { // #hope resolve by language
        return new LinkedHashSet<String>(Arrays.asList("bs", "cq/bs", "cq/ciq", "nss"));
    }

    // ===================================================================================
    //                                                                     Move Base Class
    //                                                                     ===============
    protected void moveBsClassToGapile(String mainBasePath, String gapileBasePath, String resourcePath) {
        final File mainDir = new File(mainBasePath + "/" + resourcePath);
        doMoveBsClassToGapile(gapileBasePath, resourcePath, mainDir);
    }

    protected void doMoveBsClassToGapile(String gapileBasePath, String resourcePath, File mainDir) {
        if (!mainDir.exists()) { // e.g. bsbhv of first sql2entity
            return;
        }
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);
        if (hasJustBelowFile(mainDir)) { // to avoid deleting e.g. behaviors when sql2entity
            if (gapileDir.exists()) {
                final File[] gapileClassFiles = gatherJustBelowFiles(gapileDir); // contains e.g. .gitignore
                for (File gapileClassFile : gapileClassFiles) {
                    gapileClassFile.delete();
                }
            } else {
                gapileDir.mkdirs();
            }
            show("...Moving base " + resourcePath + " to gapile directory.");
            // quit directory renaming because of deleting sql2entity classes
            //final boolean renamed = currentDir.renameTo(gapileDir);
            final File[] mainClassFiles = gatherJustBelowFiles(mainDir); // contains e.g. .gitignore
            for (File mainClassFile : mainClassFiles) {
                final File gapileClassFile = new File(gapileDir.getPath() + "/" + mainClassFile.getName());
                final boolean renamed = mainClassFile.renameTo(gapileClassFile);
                if (!renamed) {
                    show("*Failed to move " + resourcePath + " to gapile file: " + gapileClassFile.getPath());
                }
            }
        }
        // e.g. generate (cbean/bs), sql2entity (bsbhv/pmbean)
        final File[] subDirs = gatherSubDirs(mainDir);
        for (File subDir : subDirs) {
            final String subResourcePath = resourcePath + "/" + subDir.getName();
            doMoveBsClassToGapile(gapileBasePath, subResourcePath, subDir); // recursive call
        }
        final boolean deleted = mainDir.delete(); // basically success
        if (!deleted) {
            show("*Failed to delete " + resourcePath + " of main directory: " + mainDir.getPath());
        }
        // give up when sql2entity:
        // cannot be reflected when all outsideSqls are deleted
        // because main process itself cannot delete them
    }

    // ===================================================================================
    //                                                                 Move Extended Class
    //                                                                 ===================
    protected void moveExClassToGapile(String mainBasePath, String gapileBasePath, String resourceDir, Set<String> baseSubSet) {
        final File mainDir = new File(mainBasePath + "/" + resourceDir);
        doMoveToGapile(gapileBasePath, resourceDir, mainDir, baseSubSet);
    }

    protected void doMoveToGapile(String gapileBasePath, String resourcePath, File mainDir, Set<String> baseSubSet) {
        if (!mainDir.exists()) { // e.g. cbean/bs, cbean/cq/bs (already moved here)
            return;
        }
        if (baseSubSet.contains(resourcePath)) { // also e.g. cbean/bs, cbean/cq/bs (just in case)
            return;
        }
        final File[] subDirs = gatherSubDirs(mainDir); // e.g. cbean/cq, exbhv/pmbean
        for (File subDir : subDirs) {
            final String subResourcePath = resourcePath + "/" + subDir.getName();
            doMoveToGapile(gapileBasePath, subResourcePath, subDir, baseSubSet); // recursive call
        }
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);

        // class files only because extended area is developer world so keep other files
        final File[] mainClassFiles = gatherClassFiles(mainDir);
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
        msgSb.append("...Moving extended ").append(resourcePath).append(" to gapile directory");
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

    protected boolean hasClassFile(File dir) {
        final String ext = getClassExt();
        final File[] programFile = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(ext);
            }
        });
        return programFile != null && programFile.length > 0;
    }

    protected boolean hasJustBelowFile(File dir) {
        final File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        return files != null && files.length > 0;
    }

    protected File[] gatherClassFiles(File dir) {
        final String ext = getClassExt();
        final File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(ext);
            }
        });
        return files != null ? files : EMPTY_FILES;
    }

    protected File[] gatherClassFile(File dir, Set<String> existingClassNameSet) {
        final String ext = getClassExt();
        final File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(ext) && !existingClassNameSet.contains(name);
            }
        });
        return files != null ? files : EMPTY_FILES;
    }

    protected String getClassExt() {
        return "." + _grammar.getClassFileExtension();
    }

    protected File[] gatherJustBelowFiles(File dir) {
        final File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        return files != null ? files : EMPTY_FILES;
    }

    private File[] gatherSubDirs(File currentDir) {
        final File[] dirs = currentDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        return dirs != null ? dirs : EMPTY_FILES;
    }

    protected void show(String msg) {
        _log.info(msg);
    }
}
