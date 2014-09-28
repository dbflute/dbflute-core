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
public final class DfHibernateProperties extends DfAbstractHelperProperties {

    // 
    // ...Making (2009/07/11 Saturday)
    // 

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfHibernateProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    protected Map<String, Object> _hibernateDefinitionMap;

    protected Map<String, Object> getHibernateDefinitionMap() { // It's closet!
        if (_hibernateDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.hibernateDefinitionMap", DEFAULT_EMPTY_MAP);
            _hibernateDefinitionMap = newLinkedHashMap();
            _hibernateDefinitionMap.putAll(map);
        }
        return _hibernateDefinitionMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasHibernateDefinition() {
        return !getHibernateDefinitionMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Detail Property
    //                                                                     ===============
    public String getManyToOneFetch() {
        return getEntityPropertyIfNullEmpty("manyToOneFetch");
    }

    public String getOneToOneFetch() {
        return getEntityPropertyIfNullEmpty("oneToOneFetch");
    }

    public String getOneToManyFetch() {
        return getEntityPropertyIfNullEmpty("oneToManyFetch");
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getEntityPropertyRequired(String key) {
        final String value = getEntityProperty(key);
        if (value == null || value.trim().length() == 0) {
            String msg = "The property '" + key + "' should not be null or empty:";
            msg = msg + " hibernateDefinitionMap=" + getHibernateDefinitionMap();
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
        final String value = (String) getHibernateDefinitionMap().get(key);
        return value;
    }
}