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
package org.seasar.dbflute.properties;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.6.9 (2008/04/11 Friday)
 */
public final class DfRefreshProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefreshProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    protected Map<String, Object> refreshDefinitionMap;

    protected Map<String, Object> getRefreshDefinitionMap() {
        if (refreshDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.refreshDefinitionMap", DEFAULT_EMPTY_MAP);
            refreshDefinitionMap = newLinkedHashMap();
            refreshDefinitionMap.putAll(map);
        }
        return refreshDefinitionMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasRefreshDefinition() {
        return !getRefreshDefinitionMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Detail Property
    //                                                                     ===============
    public List<String> getProjectNameList() {
        final String prop = getRefreshProperty("projectName");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(prop)) {
            return DfStringUtil.splitListTrimmed(prop, "/");
        } else {
            return DfCollectionUtil.emptyList();
        }
    }

    public String getRequestUrl() {
        final String prop = getRefreshProperty("requestUrl");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(prop)) {
            return prop;
        } else {
            return null;
        }
    }

    protected String getRefreshPropertyIfNullEmpty(String key) {
        final String value = getRefreshProperty(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getRefreshProperty(String key) {
        return (String) getRefreshDefinitionMap().get(key);
    }
}