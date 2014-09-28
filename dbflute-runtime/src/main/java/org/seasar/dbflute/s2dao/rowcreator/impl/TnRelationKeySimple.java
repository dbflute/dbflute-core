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

import org.seasar.dbflute.s2dao.rowcreator.TnRelationKey;

/**
 * @author jflute
 */
public final class TnRelationKeySimple implements TnRelationKey {

    private final String _columnLabel;
    private final Object _keyValue;
    private final int _hashCode;

    public TnRelationKeySimple(String columnLabel, Object keyValue) {
        _columnLabel = columnLabel;
        _keyValue = keyValue;
        _hashCode = keyValue.hashCode();
    }

    public boolean containsColumn(String columnLabel) {
        return _columnLabel.equals(columnLabel);
    }

    public Object extractKeyValue(String columnLabel) {
        return containsColumn(columnLabel) ? _keyValue : null;
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TnRelationKeySimple)) {
            return false;
        }
        return _keyValue.equals(((TnRelationKeySimple) o)._keyValue);
    }

    @Override
    public String toString() {
        return "{" + _columnLabel + "=" + _keyValue + "}";
    }
}
