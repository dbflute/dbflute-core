/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.replaceschema.process.arrangebefore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.3.1 (2025/12/27 Saturday at ikspiari)
 */
public class DfArrangeBeforeRepsReady {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String KEY_DEFINE = "define";
    protected static final String KEY_COPY = "copy";
    protected static final String KEY_FILTER_TEXT = "filterText";
    protected static final String KEY_FILTER_TEXT_REPLACE_LINELY = "replaceLinely";
    protected static final String KEY_FILTER_TEXT_REPLACE_WHOLLY = "replaceWholly";
    protected static final String KEY_SCRIPT = "script";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, Object>> _arrangeBeforeRepsMap; // not null, read-only
    protected final Map<String, String> _defineMap; // not null, read-only
    protected final Map<String, String> _copyMap; // not null, read-only
    protected final Map<String, Object> _filterTextMap; // not null, read-only
    protected final Map<String, Object> _scriptMap; // not null, read-only

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfArrangeBeforeRepsReady(Map<String, Map<String, Object>> arrangeBeforeRepsMap) {
        _arrangeBeforeRepsMap = arrangeBeforeRepsMap;
        _defineMap = prepareDefineMap();
        _copyMap = prepareCopyMap();
        _filterTextMap = prepareFilterTextMap();
        _scriptMap = prepareScriptMap();
    }

    // -----------------------------------------------------
    //                                                Define
    //                                                ------
    protected Map<String, String> prepareDefineMap() {
        final Map<String, Object> elementMap = _arrangeBeforeRepsMap.get(KEY_DEFINE);
        if (elementMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, String> defineMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : elementMap.entrySet()) {
            defineMap.put(entry.getKey(), (String) entry.getValue());
        }
        return Collections.unmodifiableMap(defineMap);
    }

    // -----------------------------------------------------
    //                                           Copy (File)
    //                                           -----------
    public Map<String, String> prepareCopyMap() { // should be after prepareDefineMap()
        final Map<String, Object> plainMap = _arrangeBeforeRepsMap.get(KEY_COPY);
        if (plainMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, String> copyMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : plainMap.entrySet()) {
            final String key = filterDefinePath(entry.getKey());
            final String value = filterDefinePath((String) entry.getValue());
            copyMap.put(key, value);
        }
        return Collections.unmodifiableMap(copyMap);
    }

    // -----------------------------------------------------
    //                                           Filter Text
    //                                           -----------
    public Map<String, Object> prepareFilterTextMap() { // should be after prepareDefineMap()
        final Map<String, Object> filterTextPlainMap = _arrangeBeforeRepsMap.get(KEY_FILTER_TEXT);
        if (filterTextPlainMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, Object> filterTextNewMap = new LinkedHashMap<String, Object>();
        for (Entry<String, Object> rootEntry : filterTextPlainMap.entrySet()) {
            final String key = rootEntry.getKey(); // e.g. replaceLinely, replaceWholly
            if (KEY_FILTER_TEXT_REPLACE_LINELY.equals(key)) {
                final Map<String, Object> replaceLinelyNewMap = new LinkedHashMap<String, Object>();
                @SuppressWarnings("unchecked")
                final Map<String, Object> replaceLinelyPlainMap = (Map<String, Object>) rootEntry.getValue();
                for (Entry<String, Object> replaceLinelyEntry : replaceLinelyPlainMap.entrySet()) {
                    final String filteredPath = filterDefinePath(replaceLinelyEntry.getKey());
                    replaceLinelyNewMap.put(filteredPath, replaceLinelyEntry.getValue());
                }
                filterTextNewMap.put(key, Collections.unmodifiableMap(replaceLinelyNewMap));
            } else if (KEY_FILTER_TEXT_REPLACE_WHOLLY.equals(key)) {
                final Map<String, Object> replaceWhollyNewMap = new LinkedHashMap<String, Object>();
                @SuppressWarnings("unchecked")
                final Map<String, Object> replaceWhollyPlainMap = (Map<String, Object>) rootEntry.getValue();
                for (Entry<String, Object> replaceWhollyEntry : replaceWhollyPlainMap.entrySet()) {
                    final String filteredPath = filterDefinePath(replaceWhollyEntry.getKey());
                    replaceWhollyNewMap.put(filteredPath, replaceWhollyEntry.getValue());
                }
                filterTextNewMap.put(key, Collections.unmodifiableMap(replaceWhollyNewMap));
            } else { // others, add if new command at future (2025/12/27)
                filterTextNewMap.put(key, rootEntry.getValue());
            }
        }
        return Collections.unmodifiableMap(filterTextNewMap);
    }

    // -----------------------------------------------------
    //                                                Script
    //                                                ------
    protected Map<String, Object> prepareScriptMap() { // should be after prepareDefineMap()
        final Map<String, Object> plainMap = _arrangeBeforeRepsMap.get(KEY_SCRIPT);
        if (plainMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, Object> scriptMap = new LinkedHashMap<String, Object>();
        for (Entry<String, Object> entry : plainMap.entrySet()) {
            final String key = filterDefinePath(entry.getKey());
            // value is dummy for now
            //final String value = filterDefinePath((String) entry.getValue());
            scriptMap.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(scriptMap);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String filterDefinePath(String path) {
        if (!_defineMap.isEmpty()) {
            return Srl.replaceBy(path, _defineMap);
        } else {
            return path;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                           Copy (File)
    //                                           -----------
    public Map<String, String> getCopyMap() {
        return _copyMap;
    }

    // -----------------------------------------------------
    //                                           Filter Text
    //                                           -----------
    protected Map<String, Object> getFilterTextMap() {
        return _filterTextMap;
    }

    public Map<String, Object> getFilterTextReplaceLinelyMap() {
        final String key = KEY_FILTER_TEXT_REPLACE_LINELY;
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) getFilterTextMap().get(key);
        if (map != null) {
            return map;
        } else {
            return DfCollectionUtil.emptyMap();
        }
    }

    public Map<String, Object> getFilterTextReplaceWhollyMap() {
        final String key = KEY_FILTER_TEXT_REPLACE_WHOLLY;
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) getFilterTextMap().get(key);
        if (map != null) {
            return map;
        } else {
            return DfCollectionUtil.emptyMap();
        }
    }

    // -----------------------------------------------------
    //                                                Script
    //                                                ------
    public Map<String, Object> getScriptMap() {
        return _scriptMap;
    }
}
