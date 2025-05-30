/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.manage.freegen;

import java.util.Map;

/**
 * @author jflute
 */
public class DfFreeGenMapProp {

    protected final Map<String, Object> optionMap;
    protected final Map<String, Map<String, String>> mappingMap;
    protected final Map<String, DfFreeGenRequest> requestMap;

    public DfFreeGenMapProp(Map<String, Object> optionMap, Map<String, Map<String, String>> mappingMap,
            Map<String, DfFreeGenRequest> requestMap) {
        this.optionMap = optionMap;
        this.mappingMap = mappingMap;
        this.requestMap = requestMap;
    }

    public Map<String, Object> getOptionMap() {
        return optionMap;
    }

    public Map<String, Map<String, String>> getMappingMap() {
        return mappingMap;
    }

    public Map<String, DfFreeGenRequest> getRequestMap() {
        return requestMap;
    }
}
