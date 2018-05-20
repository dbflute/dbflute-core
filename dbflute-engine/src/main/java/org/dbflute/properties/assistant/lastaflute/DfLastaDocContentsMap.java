/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.properties.assistant.lastaflute;

import java.util.Map;

import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.1.8 (2018/05/17 Thursday)
 */
public class DfLastaDocContentsMap {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String KEY_lastaDocContentsMap = "lastaDocContentsMap";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _lastafluteMap;
    protected final DfPropertyValueHandler _propertyValueHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaDocContentsMap(Map<String, Object> lastafluteMap, DfPropertyValueHandler propertyValueHandler) {
        _lastafluteMap = lastafluteMap;
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                            Base Map
    //                                                                            ========
    // ; lastaDocContentsMap = map:{
    //     ; actionMap = map:{
    //         ; isSuppressDescriptionInList = true
    //         ; isSuppressAuthorInList = true
    //     }
    // }
    protected Map<String, Object> _lastaDocContentsMap; // cache

    protected Map<String, Object> getLastaDocContentsMap() {
        if (_lastaDocContentsMap != null) {
            return _lastaDocContentsMap;
        }
        final Object obj = _lastafluteMap.get(KEY_lastaDocContentsMap);
        if (obj == null) {
            _lastaDocContentsMap = DfCollectionUtil.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Object> contentsMap = (Map<String, Object>) obj;
            _lastaDocContentsMap = contentsMap;
        }
        return _lastaDocContentsMap;
    }

    // ===================================================================================
    //                                                                          Header Map
    //                                                                          ==========
    protected DfLastaDocContentsHeaderMap _headerMap; // cache

    public DfLastaDocContentsHeaderMap getHeaderMap() {
        if (_headerMap != null) {
            return _headerMap;
        }
        _headerMap = new DfLastaDocContentsHeaderMap(getLastaDocContentsMap(), _propertyValueHandler);
        return _headerMap;
    }

    public static class DfLastaDocContentsHeaderMap {

        protected final Map<String, Object> _lastaDocContentsMap;
        protected final DfPropertyValueHandler _propertyValueHandler;

        protected Map<String, Object> _actionMap; // cache

        public DfLastaDocContentsHeaderMap(Map<String, Object> lastaDocContentsMap, DfPropertyValueHandler propertyValueHandler) {
            _lastaDocContentsMap = lastaDocContentsMap;
            _propertyValueHandler = propertyValueHandler;
        }

        protected Map<String, Object> getHeaderMap() {
            if (_actionMap != null) {
                return _actionMap;
            }
            final Object obj = _lastaDocContentsMap.get("headerMap");
            if (obj == null) {
                _actionMap = DfCollectionUtil.emptyMap();
            } else {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) obj;
                _actionMap = map;
            }
            return _actionMap;
        }

        public boolean isSuppressSchemaHtmlLink() {
            return _propertyValueHandler.isProperty("isSuppressSchemaHtmlLink", false, getHeaderMap());
        }
    }

    // ===================================================================================
    //                                                                          Action Map
    //                                                                          ==========
    protected DfLastaDocContentsActionMap _actionMap; // cache

    public DfLastaDocContentsActionMap getActionMap() {
        if (_actionMap != null) {
            return _actionMap;
        }
        _actionMap = new DfLastaDocContentsActionMap(getLastaDocContentsMap(), _propertyValueHandler);
        return _actionMap;
    }

    public static class DfLastaDocContentsActionMap {

        protected final Map<String, Object> _lastaDocContentsMap;
        protected final DfPropertyValueHandler _propertyValueHandler;

        protected Map<String, Object> _actionMap; // cache

        public DfLastaDocContentsActionMap(Map<String, Object> lastaDocContentsMap, DfPropertyValueHandler propertyValueHandler) {
            _lastaDocContentsMap = lastaDocContentsMap;
            _propertyValueHandler = propertyValueHandler;
        }

        protected Map<String, Object> getActionMap() {
            if (_actionMap != null) {
                return _actionMap;
            }
            final Object obj = _lastaDocContentsMap.get("actionMap");
            if (obj == null) {
                _actionMap = DfCollectionUtil.emptyMap();
            } else {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) obj;
                _actionMap = map;
            }
            return _actionMap;
        }

        public boolean isSuppressDescriptionInList() {
            return _propertyValueHandler.isProperty("isSuppressDescriptionInList", false, getActionMap());
        }

        public boolean isSuppressAuthorInList() {
            return _propertyValueHandler.isProperty("isSuppressAuthorInList", false, getActionMap());
        }
    }
}
