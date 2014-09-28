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
package org.seasar.dbflute.s2dao.rowcreator.impl;

import java.util.Map;

import org.seasar.dbflute.s2dao.rowcreator.TnRelationKey;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public final class TnRelationKeyCompound implements TnRelationKey {

    private final Map<String, Object> _relKeyValues;
    private final int _hashCode;

    public TnRelationKeyCompound(Map<String, Object> relKeyValues) {
        _relKeyValues = relKeyValues;
        _hashCode = relKeyValues.hashCode();
    }

    public Map<String, Object> getRelKeyValues() {
        return _relKeyValues;
    }

    public boolean containsColumn(String columnLabel) {
        return _relKeyValues.containsKey(columnLabel);
    }

    public Object extractKeyValue(String columnLabel) {
        return _relKeyValues.get(columnLabel);
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TnRelationKeyCompound)) {
            return false;
        }
        return _relKeyValues.equals(((TnRelationKeyCompound) o)._relKeyValues);
    }

    @Override
    public String toString() {
        return _relKeyValues.toString();
    }
}
