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

/**
 * @author jflute
 * @since 0.9.8.2 (2011/04/17 Sunday)
 */
public class DfOutsideSqlLocation {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _projectName;
    protected final String _sqlDirectory;
    protected final String _sql2EntityOutputDirectory;
    protected final boolean _sqlAp;
    protected final boolean _suppressDirectoryCheck;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOutsideSqlLocation(String projectName, String sqlDirectory, String sql2EntityOutputDirectory,
            boolean sqlAp, boolean suppressDirectoryCheck) {
        _projectName = projectName;
        _sqlDirectory = sqlDirectory;
        _sql2EntityOutputDirectory = sql2EntityOutputDirectory;
        _sqlAp = sqlAp;
        _suppressDirectoryCheck = suppressDirectoryCheck;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProjectName() {
        return _projectName;
    }

    public String getSqlDirectory() {
        return _sqlDirectory;
    }

    public String getSql2EntityOutputDirectory() {
        return _sql2EntityOutputDirectory;
    }

    public boolean isSqlAp() {
        return _sqlAp;
    }

    public boolean isSuppressDirectoryCheck() {
        return _suppressDirectoryCheck;
    }
}
