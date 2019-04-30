/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.twowaysql.pmbean;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The simple implementation of map parameter-bean.
 * @author jflute
 * @param <VALUE> The type of value.
 */
public class SimpleMapPmb<VALUE> implements MapParameterBean<VALUE>, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The map of parameter. (NullAllowed) */
    protected Map<String, VALUE> _parameterMap;

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    public Map<String, VALUE> getParameterMap() {
        initializeParameterMapIfNeeds();
        return _parameterMap;
    }

    /**
     * Add the parameter to the map.
     * @param key The key of parameter. (NotNull)
     * @param value The value of parameter. (NullAllowed)
     */
    public void addParameter(String key, VALUE value) {
        initializeParameterMapIfNeeds();
        _parameterMap.put(key, value);
    }

    protected void initializeParameterMapIfNeeds() {
        if (_parameterMap == null) {
            _parameterMap = new LinkedHashMap<String, VALUE>();
        }
    }

    // ===================================================================================
    //                                                                         Map Element
    //                                                                         ===========
    public int size() {
        return getParameterMap().size();
    }

    public boolean isEmpty() {
        return getParameterMap().isEmpty();
    }

    public Collection<VALUE> values() {
        return getParameterMap().values();
    }
}
