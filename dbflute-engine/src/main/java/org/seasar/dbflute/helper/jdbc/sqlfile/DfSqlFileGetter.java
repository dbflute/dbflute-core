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
package org.seasar.dbflute.helper.jdbc.sqlfile;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 */
public class DfSqlFileGetter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected FileFilter _sqlFileFileter;

    protected FileFilter _directoryOnlyFilter;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public List<File> getSqlFileList(String sqlDirectory) throws FileNotFoundException {
        final List<File> fileList = new ArrayList<File>();
        final File fileDir = toFileDir(sqlDirectory);
        registerFile(fileList, fileDir);
        return fileList;
    }

    protected File toFileDir(String sqlDirectory) throws FileNotFoundException {
        final File dir = new File(sqlDirectory);
        if (!dir.exists()) {
            String msg = "The sqlDirectory does not exist: " + dir;
            throw new FileNotFoundException(msg);
        }
        if (!dir.isDirectory()) {
            String msg = "The sqlDirectory should be directory. but file...: " + dir;
            throw new FileNotFoundException(msg);
        }
        return dir;
    }

    protected void registerFile(List<File> fileList, File fileDir) {
        final FileFilter sqlFileFileter;
        if (_sqlFileFileter != null) {
            sqlFileFileter = _sqlFileFileter;
        } else {
            sqlFileFileter = createDefaultSqlFileFileFilter();
        }
        final File[] sqlFiles = fileDir.listFiles(sqlFileFileter);
        for (final File sqlFile : sqlFiles) {
            fileList.add(sqlFile);
        }
        final FileFilter directoryOnlyFilter;
        if (_sqlFileFileter != null) {
            directoryOnlyFilter = _directoryOnlyFilter;
        } else {
            directoryOnlyFilter = createDefaultDirectoryOnlyFileFilter();
        }
        final File[] directories = fileDir.listFiles(directoryOnlyFilter);
        for (final File subdir : directories) {
            registerFile(fileList, subdir);
        }
    }

    // ===================================================================================
    //                                                                  Default FileFilter
    //                                                                  ==================
    protected FileFilter createDefaultSqlFileFileFilter() {
        return new FileFilter() {
            public boolean accept(File file) {
                return acceptSqlFile(file);
            }
        };
    }

    protected boolean acceptSqlFile(File file) {
        return file.getName().toLowerCase().endsWith(".sql");
    }

    protected FileFilter createDefaultDirectoryOnlyFileFilter() {
        return new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public FileFilter getSqlFileFileter() {
        return _sqlFileFileter;
    }

    public void setSqlFileFileter(FileFilter sqlFileFileter) {
        this._sqlFileFileter = sqlFileFileter;
    }
}
