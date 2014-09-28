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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 */
public class DfPrimaryKeyMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, String> _primaryKeyMap = new LinkedHashMap<String, String>();

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public boolean containsColumn(String columnName) {
        return _primaryKeyMap.containsKey(columnName);
    }

    public boolean hasPrimaryKey() {
        return !_primaryKeyMap.isEmpty();
    }

    public List<String> getPrimaryKeyList() {
        return new ArrayList<String>(_primaryKeyMap.keySet());
    }

    public void addPrimaryKey(String columnName, String pkName) {
        // basically same PK name at any columns
        // but meta data treats PK name per column 
        _primaryKeyMap.put(columnName, pkName);
    }

    public String getPrimaryKeyName(String columnName) {
        return _primaryKeyMap.get(columnName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return getPrimaryKeyList().toString();
    }
}
