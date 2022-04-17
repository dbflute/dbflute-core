/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter.line;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataFirstLineInfo {

    protected final List<String> _columnNameList;
    protected final boolean _quotated;

    public DfDelimiterDataFirstLineInfo(List<String> columnNameList, boolean quotated) {
        _columnNameList = columnNameList;
        _quotated = quotated;
    }

    public List<String> getColumnNameToLowerList() {
        final List<String> ls = new ArrayList<String>();
        for (String columnName : _columnNameList) {
            ls.add(columnName.toLowerCase());
        }
        return ls;
    }

    public List<String> getColumnNameList() {
        return _columnNameList;
    }

    public boolean isQuotated() {
        return _quotated;
    }
}
