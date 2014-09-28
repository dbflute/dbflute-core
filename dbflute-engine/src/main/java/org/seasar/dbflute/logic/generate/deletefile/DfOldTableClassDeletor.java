/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.generate.deletefile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;

/**
 * @author jflute
 * @since 0.7.0 (2008/05/07 Wednesday)
 */
public class DfOldTableClassDeletor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfOldClassHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _outputDirectory;
    protected final DfPackagePathHandler _packagePathHandler;
    protected final List<String> _packagePathList = new ArrayList<String>();
    protected String _classPrefix;
    protected String _classSuffix;
    protected String _classExtension;
    protected Set<String> notDeleteClassNameSet;

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
                if (notDeleteClassNameSet.contains(nameWithoutExt)) {
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
     * @return The list of package files. (NotNull)
     */
    protected List<File> findPackageFileList(String packagePath, final String classPrefix, final String classSuffix) {
        final String packageAsPath = _packagePathHandler.getPackageAsPath(packagePath);
        final String dirPath = _outputDirectory + "/" + packageAsPath;
        final File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<File>();
        }
        final FilenameFilter filter = new FilenameFilter() {
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
        final File[] listFiles = dir.listFiles(filter);
        if (listFiles == null || listFiles.length == 0) {
            return new ArrayList<File>();
        }
        return Arrays.asList(listFiles);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void addPackagePath(String packagePath) {
        _packagePathList.add(packagePath);
    }

    protected String getClassPrefix() {
        return _classPrefix;
    }

    public void setClassPrefix(String classPrefix) {
        _classPrefix = classPrefix;
    }

    public String getClassSuffix() {
        return _classSuffix;
    }

    public void setClassSuffix(String classSuffix) {
        _classSuffix = classSuffix;
    }

    public String getClassExtension() {
        return _classExtension;
    }

    public void setClassExtension(String classExtension) {
        if (classExtension != null && !classExtension.startsWith(".")) {
            this._classExtension = "." + classExtension;
        } else {
            this._classExtension = classExtension;
        }
    }

    public Set<String> getNotDeleteClassNameSet() {
        return notDeleteClassNameSet;
    }

    public void setNotDeleteClassNameSet(Set<String> notDeleteClassNameSet) {
        this.notDeleteClassNameSet = notDeleteClassNameSet;
    }
}
