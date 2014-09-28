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

/**
 * @author jflute
 * @since 0.9.8.2 (2011/04/17 Sunday)
 */
public class DfOutsideSqlFile {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final File _physicalFile;
    protected final DfOutsideSqlLocation _outsideSqlLocation;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOutsideSqlFile(File physicalFile, DfOutsideSqlLocation outsideSqlLocation) {
        _physicalFile = physicalFile;
        _outsideSqlLocation = outsideSqlLocation;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass().isAssignableFrom(obj.getClass())) {
            return _physicalFile.equals(getClass().cast(obj).getPhysicalFile());
        } else if (File.class.isAssignableFrom(obj.getClass())) {
            return _physicalFile.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _physicalFile.hashCode();
    }

    @Override
    public String toString() {
        return _physicalFile.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public File getPhysicalFile() {
        return _physicalFile;
    }

    public String getProjectName() {
        return _outsideSqlLocation.getProjectName();
    }

    public String getSqlDirectory() {
        return _outsideSqlLocation.getSqlDirectory();
    }

    public String getSql2EntityOutputDirectory() {
        return _outsideSqlLocation.getSql2EntityOutputDirectory();
    }

    public boolean isSqlAp() {
        return _outsideSqlLocation.isSqlAp();
    }
}
