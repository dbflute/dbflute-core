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
import java.util.Map.Entry;

import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.7 (2023/02/02 at roppongi japanese)
 */
public class DfLReverseExistingTsvInfo implements DfLReverseExistingFileInfo {

    protected final Map<File, String> _existingTsvTableMap; // not null
    protected final Map<String, List<File>> _tableAllExistingTsvListMap; // not null
    protected final Map<String, File> _tableFirstExistingTsvListMap; // not null

    public DfLReverseExistingTsvInfo(Map<File, String> existingTsvTableMap, Map<String, List<File>> tableAllExistingTsvListMap,
            Map<String, File> tableFirstExistingTsvListMap) {
        _existingTsvTableMap = existingTsvTableMap;
        _tableAllExistingTsvListMap = tableAllExistingTsvListMap;
        _tableFirstExistingTsvListMap = tableFirstExistingTsvListMap;
    }

    /**
     * @return The map of existing TSV file's on-file table name. (NotNull)
     */
    public Map<File, String> getExistingTsvTableMap() {
        return _existingTsvTableMap;
    }

    /**
     * @return The map of on-file table name's all existing TSV files. (NotNull)
     */
    public Map<String, List<File>> getTableAllExistingTsvListMap() {
        return _tableAllExistingTsvListMap;
    }

    /**
     * @return The map of on-file table name's first existing TSV file. (NotNull)
     */
    public Map<String, File> getTableFirstExistingTsvMap() {
        return _tableFirstExistingTsvListMap;
    }

    @Override
    public Map<File, List<String>> getExistingFileTableListMap() {
        final Map<File, List<String>> tableMap = DfCollectionUtil.newLinkedHashMap();
        for (Entry<File, String> entry : getExistingTsvTableMap().entrySet()) {
            final List<String> elementList = DfCollectionUtil.newArrayList();
            elementList.add(entry.getValue());
            tableMap.put(entry.getKey(), elementList); // adapting
        }
        return tableMap;
    }

    @Override
    public Map<String, List<File>> getTableAllExistingFileListMap() {
        return getTableAllExistingTsvListMap();
    }

    @Override
    public Map<String, File> getTableFirstExistingFileMap() {
        return getTableFirstExistingTsvMap();
    }
}
