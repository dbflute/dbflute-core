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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 * @since 0.9.8.2 (2011/04/17 Sunday)
 */
public class DfOutsideSqlPack {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<File, DfOutsideSqlFile> _sqlFileMap = new LinkedHashMap<File, DfOutsideSqlFile>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOutsideSqlPack() {
    }

    // ===================================================================================
    //                                                                      Basic Accessor
    //                                                                      ==============
    public DfOutsideSqlFile getOutsideSqlFile(File physicalFile) {
        return _sqlFileMap.get(physicalFile);
    }

    public List<DfOutsideSqlFile> getOutsideSqlFileList() {
        return new ArrayList<DfOutsideSqlFile>(_sqlFileMap.values());
    }

    public List<File> getPhysicalFileList() {
        return new ArrayList<File>(_sqlFileMap.keySet());
    }

    // ===================================================================================
    //                                                                      Collection I/F
    //                                                                      ==============
    public void add(DfOutsideSqlFile outsideSqlFile) {
        _sqlFileMap.put(outsideSqlFile.getPhysicalFile(), outsideSqlFile);
    }

    public void addAll(List<DfOutsideSqlFile> outsideSqlFileList) {
        for (DfOutsideSqlFile outsideSqlFile : outsideSqlFileList) {
            add(outsideSqlFile);
        }
    }

    public boolean isEmpty() {
        return _sqlFileMap.isEmpty();
    }

    public int size() {
        return _sqlFileMap.size();
    }
}
