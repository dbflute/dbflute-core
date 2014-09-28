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

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfProcedureArgumentInfo {

    protected String _packageName;
    protected String _objectName;
    protected String _overload;
    protected String _sequence;
    protected String _argumentName;
    protected String _inOut;
    protected String _dataType;
    protected String _dataLength;
    protected String _dataPrecision;
    protected String _dataScale;
    protected String _typeOwner;
    protected String _typeName;
    protected String _typeSubName;

    public String buildArrayTypeName() {
        final String typeName = getTypeName();
        final String typeSubName = getTypeSubName();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(typeSubName)) {
            // *typeOwner handling is under review
            //final String typeOwner = argInfo.getTypeOwner();
            return typeName + "." + typeSubName;
        } else {
            // *it may need to add typeOwner if additional schema at the future
            return typeName;
        }
    }

    public String getPackageName() {
        return _packageName;
    }

    public void setPackageName(String packageName) {
        this._packageName = packageName;
    }

    public String getObjectName() {
        return _objectName;
    }

    public void setObjectName(String objectName) {
        this._objectName = objectName;
    }

    public String getOverload() {
        return _overload;
    }

    public void setOverload(String overload) {
        this._overload = overload;
    }

    public String getSequence() {
        return _sequence;
    }

    public void setSequence(String sequence) {
        this._sequence = sequence;
    }

    public String getArgumentName() {
        return _argumentName;
    }

    public void setArgumentName(String argumentName) {
        this._argumentName = argumentName;
    }

    public String getInOut() {
        return _inOut;
    }

    public void setInOut(String inOut) {
        this._inOut = inOut;
    }

    public String getDataType() {
        return _dataType;
    }

    public void setDataType(String dataType) {
        this._dataType = dataType;
    }

    public String getDataLength() {
        return _dataLength;
    }

    public void setDataLength(String dataLength) {
        this._dataLength = dataLength;
    }

    public String getDataPrecision() {
        return _dataPrecision;
    }

    public void setDataPrecision(String dataPrecision) {
        this._dataPrecision = dataPrecision;
    }

    public String getDataScale() {
        return _dataScale;
    }

    public void setDataScale(String dataScale) {
        this._dataScale = dataScale;
    }

    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String typeName) {
        this._typeName = typeName;
    }

    public String getTypeOwner() {
        return _typeOwner;
    }

    public void setTypeOwner(String typeOwner) {
        this._typeOwner = typeOwner;
    }

    public String getTypeSubName() {
        return _typeSubName;
    }

    public void setTypeSubName(String typeSubName) {
        this._typeSubName = typeSubName;
    }
}
