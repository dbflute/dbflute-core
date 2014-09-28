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
package org.seasar.dbflute.helper.dataset;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.dataset.types.DfDtsColumnType;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDataColumn {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _columnDbName;
    protected DfDtsColumnType _columnType; // can be overridden
    protected int _columnIndex; // can be overridden
    protected boolean _primaryKey = false;
    protected boolean _writable = true;
    protected String _formatPattern;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDataColumn(String columnDbName, DfDtsColumnType columnType, int columnIndex) {
        _columnDbName = columnDbName;
        _columnType = columnType;
        _columnIndex = columnIndex;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getColumnDbName() {
        return _columnDbName;
    }

    public String getColumnSqlName() {
        return quoteColumnNameIfNeeds(_columnDbName);
    }

    protected String quoteColumnNameIfNeeds(String columnDbName) {
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.quoteColumnNameIfNeedsDirectUse(columnDbName);
    }

    public DfDtsColumnType getColumnType() {
        return _columnType;
    }

    public void setColumnType(DfDtsColumnType columnType) {
        this._columnType = columnType;
    }

    public int getColumnIndex() {
        return _columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this._columnIndex = columnIndex;
    }

    public boolean isPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this._primaryKey = primaryKey;
    }

    public boolean isWritable() {
        return _writable;
    }

    public void setWritable(boolean writable) {
        this._writable = writable;
    }

    public String getFormatPattern() {
        return _formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this._formatPattern = formatPattern;
    }

    public Object convert(Object value) {
        return _columnType.convert(value, _formatPattern);
    }
}
