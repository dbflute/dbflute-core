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
package org.seasar.dbflute.helper.jprop;

import java.util.List;

import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class JavaPropertiesProperty {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _propertyKey; // errors.required
    protected final String _propertyValue;
    protected final boolean _canBeIntegerProperty;
    protected final boolean _canBeLongProperty;
    protected final boolean _canBeDecimalProperty;
    protected final boolean _canBeDateProperty;
    protected final boolean _mayBeBooleanProperty;
    protected String _defName; // e.g. ERRORS_REQUIRED
    protected String _camelizedName;
    protected String _capCamelName;
    protected String _uncapCamelName;
    protected String _variableArgDef;
    protected String _variableArgSet;
    protected List<Integer> _variableNumberList;
    protected String _comment;
    protected boolean _extends;
    protected boolean _override;
    protected boolean _secure;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public JavaPropertiesProperty(String propertyKey, String propertyValue) {
        _propertyKey = propertyKey;
        _propertyValue = propertyValue;
        _canBeIntegerProperty = deriveCanBeIntegerProperty();
        _canBeLongProperty = deriveCanBeLongProperty();
        _canBeDecimalProperty = deriveCanBeDecimalProperty();
        _canBeDateProperty = deriveCanBeDateProperty();
        _mayBeBooleanProperty = deriveMayBeBooleanProperty();
    }

    // -----------------------------------------------------
    //                                         Deriving Type
    //                                         -------------
    protected boolean deriveCanBeIntegerProperty() {
        if (_propertyValue != null) {
            try {
                DfTypeUtil.toInteger(_propertyValue);
                return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    protected boolean deriveCanBeLongProperty() {
        if (_propertyValue != null) {
            try {
                DfTypeUtil.toLong(_propertyValue);
                return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    protected boolean deriveCanBeDecimalProperty() {
        if (_propertyValue != null) {
            try {
                DfTypeUtil.toBigDecimal(_propertyValue);
                return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    protected boolean deriveCanBeDateProperty() {
        if (_propertyValue != null) {
            try {
                DfTypeUtil.toDate(_propertyValue);
                return true;
            } catch (RuntimeException ignored) {
            }
        }
        return false;
    }

    protected boolean deriveMayBeBooleanProperty() {
        return _propertyValue != null && isTrueOrFalseProperty(_propertyValue.trim());
    }

    protected boolean isTrueOrFalseProperty(String propertyValue) {
        return propertyValue.equalsIgnoreCase("true") || propertyValue.equalsIgnoreCase("false");
    }

    // ===================================================================================
    //                                                                    Derived Property
    //                                                                    ================
    public boolean mayBeIntegerProperty() {
        return _canBeIntegerProperty;
    }

    public boolean mayBeLongProperty() {
        return !mayBeIntegerProperty() && _canBeLongProperty;
    }

    public boolean mayBeDecimalProperty() {
        return !mayBeIntegerProperty() && !mayBeLongProperty() && _canBeDecimalProperty;
    }

    protected boolean mayBeNumber() {
        return mayBeIntegerProperty() || mayBeLongProperty() || mayBeDecimalProperty();
    }

    public boolean mayBeDateProperty() {
        return !mayBeNumber() && _canBeDateProperty;
    }

    public boolean mayBeBooleanProperty() {
        return _mayBeBooleanProperty;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JavaPropertiesProperty)) {
            return false;
        }
        final JavaPropertiesProperty another = (JavaPropertiesProperty) obj;
        return _propertyKey.equals(another._propertyKey);
    }

    @Override
    public int hashCode() {
        return _propertyKey.hashCode();
    }

    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _propertyKey + ", " + _propertyValue + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getPropertyKey() {
        return _propertyKey;
    }

    public String getPropertyValue() {
        return _propertyValue;
    }

    public String getDefName() {
        return _defName;
    }

    public void setDefName(String defName) {
        _defName = defName;
    }

    public String getCamelizedName() {
        return _camelizedName;
    }

    public void setCamelizedName(String camelizedName) {
        _camelizedName = camelizedName;
    }

    public String getCapCamelName() {
        return _capCamelName;
    }

    public void setCapCamelName(String capCamelName) {
        _capCamelName = capCamelName;
    }

    public String getUncapCamelName() {
        return _uncapCamelName;
    }

    public void setUncapCamelName(String uncapCamelName) {
        _uncapCamelName = uncapCamelName;
    }

    public String getVariableArgDef() {
        return _variableArgDef;
    }

    public void setVariableArgDef(String variableArgDef) {
        _variableArgDef = variableArgDef;
    }

    public String getVariableArgSet() {
        return _variableArgSet;
    }

    public void setVariableArgSet(String variableArgSet) {
        _variableArgSet = variableArgSet;
    }

    public List<Integer> getVariableNumberList() {
        return _variableNumberList;
    }

    public void setVariableNumberList(List<Integer> variableNumberList) {
        _variableNumberList = variableNumberList;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public boolean isExtends() {
        return _extends;
    }

    public void toBeExtends() {
        _extends = true;
    }

    public boolean isOverride() {
        return _override;
    }

    public void toBeOverride() {
        _override = true;
    }

    public boolean isSecure() {
        return _secure;
    }

    public void toBeSecure() {
        _secure = true;
    }
}
