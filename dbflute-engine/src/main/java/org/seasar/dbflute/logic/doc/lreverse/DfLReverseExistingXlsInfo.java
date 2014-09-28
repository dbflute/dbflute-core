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
package org.seasar.dbflute.logic.doc.lreverse;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 */
public class DfLReverseExistingXlsInfo {

    protected final Map<File, List<String>> _existingXlsTableListMap;
    protected final Map<String, File> _tableExistingXlsMap;

    public DfLReverseExistingXlsInfo(Map<File, List<String>> existingXlsTableListMap,
            Map<String, File> tableExistingXlsMap) {
        _existingXlsTableListMap = existingXlsTableListMap;
        _tableExistingXlsMap = tableExistingXlsMap;
    }

    public Map<File, List<String>> getExistingXlsTableListMap() {
        return _existingXlsTableListMap;
    }

    public Map<String, File> getTableExistingXlsMap() {
        return _tableExistingXlsMap;
    }
}
