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
package org.seasar.dbflute.cbean.sqlclause.clause;

/**
 * @author jflute
 */
public enum SelectClauseType {

    COLUMNS(false, false, false, false) // normal
    , UNIQUE_COUNT(true, true, true, false) // basically for selectCount(cb)
    , PLAIN_COUNT(true, true, false, false) // basically for count of selectPage(cb)
    // scalar mainly for Behavior.scalarSelect(cb)
    , COUNT_DISTINCT(false, true, true, true) // count(distinct)
    , MAX(false, true, true, true), MIN(false, true, true, true) // max(), min()
    , SUM(false, true, true, true), AVG(false, true, true, true); // sum(), avg()

    private final boolean _count;
    private final boolean _scalar;
    private final boolean _uniqueScalar;
    private final boolean _specifiedScalar;

    private SelectClauseType(boolean count, boolean scalar, boolean uniqueScalar, boolean specifiedScalar) {
        _count = count;
        _scalar = scalar;
        _uniqueScalar = uniqueScalar;
        _specifiedScalar = specifiedScalar;
    }

    public boolean isCount() { // except count-distinct
        return _count;
    }

    public boolean isScalar() { // also contains count
        return _scalar;
    }

    /**
     * Should the scalar be selected uniquely?
     * @return The determination, true or false.
     */
    public boolean isUniqueScalar() { // not contains plain-count
        return _uniqueScalar;
    }

    /**
     * Does the scalar need specified only-one column?
     * @return The determination, true or false.
     */
    public boolean isSpecifiedScalar() { // not contains all-count
        return _specifiedScalar;
    }
}
