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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dbflute.helper.filesystem.FileTextIO;
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
    protected final String _gapileDirectory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfGapileClassReflector(String outputDirectory, String packageBase, DfLanguageClassPackage classPackage, String gapileDirectory) {
        _outputDirectory = outputDirectory;
        _packageBase = packageBase;
        _classPackage = classPackage;
        _gapileDirectory = gapileDirectory;
    }

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    public void reflect() {
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
        moveBsClassToGapile(mainBasePath, gapileBasePath, "cbean/bs");
        moveBsClassToGapile(mainBasePath, gapileBasePath, "cbean/cq/bs");
        moveBsClassToGapile(mainBasePath, gapileBasePath, "cbean/cq/ciq");
        moveBsClassToGapile(mainBasePath, gapileBasePath, "cbean/nss");
        copyExClassToGapile(mainBasePath, gapileBasePath, "cbean");
        copyExClassToGapile(mainBasePath, gapileBasePath, "exbhv");
        copyExClassToGapile(mainBasePath, gapileBasePath, "exentity");
    }

    // ===================================================================================
    //                                                                     Move Base Class
    //                                                                     ===============
    protected void moveBsClassToGapile(String mainBasePath, String gapileBasePath, String resourcePath) {
        final File currentDir = new File(mainBasePath + "/" + resourcePath);
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);
        if (currentDir.exists()) {
            completelyDeleteDirIfExists(gapileDir);
            final File parentDir = gapileDir.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            _log.info("...Moving {} to gapile directory: {}", resourcePath, gapileDir.getPath());
            final boolean renamed = currentDir.renameTo(gapileDir);
            if (!renamed) {
                _log.info("*Failed to move {} to gapile directory: {}", resourcePath, gapileDir.getPath());
            }
        }
    }

    // ===================================================================================
    //                                                                 Copy Extended Class
    //                                                                 ===================
    protected void copyExClassToGapile(String mainBasePath, String gapileBasePath, String resourceDir) {
        final File currentDir = new File(mainBasePath + "/" + resourceDir);
        doCopyToGapile(gapileBasePath, resourceDir, currentDir);
    }

    protected void doCopyToGapile(String gapileBasePath, String resourcePath, File currentDir) {
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
                doCopyToGapile(gapileBasePath, resourcePath + "/" + subDir.getName(), subDir);
            }
        }
        final File gapileDir = new File(gapileBasePath + "/" + resourcePath);
        final File[] mainClassFiles = gatherClassFiles(currentDir);
        final Set<String> existingClassNameSet = new HashSet<>();
        if (mainClassFiles.length > 0) {
            gapileDir.mkdirs();
        }
        _log.info("...Copying {} to gapile directory: {}", resourcePath, gapileDir.getPath());
        for (File mainClassFile : mainClassFiles) {
            existingClassNameSet.add(mainClassFile.getName());
        }
        for (File mainClassFile : mainClassFiles) {
            File gapileClassFile;
            try {
                gapileClassFile = new File(gapileDir.getCanonicalPath() + "/" + mainClassFile.getName());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to get the cannonical path: " + gapileDir, e);
            }
            if (!gapileClassFile.exists()) {
                copyText(mainClassFile, gapileClassFile); // new table
            }
        }
        final File[] deletedGapileClassFiles = gatherClassFile(gapileDir, existingClassNameSet);
        for (File deletedResourceFile : deletedGapileClassFiles) {
            deletedResourceFile.delete(); // old table
        }
    }

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
}
