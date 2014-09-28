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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileGetter;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;

/**
 * @author jflute
 * @since 0.7.9 (2008/08/29 Friday)
 */
public class DfOutsideSqlCollector {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfOutsideSqlCollector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _suppressDirectoryCheck;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOutsideSqlCollector() {
    }

    // ===================================================================================
    //                                                                             Collect
    //                                                                             =======
    /**
     * Collect outside-SQL containing its file info as pack.
     * @return The pack object for outside-SQL files. (NotNull)
     */
    public DfOutsideSqlPack collectOutsideSql() {
        final DfOutsideSqlPack outsideSqlPack = new DfOutsideSqlPack();
        final List<DfOutsideSqlLocation> sqlDirectoryList = getSqlDirectoryList();
        for (DfOutsideSqlLocation sqlLocation : sqlDirectoryList) {
            final String sqlDirectory = sqlLocation.getSqlDirectory();
            if (existsSqlDir(sqlDirectory)) {
                try {
                    outsideSqlPack.addAll(collectSqlFile(sqlDirectory, sqlLocation));
                } catch (FileNotFoundException e) {
                    String msg = "Failed to collect SQL files at the directory: " + sqlDirectory;
                    throw new IllegalStateException(msg, e);
                }
                handleSecondaryDirectory(outsideSqlPack, sqlLocation, sqlDirectory, false);
            } else {
                final boolean suppressCheck = _suppressDirectoryCheck || sqlLocation.isSuppressDirectoryCheck();
                final boolean foundSecondaryDirectory = handleSecondaryDirectory(outsideSqlPack, sqlLocation,
                        sqlDirectory, suppressCheck);
                if (!foundSecondaryDirectory && !suppressCheck) { // means both primary and secondary directory
                    String msg = "The sqlDirectory does not exist: " + sqlDirectory;
                    throw new IllegalStateException(msg);
                }
            }
        }
        return outsideSqlPack;
    }

    protected List<DfOutsideSqlLocation> getSqlDirectoryList() {
        final DfOutsideSqlProperties prop = getOutsideSqlProperties();
        return prop.getSqlLocationList();
    }

    protected boolean existsSqlDir(String sqlDirPath) {
        return new File(sqlDirPath).exists();
    }

    protected List<DfOutsideSqlFile> collectSqlFile(String realSqlDirectory, DfOutsideSqlLocation sqlLocation)
            throws FileNotFoundException {
        final List<File> sqlFileList = createSqlFileGetter().getSqlFileList(realSqlDirectory);
        final List<DfOutsideSqlFile> outsideSqlList = new ArrayList<DfOutsideSqlFile>();
        for (File sqlFile : sqlFileList) {
            outsideSqlList.add(new DfOutsideSqlFile(sqlFile, sqlLocation));
        }
        return outsideSqlList;
    }

    protected DfSqlFileGetter createSqlFileGetter() {
        final DfLanguageDependency dependencyInfo = getBasicProperties().getLanguageDependency();
        return new DfSqlFileGetter() {
            @Override
            protected boolean acceptSqlFile(File file) {
                if (!dependencyInfo.isCompileTargetFile(file)) {
                    return false;
                }
                return super.acceptSqlFile(file);
            }
        };
    }

    protected boolean handleSecondaryDirectory(DfOutsideSqlPack outsideSqlPack, DfOutsideSqlLocation sqlLocation,
            String sqlDirectory, boolean checkNotFound) {
        final DfBasicProperties basicProp = getBasicProperties();
        final DfLanguageDependency lang = basicProp.getLanguageDependency();
        final String secondaryDirectory = lang.convertToSecondaryOutsideSqlDirectory(sqlDirectory);
        final boolean foundSecondaryDirectory;
        if (secondaryDirectory != null && !sqlDirectory.equals(secondaryDirectory)) {
            try {
                outsideSqlPack.addAll(collectSqlFile(secondaryDirectory, sqlLocation));
            } catch (FileNotFoundException e) {
                if (checkNotFound) {
                    String msg = "The sqlDirectory does not exist: " + secondaryDirectory;
                    throw new IllegalStateException(msg);
                } else {
                    _log.info("Not found sql directory on resources: " + secondaryDirectory);
                }
            }
            foundSecondaryDirectory = true;
        } else {
            foundSecondaryDirectory = false;
        }
        return foundSecondaryDirectory;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void suppressDirectoryCheck() {
        _suppressDirectoryCheck = true;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }
}
