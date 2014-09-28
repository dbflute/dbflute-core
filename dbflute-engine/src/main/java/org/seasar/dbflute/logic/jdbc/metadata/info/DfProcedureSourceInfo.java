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

/**
 * @author jflute
 */
public class DfProcedureSourceInfo {

    protected String _sourceCode; // main code or all code
    protected String _supplementCode; // e.g. parameters on MySQL
    protected Integer _sourceLine; // not accurate
    protected Integer _sourceSize; // not accurate

    public String toSourceHash() {
        final String resource = (_sourceCode != null ? _sourceCode : "")
                + (_supplementCode != null ? "\n" + _supplementCode : "");
        return Integer.toHexString(resource.hashCode());
    }

    @Override
    public String toString() {
        return "{" + _sourceLine + ", " + _sourceSize + ", " + toSourceHash() + "}";
    }

    public String getSourceCode() {
        return _sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this._sourceCode = sourceCode;
    }

    public String getSupplementCode() {
        return _supplementCode;
    }

    public void setSupplementCode(String supplementCode) {
        this._supplementCode = supplementCode;
    }

    public Integer getSourceLine() {
        return _sourceLine;
    }

    public void setSourceLine(Integer sourceLine) {
        this._sourceLine = sourceLine;
    }

    public Integer getSourceSize() {
        return _sourceSize;
    }

    public void setSourceSize(Integer sourceSize) {
        this._sourceSize = sourceSize;
    }
}
