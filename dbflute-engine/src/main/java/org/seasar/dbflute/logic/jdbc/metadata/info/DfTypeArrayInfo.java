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

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.6 (2010/11/18 Thursday)
 */
public class DfTypeArrayInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UnifiedSchema _unifiedSchema; // required at first
    protected final String _typeName; // required at first
    protected String _elementType; // required at first (if unknown, the value is "Unknown")
    protected DfTypeArrayInfo _nestedArrayInfo; // if element type is ARRAY (nested)
    protected DfTypeStructInfo _elementStructInfo; // if element type is STRUCT
    protected String _elementJavaNative; // is set after analyzing

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfTypeArrayInfo(UnifiedSchema unifiedSchema, String typeName) {
        _unifiedSchema = unifiedSchema;
        _typeName = typeName;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasNestedArray() { // means the element type is ARRAY
        return _nestedArrayInfo != null;
    }

    public boolean hasElementStructInfo() { // means the element type is STRUCT
        return _elementStructInfo != null;
    }

    public boolean hasElementJavaNative() {
        return _elementJavaNative != null;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    @Override
    public String toString() {
        return _typeName + toStringElementOnly();
    }

    public String toStringForHtml() {
        return _typeName + toStringElementOnlyForHtml();
    }

    public String toStringElementOnly() {
        return doToStringElementOnly(false);
    }

    public String toStringElementOnlyForHtml() {
        return doToStringElementOnly(true);
    }

    protected String doToStringElementOnly(boolean escape) {
        final StringBuilder sb = new StringBuilder();
        sb.append(escape ? "&lt;" : "<");
        if (_nestedArrayInfo != null) {
            sb.append(escape ? _nestedArrayInfo.toStringForHtml() : _nestedArrayInfo.toString());
        } else if (_elementStructInfo != null) {
            sb.append(_elementStructInfo.toStringSimple());
        } else {
            sb.append(_elementType);
        }
        sb.append(escape ? "&gt;" : ">");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public String getTypeName() { // unique name (with schema prefix)
        return _typeName;
    }

    public String getTypePureName() {
        return Srl.substringLastRear(_typeName, ".");
    }

    public String getTypeSqlName() {
        return _unifiedSchema.isAdditionalSchema() ? getTypeName() : getTypePureName();
    }

    public String getElementType() {
        return _elementType;
    }

    public void setElementType(String elementType) {
        this._elementType = elementType;
    }

    public DfTypeArrayInfo getNestedArrayInfo() {
        return _nestedArrayInfo;
    }

    public void setNestedArrayInfo(DfTypeArrayInfo nestedArrayInfo) {
        this._nestedArrayInfo = nestedArrayInfo;
    }

    public DfTypeStructInfo getElementStructInfo() {
        return _elementStructInfo;
    }

    public void setElementStructInfo(DfTypeStructInfo structInfo) {
        this._elementStructInfo = structInfo;
    }

    public String getElementJavaNative() {
        return _elementJavaNative;
    }

    public void setElementJavaNative(String elementJavaNative) {
        this._elementJavaNative = elementJavaNative;
    }
}
