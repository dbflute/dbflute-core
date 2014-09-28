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
package org.seasar.dbflute.helper.token.file;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 */
public class FileTokenizingHeaderInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _columnNameList = new ArrayList<String>();
    protected String _columnNameRowString;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public void acceptColumnNameList(List<String> columnNameList) {
        clear();
        for (String columnName : columnNameList) {
            addColumnName(columnName);
        }
    }

    public boolean isEmpty() {
        return _columnNameList.isEmpty();
    }

    public void clear() {
        _columnNameList.clear();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<String> getColumnNameList() {
        return _columnNameList;
    }

    public void addColumnName(String columnName) {
        _columnNameList.add(columnName);
    }

    public String getColumnNameRowString() {
        return _columnNameRowString;
    }

    public void setColumnNameRowString(String columnNameRowString) {
        _columnNameRowString = columnNameRowString;
    }
}
