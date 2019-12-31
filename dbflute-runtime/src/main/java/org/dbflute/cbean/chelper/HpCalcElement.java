/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.cbean.chelper;

import org.dbflute.cbean.coption.ColumnConversionOption;
import org.dbflute.cbean.dream.SpecifiedColumn;

/**
 * @author jflute
 */
public class HpCalcElement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected CalculationType _calculationType;
    protected Number _calculationValue;
    protected SpecifiedColumn _calculationColumn;
    protected ColumnConversionOption _columnConversionOption;
    protected boolean _preparedConvOption;

    // ===================================================================================
    //                                                                       Determination 
    //                                                                       =============
    public boolean hasCalculationValue() {
        return _calculationValue != null;
    }

    public boolean hasCalculationColumn() {
        return _calculationColumn != null;
    }

    // ===================================================================================
    //                                                                    Calculation Type 
    //                                                                    ================
    public static enum CalculationType {
        CONV("$$FUNC$$"), PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/");
        private String _operand;

        private CalculationType(String operand) {
            _operand = operand;
        }

        public String operand() {
            return _operand;
        }
    }

    // ===================================================================================
    //                                                                      Basic Override 
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _calculationType + ", number=" + _calculationValue + ", column=" + _calculationColumn + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public CalculationType getCalculationType() {
        return _calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        _calculationType = calculationType;
    }

    public Number getCalculationValue() {
        return _calculationValue;
    }

    public void setCalculationValue(Number calculationValue) {
        _calculationValue = calculationValue;
    }

    public SpecifiedColumn getCalculationColumn() {
        return _calculationColumn;
    }

    public void setCalculationColumn(SpecifiedColumn calculationColumn) {
        _calculationColumn = calculationColumn;
    }

    public ColumnConversionOption getColumnConversionOption() {
        return _columnConversionOption;
    }

    public void setColumnConversionOption(ColumnConversionOption columnConversionOption) {
        _columnConversionOption = columnConversionOption;
    }

    public boolean isPreparedConvOption() {
        return _preparedConvOption;
    }

    public void setPreparedConvOption(boolean preparedConvOption) {
        _preparedConvOption = preparedConvOption;
    }
}
