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

import java.util.Map;
import java.util.Properties;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/17 Friday)
 */
public final class DfSqlLogRegistryProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSqlLogRegistryProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                         sqlLogRegistryDefinitionMap
    //                                                         ===========================
    public static final String KEY_sqlLogRegistryDefinitionMap = "sqlLogRegistryDefinitionMap";
    protected Map<String, Object> _sqlLogRegistryDefinitionMap;

    protected Map<String, Object> getSqlLogRegistryDefinitionMap() { // It's closet!
        if (_sqlLogRegistryDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_sqlLogRegistryDefinitionMap, DEFAULT_EMPTY_MAP);
            _sqlLogRegistryDefinitionMap = newLinkedHashMap();
            _sqlLogRegistryDefinitionMap.putAll(map);
        }
        return _sqlLogRegistryDefinitionMap;
    }

    public boolean isValid() {
        String value = (String) getSqlLogRegistryDefinitionMap().get("valid");
        if (value == null || value.trim().length() == 0) {
            return false;
        }
        return value.trim().equalsIgnoreCase("true");
    }

    public int getLimitSize() {
        String value = (String) getSqlLogRegistryDefinitionMap().get("limitSize");
        if (value == null || value.trim().length() == 0) {
            return 3; // as Default
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            String msg = "The limitSize of sqlLogRegistryDefinitionMap should be number:";
            msg = msg + " limitSize=" + value;
            throw new IllegalStateException(msg, e);
        }
    }
}