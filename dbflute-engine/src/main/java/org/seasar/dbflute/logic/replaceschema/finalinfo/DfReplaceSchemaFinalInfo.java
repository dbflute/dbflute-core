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
package org.seasar.dbflute.logic.replaceschema.finalinfo;

/**
 * @author jflute
 */
public class DfReplaceSchemaFinalInfo {

    protected DfCreateSchemaFinalInfo _createSchemaFinalInfo;
    protected DfLoadDataFinalInfo _loadDataFinalInfo;
    protected DfTakeFinallyFinalInfo _takeFinallyFinalInfo;

    public DfReplaceSchemaFinalInfo(DfCreateSchemaFinalInfo createSchemaFinalInfo,
            DfLoadDataFinalInfo loadDataFinalInfo, DfTakeFinallyFinalInfo takeFinallyFinalInfo) {
        _createSchemaFinalInfo = createSchemaFinalInfo;
        _loadDataFinalInfo = loadDataFinalInfo;
        _takeFinallyFinalInfo = takeFinallyFinalInfo;
    }

    public boolean hasFailure() {
        return isCreateSchemaFailure() || isLoadDataFailure() || isTakeFinallyFailure();
    }

    public boolean isCreateSchemaFailure() {
        if (_createSchemaFinalInfo != null && _createSchemaFinalInfo.isFailure()) {
            return true;
        }
        return false;
    }

    public boolean isLoadDataFailure() {
        if (_loadDataFinalInfo != null && _loadDataFinalInfo.isFailure()) {
            return true;
        }
        return false;
    }

    public boolean isTakeFinallyFailure() {
        if (_takeFinallyFinalInfo != null && _takeFinallyFinalInfo.isFailure()) {
            return true;
        }
        return false;
    }

    public DfCreateSchemaFinalInfo getCreateSchemaFinalInfo() {
        return _createSchemaFinalInfo;
    }

    public DfLoadDataFinalInfo getLoadDataFinalInfo() {
        return _loadDataFinalInfo;
    }

    public DfTakeFinallyFinalInfo getTakeFinallyFinalInfo() {
        return _takeFinallyFinalInfo;
    }
}
