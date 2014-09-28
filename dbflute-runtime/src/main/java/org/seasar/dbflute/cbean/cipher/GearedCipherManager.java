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

import java.util.Map;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.helper.StringKeyMap;

/**
 * @author jflute
 * @since 0.9.8.4 (2011/05/21 Saturday)
 */
public class GearedCipherManager {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Map<String, ColumnFunctionCipher>> _cipherMap = StringKeyMap
            .createAsFlexibleConcurrent();

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    public void addFunctionFilter(ColumnInfo columnInfo, CipherFunctionFilter filter) {
        assertFunctionFilterArgument(columnInfo, filter);
        final String tableDbName = columnInfo.getDBMeta().getTableDbName();
        final String columnDbName = columnInfo.getColumnDbName();
        Map<String, ColumnFunctionCipher> elementMap = _cipherMap.get(tableDbName);
        if (elementMap == null) {
            elementMap = StringKeyMap.createAsFlexible();
            _cipherMap.put(tableDbName, elementMap);
        }
        ColumnFunctionCipher function = elementMap.get(columnDbName);
        if (function == null) {
            function = new ColumnFunctionCipher(columnInfo);
            elementMap.put(columnDbName, function);
        }
        function.addFunctionFilter(filter);
    }

    protected void assertFunctionFilterArgument(ColumnInfo columnInfo, CipherFunctionFilter filter) {
        if (columnInfo == null) {
            String msg = "The argument 'columnInfo' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (filter == null) {
            String msg = "The argument 'filter' should not be null: " + columnInfo;
            throw new IllegalArgumentException(msg);
        }
        if (columnInfo.isPrimary()) {
            String msg = "You cannot set a primary key column for cipher: " + columnInfo;
            throw new IllegalArgumentException(msg);
        }
        if (columnInfo.isForeignKey()) {
            String msg = "You cannot set a foreign key column for cipher: " + columnInfo;
            throw new IllegalArgumentException(msg);
        }
        if (columnInfo.isOptimisticLock()) {
            String msg = "You cannot set an optimistic lock column for cipher: " + columnInfo;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                              Cipher
    //                                                                              ======
    public ColumnFunctionCipher findColumnFunctionCipher(ColumnInfo columnInfo) {
        return findColumnFunctionCipher(columnInfo.getDBMeta().getTableDbName(), columnInfo.getColumnDbName());
    }

    public ColumnFunctionCipher findColumnFunctionCipher(String tableDbName, String columnDbName) {
        final Map<String, ColumnFunctionCipher> elementMap = _cipherMap.get(tableDbName);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get(columnDbName);
    }
}
