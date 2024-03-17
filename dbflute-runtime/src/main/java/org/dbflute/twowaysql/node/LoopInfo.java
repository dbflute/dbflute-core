/*
 * Copyright 2014-2024 the original author or authors.
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

import java.util.List;

import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.9.7.0 (2010/05/29 Saturday)
 */
public class LoopInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _expression; // as loop meta information
    protected String _specifiedSql; // as loop meta information
    protected List<?> _parameterList;
    protected int _loopSize;
    protected FilteringBindOption _filteringBindOption;
    protected int _loopIndex;
    protected LoopInfo _parentLoop;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _loopIndex + "/" + _loopSize + ", " + _parameterList + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExpression() {
        return _expression;
    }

    public void setExpression(String expression) {
        _expression = expression;
    }

    public String getSpecifiedSql() {
        return _specifiedSql;
    }

    public void setSpecifiedSql(String specifiedSql) {
        _specifiedSql = specifiedSql;
    }

    public List<?> getParameterList() {
        return _parameterList;
    }

    public void setParameterList(List<?> parameterList) {
        _parameterList = parameterList;
    }

    public int getLoopSize() {
        return _loopSize;
    }

    public void setLoopSize(int loopSize) {
        _loopSize = loopSize;
    }

    public FilteringBindOption getFilteringBindOption() {
        return _filteringBindOption;
    }

    public void setFilteringBindOption(FilteringBindOption filteringBindOption) {
        _filteringBindOption = filteringBindOption;
    }

    public int getLoopIndex() {
        return _loopIndex;
    }

    public void setLoopIndex(int loopIndex) {
        _loopIndex = loopIndex;
    }

    public LoopInfo getParentLoop() {
        return _parentLoop;
    }

    public void setParentLoop(LoopInfo parentLoop) {
        _parentLoop = parentLoop;
    }

    public Object getCurrentParameter() {
        return _parameterList.get(_loopIndex);
    }

    public Class<?> getCurrentParameterType() {
        final Object parameter = getCurrentParameter();
        return parameter != null ? parameter.getClass() : null;
    }
}
