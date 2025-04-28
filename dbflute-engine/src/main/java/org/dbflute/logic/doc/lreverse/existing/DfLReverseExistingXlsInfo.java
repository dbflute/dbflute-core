/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.existing;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 */
public class DfLReverseExistingXlsInfo implements DfLReverseExistingFileInfo {

    protected final Map<File, List<String>> _existingXlsTableListMap;
    protected final Map<String, List<File>> _tableAllExistingXlsListMap;
    protected final Map<String, File> _tableFirstExistingXlsMap;

    public DfLReverseExistingXlsInfo(Map<File, List<String>> existingXlsTableListMap, Map<String, List<File>> tableAllExistingXlsListMap,
            Map<String, File> tableFirstExistingXlsMap) {
        _existingXlsTableListMap = existingXlsTableListMap;
        _tableAllExistingXlsListMap = tableAllExistingXlsListMap;
        _tableFirstExistingXlsMap = tableFirstExistingXlsMap;
    }

    /**
     * @return The map of existing xls file's on-file table names. (NotNull)
     */
    public Map<File, List<String>> getExistingXlsTableListMap() {
        return _existingXlsTableListMap;
    }

    /**
     * @return The map of on-file table name's all existing xls files. (NotNull)
     */
    public Map<String, List<File>> getTableAllExistingXlsListMap() {
        return _tableAllExistingXlsListMap;
    }

    /**
     * @return The map of on-file table name's first existing xls file. (NotNull)
     */
    public Map<String, File> getTableFirstExistingXlsMap() {
        return _tableFirstExistingXlsMap;
    }

    @Override
    public Map<File, List<String>> getExistingFileTableListMap() {
        return getExistingXlsTableListMap();
    }

    @Override
    public Map<String, List<File>> getTableAllExistingFileListMap() {
        return getTableAllExistingXlsListMap();
    }

    @Override
    public Map<String, File> getTableFirstExistingFileMap() {
        return getTableFirstExistingXlsMap();
    }
}
