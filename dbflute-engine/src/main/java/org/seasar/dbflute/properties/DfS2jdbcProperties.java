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
 */
public final class DfS2jdbcProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfS2jdbcProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    protected Map<String, Object> _s2jdbcDefinitionMap;

    protected Map<String, Object> getS2JdbcDefinitionMap() { // It's closet!
        if (_s2jdbcDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.s2jdbcDefinitionMap", DEFAULT_EMPTY_MAP);
            _s2jdbcDefinitionMap = newLinkedHashMap();
            _s2jdbcDefinitionMap.putAll(map);
        }
        return _s2jdbcDefinitionMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasS2jdbcDefinition() {
        return !getS2JdbcDefinitionMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Detail Property
    //                                                                     ===============
    public String getBaseEntityPackage() {
        return getEntityPropertyRequired("baseEntityPackage");
    }

    public String getExtendedEntityPackage() {
        return getEntityPropertyRequired("extendedEntityPackage");
    }

    public String getBaseEntityPrefix() {
        return getEntityPropertyIfNullEmpty("baseEntityPrefix");
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public boolean isSuppressPublicField() {
        final String prop = getEntityProperty("isSuppressPublicField");
        return prop != null && prop.trim().equalsIgnoreCase("true");
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getEntityPropertyRequired(String key) {
        final String value = getEntityProperty(key);
        if (value == null || value.trim().length() == 0) {
            String msg = "The property '" + key + "' should not be null or empty:";
            msg = msg + " s2jdbcDefinitionMap=" + getS2JdbcDefinitionMap();
            throw new IllegalStateException(msg);
        }
        return value;
    }

    protected String getEntityPropertyIfNullEmpty(String key) {
        final String value = getEntityProperty(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getEntityProperty(String key) {
        return (String) getS2JdbcDefinitionMap().get(key);
    }
}