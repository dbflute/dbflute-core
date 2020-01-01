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
package org.dbflute.logic.generate.deletefile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.7.0 (2008/05/07 Wednesday)
 */
public class DfOldTableClassDeletor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfOldTableClassDeletor.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _outputDirectory;
    protected final DfPackagePathHandler _packagePathHandler;
    protected final List<String> _packagePathList = new ArrayList<String>();
    protected String _classPrefix;
    protected String _classSuffix;
    protected String _classExtension;
    protected Set<String> _notDeleteClassNameSet;

    // -----------------------------------------------------
    //                                         Gapile Option
    //                                         -------------
    protected String _mainOutputDirectory; // null allowed
    protected String _gapileDirectory; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOldTableClassDeletor(String outputDirectory, DfPackagePathHandler packagePathHandler) {
        _outputDirectory = outputDirectory;
        _packagePathHandler = packagePathHandler;
    }

    // ===================================================================================
    //                                                                              Delete
    //                                                                              ======
    public List<String> deleteOldTableClass() {
        final List<String> deletedClassNameList = new ArrayList<String>();
        for (String packagePath : _packagePathList) {
            final List<File> files = findPackageFileList(packagePath, _classPrefix, _classSuffix);
            for (File file : files) {
                final String name = file.getName();
                final String nameWithoutExt = name.substring(0, name.lastIndexOf(_classExtension));
                if (_notDeleteClassNameSet.contains(nameWithoutExt)) {
                    continue;
                }
                deletedClassNameList.add(nameWithoutExt);
                if (file.exists()) {
                    file.delete();
                } else {
                    String msg = "Not found the old file: " + file;
                    _log.warn(msg); // basically framework error
                }
            }
        }
        return deletedClassNameList;
    }

    /**
     * @param packagePath The path of package. (NotNull)
     * @param classPrefix The prefix of classes. (NotNull)
     * @param classSuffix The suffix of classes. (NullAllowed)
     * @return The read-only list of package files. (NotNull)
     */
    protected List<File> findPackageFileList(String packagePath, final String classPrefix, final String classSuffix) {
        final String packageAsPath = _packagePathHandler.getPackageAsPath(packagePath);
        final File dir = new File(_outputDirectory + "/" + packageAsPath);
        final FilenameFilter filter = createTargetFileFilter(classPrefix, classSuffix);
        final File[] targetFiles = dir.exists() ? dir.listFiles(filter) : null;
        if (targetFiles != null && targetFiles.length > 0) {
            return Arrays.asList(targetFiles);
        } else {
            if (isUseGapileDirectory()) {
                final File gapileDir = new File(_gapileDirectory + "/" + packageAsPath);
                final File[] gapileFiles = gapileDir.exists() ? gapileDir.listFiles(filter) : null;
                if (gapileFiles != null && gapileFiles.length > 0) {
                    return Arrays.asList(gapileFiles);
                }
            }
            return Collections.emptyList();
        }
    }

    protected FilenameFilter createTargetFileFilter(String classPrefix, String classSuffix) {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (!name.endsWith(_classExtension)) {
                    return false;
                }
                final String nameWithoutExt = name.substring(0, name.lastIndexOf(_classExtension));
                if (!nameWithoutExt.startsWith(classPrefix)) {
                    return false;
                }
                if (classPrefix != null && classPrefix.trim().length() > 0 && !nameWithoutExt.startsWith(classPrefix)) {
                    return false;
                }
                if (classSuffix != null && classSuffix.trim().length() > 0 && !nameWithoutExt.endsWith(classSuffix)) {
                    return false;
                }
                return true;
            }
        };
    }

    protected boolean isUseGapileDirectory() {
        return _mainOutputDirectory != null && _gapileDirectory != null // has gapileDirectory
                && _outputDirectory.equals(_mainOutputDirectory); // main directory
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void addPackagePath(String packagePath) {
        _packagePathList.add(packagePath);
    }

    public void setClassPrefix(String classPrefix) {
        _classPrefix = classPrefix;
    }

    public void setClassSuffix(String classSuffix) {
        _classSuffix = classSuffix;
    }

    public void setClassExtension(String ext) {
        _classExtension = (ext != null && !ext.startsWith(".") ? "." : "") + ext;
    }

    public void setNotDeleteClassNameSet(Set<String> notDeleteClassNameSet) {
        _notDeleteClassNameSet = notDeleteClassNameSet;
    }

    public void setMainOutputDirectory(String mainOutputDirectory) {
        _mainOutputDirectory = mainOutputDirectory;
    }

    public void setGapileDirectory(String gapileDirectory) {
        _gapileDirectory = gapileDirectory;
    }
}
