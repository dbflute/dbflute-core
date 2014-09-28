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

import java.util.List;
import java.util.Map;

/**
 * The row resource of file-making. <br />
 * You can set one record info to this resource as list of string or map of string with header info. <br />
 * Null resource or null data or empty data means the end of data.
 * @author jflute
 */
public class FileMakingRowResource {

    // =====================================================================================
    //                                                                             Attribute
    //                                                                             =========
    // either required, both null means end of data
    protected List<String> _valueList;
    protected Map<String, String> _valueMap;

    // =====================================================================================
    //                                                                           Constructor
    //                                                                           ===========
    public FileMakingRowResource() {
    }

    // =====================================================================================
    //                                                                      Accept ValueList
    //                                                                      ================
    /**
     * Accept the list of value as one row. (priority 1)
     * @param valueList The list of value. (NullAllowed, EmptyAllowed: if null or empty, means end of data)
     * @return this. (NotNull)
     */
    public FileMakingRowResource acceptRow(List<String> valueList) {
        _valueList = valueList;
        return this;
    }

    /**
     * Accept the map of column-key value as one row. (priority 2)
     * @param valueMap The map of column-key value. (NullAllowed, EmptyAllowed: if null or empty, means end of data)
     * @return this. (NotNull)
     */
    public FileMakingRowResource acceptRow(Map<String, String> valueMap) {
        _valueMap = valueMap;
        return this;
    }

    // =====================================================================================
    //                                                                       Resource Status
    //                                                                       ===============
    /**
     * Does it have row data? (value list or value map exists or not)
     * @return The determination, true or false.
     */
    public boolean hasRowData() {
        return hasValueList() || hasValueMap();
    }

    /**
     * Does it have value list?
     * @return The determination, true or false.
     */
    public boolean hasValueList() {
        return _valueList != null && !_valueList.isEmpty();
    }

    /**
     * Does it have column-key value map?
     * @return The determination, true or false.
     */
    public boolean hasValueMap() {
        return _valueMap != null && !_valueMap.isEmpty();
    }

    /**
     * Clear the resources for instance recycle. (called by writing process per one line)
     */
    public void clear() {
        _valueList = null;
        _valueMap = null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String disp;
        if (hasValueList()) {
            disp = "{" + _valueList + "}";
        } else if (hasValueMap()) {
            disp = "{" + _valueList + "}";
        } else {
            disp = "{null}";
        }
        return disp;
    }

    // =====================================================================================
    //                                                                              Accessor
    //                                                                              ========
    public List<String> getValueList() {
        return _valueList;
    }

    public Map<String, String> getValueMap() {
        return _valueMap;
    }
}
