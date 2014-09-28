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
package org.seasar.dbflute.infra.core.logic;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/30 Saturday)
 */
public class DfSchemaResourceFinder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<String> _prefixSet = new LinkedHashSet<String>(4);
    protected final Set<String> _suffixSet = new LinkedHashSet<String>(4);
    protected boolean _oneLevelNested;

    // ===================================================================================
    //                                                                                Find
    //                                                                                ====
    public List<File> findResourceFileList(String targetDir) {
        final File baseDir = new File(targetDir);
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (matchName(name, _prefixSet, false) && matchName(name, _suffixSet, true)) {
                    return true;
                }
                return false;
            }
        };

        // order by file name ascend
        final Comparator<File> fileNameAscComparator = new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        final TreeSet<File> treeSet = new TreeSet<File>(fileNameAscComparator);

        final List<File> resourceFileList;
        final String[] targetList = baseDir.list(filter);
        if (targetList != null) {
            for (String targetFileName : targetList) {
                final String targetFilePath = targetDir + "/" + targetFileName;
                treeSet.add(new File(targetFilePath));
            }
            resourceFileList = new ArrayList<File>(treeSet);

            // searching one-level nested files
            if (_oneLevelNested) {
                final File[] listDirs = baseDir.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
                if (listDirs != null) {
                    for (File dir : listDirs) {
                        final String nestedDir = targetDir + "/" + dir.getName();
                        final List<File> nestedFileList = findResourceFileList(nestedDir);
                        resourceFileList.addAll(nestedFileList);
                    }
                }
            }
        } else {
            resourceFileList = new ArrayList<File>();
        }
        return resourceFileList;
    }

    protected boolean matchName(String name, Set<String> set, boolean suffix) {
        if (set.isEmpty()) {
            return true;
        }
        for (String key : set) {
            if (suffix ? name.endsWith(key) : name.startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    public void addPrefix(String prefix) {
        if (prefix != null) {
            _prefixSet.add(prefix);
        }
    }

    public void addSuffix(String suffix) {
        if (suffix != null) {
            _suffixSet.add(suffix);
        }
    }

    public void containsOneLevelNested() {
        _oneLevelNested = true;
    }
}
