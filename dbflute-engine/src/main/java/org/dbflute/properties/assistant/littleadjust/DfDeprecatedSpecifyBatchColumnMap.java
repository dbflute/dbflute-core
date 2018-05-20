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
package org.dbflute.properties.assistant.littleadjust;

import java.util.List;
import java.util.Map;

import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/23 Friday)
 */
public class DfDeprecatedSpecifyBatchColumnMap {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String KEY_deprecatedSpecifyBatchColumnMap = "deprecatedSpecifyBatchColumnMap";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> _littleAdjustmentMap;
    protected final DfPropertyValueHandler _propertyValueHandler;

    protected Map<String, Object> _deprecatedSpecifyBatchColumnMap; // cache

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDeprecatedSpecifyBatchColumnMap(Map<String, Object> littleAdjustmentMap, DfPropertyValueHandler propertyValueHandler) {
        _littleAdjustmentMap = littleAdjustmentMap;
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                            Base Map
    //                                                                            ========
    // ; deprecatedSpecifyBatchColumnMap = map:{
    //     ; deprecatedComment = ...
    //     ; tableExceptList = list:{}
    //     ; tableTargetList = list:{}
    // }
    protected Map<String, Object> getDeprecatedSpecifyBatchColumnMap() {
        if (_deprecatedSpecifyBatchColumnMap != null) {
            return _deprecatedSpecifyBatchColumnMap;
        }
        final Object obj = _littleAdjustmentMap.get(KEY_deprecatedSpecifyBatchColumnMap);
        if (obj == null) {
            _deprecatedSpecifyBatchColumnMap = DfCollectionUtil.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Object> repsMap = (Map<String, Object>) obj;
            _deprecatedSpecifyBatchColumnMap = repsMap;
        }
        return _deprecatedSpecifyBatchColumnMap;
    }

    public boolean isDeprecated() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getDeprecatedComment());
    }

    public String getDeprecatedComment() {
        return _propertyValueHandler.getProperty("deprecatedComment", null, getDeprecatedSpecifyBatchColumnMap());
    }

    public boolean isTableTarget(String tableDbName) {
        final Map<String, Object> map = getDeprecatedSpecifyBatchColumnMap();
        @SuppressWarnings("unchecked")
        final List<String> tableTargetList = (List<String>) map.getOrDefault("tableTargetList", DfCollectionUtil.emptyList());
        @SuppressWarnings("unchecked")
        final List<String> tableExceptList = (List<String>) map.getOrDefault("tableExceptList", DfCollectionUtil.emptyList());
        return DfNameHintUtil.isTargetByHint(tableDbName, tableTargetList, tableExceptList);
    }
}
