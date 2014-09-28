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
package org.seasar.dbflute.cbean.cipher;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;

/**
 * @author jflute
 * @since 0.9.8.4 (2011/05/21 Saturday)
 */
public class ColumnFunctionCipher {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ColumnInfo _columnInfo;
    protected final List<CipherFunctionFilter> _functionFilterList = new ArrayList<CipherFunctionFilter>(1);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ColumnFunctionCipher(ColumnInfo columnInfo) {
        _columnInfo = columnInfo;
    }

    // ===================================================================================
    //                                                                              Cipher
    //                                                                              ======
    public String encrypt(String valueExp) {
        for (CipherFunctionFilter filter : _functionFilterList) {
            valueExp = filter.encrypt(valueExp);
        }
        return valueExp;
    }

    public String decrypt(String valueExp) {
        for (CipherFunctionFilter filter : _functionFilterList) {
            valueExp = filter.decrypt(valueExp);
        }
        return valueExp;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public ColumnInfo getColumnInfo() {
        return _columnInfo;
    }

    public void addFunctionFilter(CipherFunctionFilter filter) {
        _functionFilterList.add(filter);
    }
}
