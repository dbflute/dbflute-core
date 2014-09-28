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
package org.apache.torque.engine.database.model;

import java.math.BigDecimal;

import org.apache.torque.engine.database.transform.XmlToAppData.XmlReadingFilter;
import org.xml.sax.Attributes;

/**
 * @author jflute (this class is new-created by DBFlute but located in Torque package to keep structure)
 * @since 0.9.9.7F (2012/08/20 Monday)
 */
public class Sequence {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Database _database;
    protected String _sequenceName;
    protected UnifiedSchema _unifiedSchema;
    protected BigDecimal _minimumValue;
    protected BigDecimal _maximumValue;
    protected Integer _incrementSize;
    protected String _sequenceComment; // for the future (2012/08/18)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // -----------------------------------------------------
    //                                         Load from XML
    //                                         -------------
    public boolean loadFromXML(Attributes attrib, XmlReadingFilter readingFilter) {
        _sequenceName = attrib.getValue("name"); // sequence name
        _unifiedSchema = UnifiedSchema.createAsDynamicSchema(attrib.getValue("schema"));
        if (readingFilter != null && readingFilter.isSequenceExcept(_unifiedSchema, _sequenceName)) {
            return false;
        }
        final String minimumValue = attrib.getValue("minimumValue");
        if (minimumValue != null) {
            try {
                _minimumValue = new BigDecimal(minimumValue);
            } catch (NumberFormatException ignored) { // just in case
            }
        }
        final String maximumValue = attrib.getValue("maximumValue");
        if (maximumValue != null) {
            try {
                _maximumValue = new BigDecimal(maximumValue);
            } catch (NumberFormatException ignored) { // just in case
            }
        }
        final String incrementSize = attrib.getValue("incrementSize");
        if (incrementSize != null) {
            try {
                _incrementSize = Integer.parseInt(incrementSize);
            } catch (NumberFormatException ignored) { // just in case
            }
        }
        return true;
    }

    // ===================================================================================
    //                                                                    Derived Property
    //                                                                    ================
    public String getFormalUniqueName() {
        return _unifiedSchema.getCatalogSchema() + "." + _sequenceName;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return _unifiedSchema + "." + _sequenceName + ":{" + _minimumValue + " to " + _maximumValue + ", increment "
                + _incrementSize + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Database getDatabase() {
        return _database;
    }

    public void setDatabase(Database database) {
        this._database = database;
    }

    public String getSequenceName() {
        return _sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this._sequenceName = sequenceName;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        this._unifiedSchema = unifiedSchema;
    }

    public BigDecimal getMinimumValue() {
        return _minimumValue;
    }

    public void setMinimumValue(BigDecimal minimumValue) {
        this._minimumValue = minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return _maximumValue;
    }

    public void setMaximumValue(BigDecimal maximumValue) {
        this._maximumValue = maximumValue;
    }

    public Integer getIncrementSize() {
        return _incrementSize;
    }

    public void setIncrementSize(Integer incrementSize) {
        this._incrementSize = incrementSize;
    }

    public String getSequenceComment() {
        return _sequenceComment;
    }

    public void setSequenceComment(String sequenceComment) {
        this._sequenceComment = sequenceComment;
    }
}
