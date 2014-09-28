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
package org.seasar.dbflute.cbean.chelper;

import org.seasar.dbflute.cbean.coption.ColumnConversionOption;

/**
 * @author jflute
 */
public class HpCalcElement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected CalculationType _calculationType;
    protected Number _calculationValue;
    protected HpSpecifiedColumn _calculationColumn;
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
        this._calculationType = calculationType;
    }

    public Number getCalculationValue() {
        return _calculationValue;
    }

    public void setCalculationValue(Number calculationValue) {
        this._calculationValue = calculationValue;
    }

    public HpSpecifiedColumn getCalculationColumn() {
        return _calculationColumn;
    }

    public void setCalculationColumn(HpSpecifiedColumn calculationColumn) {
        this._calculationColumn = calculationColumn;
    }

    public ColumnConversionOption getColumnConversionOption() {
        return _columnConversionOption;
    }

    public void setColumnConversionOption(ColumnConversionOption columnConversionOption) {
        this._columnConversionOption = columnConversionOption;
    }

    public boolean isPreparedConvOption() {
        return _preparedConvOption;
    }

    public void setPreparedConvOption(boolean preparedConvOption) {
        this._preparedConvOption = preparedConvOption;
    }
}
