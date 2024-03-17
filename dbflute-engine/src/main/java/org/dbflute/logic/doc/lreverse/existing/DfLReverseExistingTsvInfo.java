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
 * @since 1.2.7 (2023/02/02 at roppongi japanese)
 */
public class DfLReverseExistingTsvInfo {

    protected final Map<File, String> _existingTsvTableMap;
    protected final Map<String, List<File>> _tableExistingTsvListMap;

    public DfLReverseExistingTsvInfo(Map<File, String> existingTsvTableMap, Map<String, List<File>> tableExistingTsvListMap) {
        _existingTsvTableMap = existingTsvTableMap;
        _tableExistingTsvListMap = tableExistingTsvListMap;
    }

    public Map<File, String> getExistingTsvTableMap() {
        return _existingTsvTableMap;
    }

    public Map<String, List<File>> getTableExistingTsvListMap() {
        return _tableExistingTsvListMap;
    }
}
