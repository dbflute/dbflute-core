/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.twowaysql.node;

/**
 * The object of one bound value. (contains embedded)
 * @author jflute
 */
public class BoundValue {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Object _firstValue;
    protected Class<?> _firstType;
    protected Object _targetValue;
    protected Class<?> _targetType;
    protected FilteringBindOption _filteringBindOption;

    // ===================================================================================
    //                                                                         Rear Option
    //                                                                         ===========
    public void filterValueByOptionIfNeeds() {
        if (_filteringBindOption == null || _targetValue == null) {
            return;
        }
        if (_targetValue instanceof String) {
            _targetValue = _filteringBindOption.generateRealValue((String) _targetValue);
        }
    }

    public String buildRearOptionOnSql() {
        if (_filteringBindOption == null || _targetValue == null) {
            return null;
        }
        if (_targetValue instanceof String) {
            final String rearOption = _filteringBindOption.getRearOption();
            return " " + rearOption.trim() + " ";
        } else {
            return null;
        }
    }

    protected void inheritLikeSearchOptionIfNeeds(LoopInfo loopInfo) {
        if (loopInfo == null || _filteringBindOption != null) {
            return;
        }
        final FilteringBindOption parent = loopInfo.getFilteringBindOption();
        if (parent != null) {
            _filteringBindOption = parent; // inherit
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Object getFirstValue() {
        return _firstValue;
    }

    public void setFirstValue(Object firstValue) {
        _firstValue = firstValue;
    }

    public Class<?> getFirstType() {
        return _firstType;
    }

    public void setFirstType(Class<?> firstType) {
        _firstType = firstType;
    }

    public Object getTargetValue() {
        return _targetValue;
    }

    public void setTargetValue(Object targetValue) {
        _targetValue = targetValue;
    }

    public Class<?> getTargetType() {
        return _targetType;
    }

    public void setTargetType(Class<?> targetType) {
        _targetType = targetType;
    }

    public FilteringBindOption getFilteringBindOption() {
        return _filteringBindOption;
    }

    public void setFilteringBindOption(FilteringBindOption filteringBindOption) {
        _filteringBindOption = filteringBindOption;
    }
}
