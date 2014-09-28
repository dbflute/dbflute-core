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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.math.BigDecimal;

/**
 * @author jflute
 */
public class DfSequenceMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String sequenceCatalog; // nullable
    protected String sequenceSchema;
    protected String sequenceName;
    protected BigDecimal minimumValue;
    protected BigDecimal maximumValue;
    protected Integer incrementSize;

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return (sequenceCatalog != null ? sequenceCatalog + "." : "") + sequenceSchema + "." + sequenceName + ":{"
                + minimumValue + " to " + maximumValue + ", increment " + incrementSize + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getSequenceCatalog() {
        return sequenceCatalog;
    }

    public void setSequenceCatalog(String sequenceCatalog) {
        this.sequenceCatalog = sequenceCatalog;
    }

    public String getSequenceSchema() {
        return sequenceSchema;
    }

    public void setSequenceSchema(String sequenceSchema) {
        this.sequenceSchema = sequenceSchema;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(BigDecimal minimumValue) {
        this.minimumValue = minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(BigDecimal maximumValue) {
        this.maximumValue = maximumValue;
    }

    public Integer getIncrementSize() {
        return incrementSize;
    }

    public void setIncrementSize(Integer incrementSize) {
        this.incrementSize = incrementSize;
    }
}
